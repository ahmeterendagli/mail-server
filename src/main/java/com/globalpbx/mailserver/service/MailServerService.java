package com.globalpbx.mailserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.globalpbx.mailserver.constant.TableNameConstants;
import com.globalpbx.mailserver.dto.MailInfoDto;
import com.globalpbx.mailserver.repository.MailServerRepository;
import com.globalpbx.mailserver.repository.MailServerVersionRepository;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;


@Service
public class MailServerService {

    @Value("${sqlite.database.url}")
    private String databaseUrl;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    private static final Logger logger = LogManager.getLogger(MailServerService.class);
    private final ReentrantLock reentrantLock = new ReentrantLock();

    private final int numThreads = 10;

    ThreadPoolExecutor executor = new ThreadPoolExecutor(numThreads, numThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    private final String mailsTable = TableNameConstants.MAILS;

    private final String versionTable = TableNameConstants.VERSIONS;

    private MailServerRepository mailServerRepository;
    private MailServerVersionRepository mailServerVersionRepository;

    private final RedisTemplate<String, String> redisTemplate;

    public void addToQueue(String data) {
        redisTemplate.opsForList().rightPush(mailsTable, data);
    }

    public String processQueue() {
        return redisTemplate.opsForList().leftPop(mailsTable);
    }

    @Autowired
    public MailServerService(MailServerRepository mailServerRepository, MailServerVersionRepository mailServerVersionRepository, RedisTemplate<String, String> redisTemplate) {
        this.mailServerRepository = mailServerRepository;
        this.mailServerVersionRepository = mailServerVersionRepository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public void sendMailWithRedis(MailInfoDto mailInfoDto, Connection connection) {
        logger.info("Entry is -> " + mailInfoDto);
        reentrantLock.lock();
        try {
            String versionNumber = mailServerVersionRepository.findLastVersion(connection);
            if (Float.parseFloat(versionNumber) > -1) {
                if (Float.parseFloat(versionNumber) < mailInfoDto.getVersionNumber()) {
                    // new column added
                    String alterTableQuery = "ALTER TABLE " + mailsTable + " ADD COLUMN new_column_name varchar(255)";
                    try (Statement statement = connection.createStatement()) {
                        statement.executeUpdate(alterTableQuery);
                        logger.info("The new column has been added successfully.");
                    }
                }
                logger.info("Last Version Number: " + versionNumber);
            }


            String savedVersion = mailServerVersionRepository.saveVersion(connection, mailInfoDto);
            logger.info(savedVersion);

            Properties props = new Properties();

            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");


//            props.put("mail.smtp.host", "smtp.gmail.com");
//            props.put("mail.smtp.port", "465");
//            props.put("mail.smtp.auth", "true");
//            props.put("mail.smtp.ssl.enable", "true");
//            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//            props.put("mail.smtp.socketFactory.fallback","false");


            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            try {
                MimeMessage message = new MimeMessage(session);

                MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

                mimeMessageHelper.setFrom(new InternetAddress(username));
                mimeMessageHelper.setTo(mailInfoDto.getRecipient());
                mimeMessageHelper.setSubject(mailInfoDto.getSubject());
                if(mailInfoDto.getIsHtml() == null) {
                    mailInfoDto.setIsHtml(false);
                }
                mimeMessageHelper.setText(mailInfoDto.getBody(),mailInfoDto.getIsHtml());
                Transport.send(message);

                MailInfoDto savedMail = mailServerRepository.saveMail(connection, mailInfoDto);
                logger.info("Saved mail -> " + savedMail);
                logger.info("Email sent successfully.");

            } catch (MessagingException | SQLException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }

        } catch (SQLException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        } finally {
            reentrantLock.unlock();
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }


    public String sendMail(List<MailInfoDto> mailInfoDtoList) {
        for (MailInfoDto mailInfoDto : mailInfoDtoList) {
            executor.execute(() -> {
                try {
                    addToQueue(new ObjectMapper().writeValueAsString(mailInfoDto));
                } catch (JsonProcessingException e) {
                    logger.error(e.getMessage());
                    throw new RuntimeException(e);
                }
            });
        }
        return "Email sent successfully!";
    }

    @Scheduled(fixedRate = 10000) // Runs every 10 seconds
    public void mailSendAndTransferDatabase() throws JsonProcessingException, ClassNotFoundException, SQLException {
        while (executor.getPoolSize() - executor.getActiveCount() != 0) {
            String mail = processQueue();
            Connection connection;
            if (mail == null) {
                logger.info("queue is empty");
                break;
            }

            MailInfoDto storedMailInfo = new ObjectMapper().readValue(mail, MailInfoDto.class);

            // SQLite JDBC driver has been created
            Class.forName(databaseUrl);

            // SQLite db connection has been created
            connection = DriverManager.getConnection(storedMailInfo.getPath());

            logger.info("You have successfully connected to the SQLite database.");

            Connection finalConnection = connection;


            createTable(connection, versionTable);
            createTable(connection, mailsTable);
            executor.execute(() ->
                    sendMailWithRedis(storedMailInfo, finalConnection));


            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Executor shutting down");
                executor.shutdown();
            }));
        }
    }


    private void createTable(Connection connection, String tableName) {
        switch (tableName) {
            case mailsTable -> mailServerRepository.createMailsTable(connection);
            case versionTable -> mailServerVersionRepository.createVersionTable(connection);
            default -> {
                logger.error("Unsupported table name: " + tableName);
                throw new IllegalArgumentException("Unsupported table name: " + tableName);
            }
        }
    }
}
