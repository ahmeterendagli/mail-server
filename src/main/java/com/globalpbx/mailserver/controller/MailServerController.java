package com.globalpbx.mailserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.globalpbx.mailserver.dto.MailInfoDto;
import com.globalpbx.mailserver.dto.MailServerInfoDto;
import com.globalpbx.mailserver.service.MailServerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/mail/")
@CrossOrigin
public class MailServerController {

    private MailServerService mailServerService;

    public MailServerController(MailServerService mailServerService) {
        this.mailServerService = mailServerService;
    }

    @PostMapping
    public ResponseEntity<String> sendMail(@RequestBody List<MailInfoDto> mailInfoDtoList) {
        return new ResponseEntity<>(mailServerService.sendMail(mailInfoDtoList), HttpStatus.OK);
    }

    @PostMapping("server")
    public ResponseEntity<String> addMailServer(@RequestBody List<MailServerInfoDto> mailServerInfoDtos) throws ClassNotFoundException, SQLException {
        return new ResponseEntity<>(mailServerService.addMailServer(mailServerInfoDtos),HttpStatus.CREATED);
    }

    @PostMapping("admin/server")
    public ResponseEntity<String> createAdminMailServer(@RequestBody MailServerInfoDto mailServerInfoDto) throws SQLException, ClassNotFoundException {
        return new ResponseEntity<>(mailServerService.createAdminMailServer(mailServerInfoDto),HttpStatus.CREATED);
    }

}
