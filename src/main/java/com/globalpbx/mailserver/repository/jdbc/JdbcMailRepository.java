package com.globalpbx.mailserver.repository.jdbc;

import com.globalpbx.mailserver.constant.MailsColumnName;
import com.globalpbx.mailserver.constant.TableNameConstants;
import com.globalpbx.mailserver.dto.MailInfoDto;
import com.globalpbx.mailserver.repository.MailServerRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcMailRepository implements MailServerRepository {
    private static final Logger logger = LogManager.getLogger(JdbcMailRepository.class);

    @Override
    public void createMailsTable(Connection connection) {
        String createTableSQL = "create table if not exists " + TableNameConstants.MAILS + "(\n" +
                "    id INTEGER PRIMARY KEY,\n" +
                "    path VARCHAR(255),\n" +
                "    version_number FLOAT,\n" +
                "    recipient  VARCHAR(255),\n" +
                "    subject VARCHAR(255),\n" +
                "    body TEXT,\n" +
                "    send_time TIMESTAMP\n" +
                ");";

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createTableSQL);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<MailInfoDto> getAllMails(Connection connection) {
        List<MailInfoDto> mailList = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM " + TableNameConstants.MAILS)) {
            while (resultSet.next()) {
                long id = resultSet.getLong(MailsColumnName.ID);
                String path = resultSet.getString(MailsColumnName.PATH);
                float versionNumber = resultSet.getFloat(MailsColumnName.VERSION_NUMBER);
                String recipient = resultSet.getString(MailsColumnName.RECIPIENT);
                String subject = resultSet.getString(MailsColumnName.SUBJECT);
                String body = resultSet.getString(MailsColumnName.BODY);
                LocalDateTime sendTime = LocalDateTime.parse(resultSet.getString(MailsColumnName.SEND_TIME));

                MailInfoDto mail = new MailInfoDto(id, path, versionNumber, recipient, subject, body,sendTime,false);
                mailList.add(mail);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        return mailList;
    }

    @Override
    public MailInfoDto saveMail(Connection connection, MailInfoDto mailInfoDto) throws SQLException {

        String insertQuery = "INSERT INTO "+ TableNameConstants.MAILS+ " (path, version_number, recipient, subject, body,send_time)\n" +
                "                VALUES (?,?,?,?,?,?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);

        preparedStatement.setString(1, mailInfoDto.getPath());
        preparedStatement.setFloat(2, mailInfoDto.getVersionNumber());
        preparedStatement.setString(3, mailInfoDto.getRecipient());
        preparedStatement.setString(4, mailInfoDto.getSubject());
        preparedStatement.setString(5, mailInfoDto.getBody());
        preparedStatement.setString(6, String.valueOf(LocalDateTime.now()));

        preparedStatement.executeUpdate();
        return mailInfoDto;
    }

}
