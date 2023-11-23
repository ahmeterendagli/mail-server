package com.globalpbx.mailserver.repository;

import com.globalpbx.mailserver.dto.MailInfoDto;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Repository
public interface MailServerRepository {
    void createMailsTable(Connection connection);
    List<MailInfoDto> getAllMails(Connection connection);
    MailInfoDto saveMail(Connection connection,MailInfoDto mailInfoDto) throws SQLException;
}
