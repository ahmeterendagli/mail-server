package com.globalpbx.mailserver.repository;

import com.globalpbx.mailserver.dto.MailInfoDto;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Repository
public interface MailServerVersionRepository {
    void createVersionTable(Connection connection);
    List<String> getAllVersion(Connection connection);
    String saveVersion(Connection connection, MailInfoDto mailInfoDto) throws SQLException;
    String findLastVersion(Connection connection) throws SQLException;
}
