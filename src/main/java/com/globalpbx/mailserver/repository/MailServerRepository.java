package com.globalpbx.mailserver.repository;

import com.globalpbx.mailserver.constant.MailsColumnName;
import com.globalpbx.mailserver.constant.TableNameConstants;
import com.globalpbx.mailserver.constant.VersionsColumnName;
import com.globalpbx.mailserver.dto.MailInfoDto;
import com.globalpbx.mailserver.dto.MailServerInfoDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MailServerRepository {
    private static final Logger logger = LogManager.getLogger(MailServerRepository.class);

    public void createMailsTable(Connection connection) {
        String createTableSQL = "create table if not exists " + TableNameConstants.MAILS + "(\n" +
                "    id INTEGER PRIMARY KEY,\n" +
                "    version_number FLOAT,\n" +
                "    recipient  VARCHAR(255),\n" +
                "    subject VARCHAR(255),\n" +
                "    body TEXT,\n" +
                "    send_time TIMESTAMP,\n" +
                "    mail_address VARCHAR(255),\n" +
                "    smtp_server_address VARCHAR(255),\n" +
                "    smtp_server_port VARCHAR(10),\n" +
                "    security_layer VARCHAR(50)\n" +
                ");";

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createTableSQL);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void createMailServersTable(Connection connection) {

        String createTableSQL = "create table if not exists " + TableNameConstants.MAIL_SERVERS + "(\n" +
                "    id INTEGER PRIMARY KEY,\n" +
                "    mail_address VARCHAR(255),\n" +
                "    mail_password VARCHAR(255),\n" +
                "    smtp_server_address VARCHAR(255),\n" +
                "    smtp_server_port VARCHAR(10),\n" +
                "    security_layer VARCHAR(50),\n" +
                "    is_active BOOLEAN\n" +
                ");";

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createTableSQL);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void createAdminMailServerTable(Connection connection) {

        String createTableSQL = "create table if not exists " + TableNameConstants.ADMIN_MAIL_SERVER + "(\n" +
                "    id INTEGER PRIMARY KEY,\n" +
                "    mail_address VARCHAR(255),\n" +
                "    mail_password VARCHAR(255),\n" +
                "    smtp_server_address VARCHAR(255),\n" +
                "    smtp_server_port VARCHAR(10),\n" +
                "    security_layer VARCHAR(50),\n" +
                "    is_active BOOLEAN\n" +
                ");";

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createTableSQL);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }


    public MailInfoDto saveMail(Connection connection, MailInfoDto mailInfoDto) throws SQLException {

        String insertQuery = "INSERT INTO "+ TableNameConstants.MAILS+ " (version_number, recipient, subject, body,send_time,mail_address,smtp_server_address, smtp_server_port,security_layer)\n" +
                "                VALUES (?,?,?,?,?,?,?,?,?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);

        preparedStatement.setFloat(1, mailInfoDto.getVersionNumber());
        preparedStatement.setString(2, mailInfoDto.getRecipient());
        preparedStatement.setString(3, mailInfoDto.getSubject());
        preparedStatement.setString(4, mailInfoDto.getBody());
        preparedStatement.setString(5, String.valueOf(LocalDateTime.now()));
        preparedStatement.setString(6, mailInfoDto.getMailAddress());
        preparedStatement.setString(7, mailInfoDto.getSmtpServerAddress());
        preparedStatement.setString(8, mailInfoDto.getSmtpServerPort());
        preparedStatement.setString(9, mailInfoDto.getSecurityLayer());

        preparedStatement.executeUpdate();
        return mailInfoDto;
    }

    public MailServerInfoDto saveMailServer(Connection connection, MailServerInfoDto mailServerInfoDto) throws SQLException {
        String insertQuery = "INSERT INTO "+ TableNameConstants.MAIL_SERVERS+ " (mail_address," +
                " mail_password, smtp_server_address," +
                " smtp_server_port, security_layer, is_active)\n" +
                "                VALUES (?,?,?,?,?,?)";

        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);

        preparedStatement.setString(1, mailServerInfoDto.getMailAddress());
        preparedStatement.setString(2, mailServerInfoDto.getMailPassword());
        preparedStatement.setString(3, mailServerInfoDto.getSmtpServerAddress());
        preparedStatement.setString(4, mailServerInfoDto.getSmtpServerPort());
        preparedStatement.setString(5, mailServerInfoDto.getSecurityLayer());
        preparedStatement.setBoolean(6, mailServerInfoDto.getIsActive());

        preparedStatement.executeUpdate();
        return mailServerInfoDto;
    }

    public MailServerInfoDto saveAdminMailServer(Connection connection, MailServerInfoDto mailServerInfoDto) throws SQLException {
        String insertQuery = "INSERT INTO "+ TableNameConstants.ADMIN_MAIL_SERVER+ " (mail_address," +
                " mail_password, smtp_server_address," +
                " smtp_server_port, security_layer, is_active)\n" +
                "                VALUES (?,?,?,?,?,?)";

        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);

        preparedStatement.setString(1, mailServerInfoDto.getMailAddress());
        preparedStatement.setString(2, mailServerInfoDto.getMailPassword());
        preparedStatement.setString(3, mailServerInfoDto.getSmtpServerAddress());
        preparedStatement.setString(4, mailServerInfoDto.getSmtpServerPort());
        preparedStatement.setString(5, mailServerInfoDto.getSecurityLayer());
        preparedStatement.setBoolean(6, true);

        preparedStatement.executeUpdate();
        return mailServerInfoDto;
    }

    public void checkAndMakePassiveAdminMailServer(Connection connection) throws SQLException {
        String checkDataQuery = "SELECT COUNT(*) AS count FROM " + TableNameConstants.ADMIN_MAIL_SERVER;
        PreparedStatement preparedStatement = connection.prepareStatement(checkDataQuery);
        ResultSet rs = preparedStatement.executeQuery();

        int count = 0;
        if (rs.next()) {
            count = rs.getInt("count");
        }
        preparedStatement.close();

        if (count > 0) {
            String updateQuery = "UPDATE " + TableNameConstants.ADMIN_MAIL_SERVER + " SET is_active = false WHERE id = (SELECT id FROM "+TableNameConstants.ADMIN_MAIL_SERVER+" ORDER BY id DESC LIMIT 1)";
            PreparedStatement preparedStatement1 = connection.prepareStatement(updateQuery);
            preparedStatement1.executeUpdate();
            System.out.println("The isActive field of the last data is updated to false.");
        } else {
            System.out.println("No data found in the table.");
        }
    }

    public MailInfoDto findActiveAdminMailServer(Connection connection) throws SQLException {
        String selectLastRowQuery = "SELECT * FROM " + TableNameConstants.ADMIN_MAIL_SERVER + " ORDER BY rowid DESC LIMIT 1";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectLastRowQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                long id = resultSet.getLong("id");
                String mailAddress = resultSet.getString("mail_address");
                String mailPassword = resultSet.getString("mail_password");
                String smtpServerAddress = resultSet.getString("smtp_server_address");
                String smtpServerPort = resultSet.getString("smtp_server_port");
                String securityLayer = resultSet.getString("security_layer");

                return new MailInfoDto(id, -1f,null,
                        null,null
                        ,null,false,mailAddress, mailPassword,
                        smtpServerAddress, smtpServerPort,securityLayer);
            }
        }
        return null;
    }
}
