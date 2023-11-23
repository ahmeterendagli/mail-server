package com.globalpbx.mailserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.globalpbx.mailserver.dto.MailInfoDto;
import com.globalpbx.mailserver.service.MailServerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mail/")
public class MailServerController {

    private MailServerService mailServerService;

    public MailServerController(MailServerService mailServerService) {
        this.mailServerService = mailServerService;
    }

    @PostMapping
    public ResponseEntity<String> sendMail(@RequestBody List<MailInfoDto> mailInfoDtoList) throws JsonProcessingException {
        return new ResponseEntity<>(mailServerService.sendMail(mailInfoDtoList), HttpStatus.OK);
    }

}
