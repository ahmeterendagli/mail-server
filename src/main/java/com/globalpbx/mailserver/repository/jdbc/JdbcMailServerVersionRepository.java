package com.globalpbx.mailserver.repository.jdbc;

import com.globalpbx.mailserver.constant.TableNameConstants;
import com.globalpbx.mailserver.constant.VersionsColumnName;
import com.globalpbx.mailserver.dto.MailInfoDto;
import com.globalpbx.mailserver.repository.MailServerVersionRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcMailServerVersionRepository implements MailServerVersionRepository {
    private static final Logger logger = LogManager.getLogger(JdbcMailServerVersionRepository.class);
    @Override
    public void createVersionTable(Connection connection) {

        String createTableSQL = "create table if not exists  " + TableNameConstants.VERSIONS + " (id INTEGER PRIMARY KEY, version_number FLOAT)";

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createTableSQL);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getAllVersion(Connection connection) {

        List<String> versionList = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM " + TableNameConstants.VERSIONS)) {
            while (resultSet.next()) {
                float versionNumber = resultSet.getFloat(VersionsColumnName.VERSION_NUMBER);
                versionList.add(String.valueOf(versionNumber));
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return versionList;
    }

    @Override
    public String saveVersion(Connection connection, MailInfoDto mailInfoDto) throws SQLException {
        String selectQueryFromVersionTable = "SELECT * FROM " + TableNameConstants.VERSIONS + " WHERE version_number = ?";
        PreparedStatement preparedStatementSelect = connection.prepareStatement(selectQueryFromVersionTable);
        preparedStatementSelect.setString(1, String.valueOf(mailInfoDto.getVersionNumber()));

        ResultSet resultSet = preparedStatementSelect.executeQuery();

        if (resultSet.next()) {
            return "Version number already exists: " + mailInfoDto.getVersionNumber();
        } else {
            String insertQueryToVersionTable = "INSERT INTO " + TableNameConstants.VERSIONS + " (version_number) VALUES (?)";
            PreparedStatement preparedStatementVersionTable = connection.prepareStatement(insertQueryToVersionTable);

            preparedStatementVersionTable.setString(1, String.valueOf(mailInfoDto.getVersionNumber()));

            preparedStatementVersionTable.executeUpdate();
            return "Version added successfully! -> " + mailInfoDto.getVersionNumber();
        }
    }

    @Override
    public String findLastVersion(Connection connection) throws SQLException {
        String selectLastRowQuery = "SELECT * FROM " + TableNameConstants.VERSIONS + " ORDER BY rowid DESC LIMIT 1";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectLastRowQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getString(VersionsColumnName.VERSION_NUMBER);
            }
        }
        return String.valueOf(-1);
    }


}
