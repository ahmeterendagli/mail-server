package com.globalpbx.mailserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.globalpbx.mailserver.dto.MailInfoDto;

import java.util.List;

public interface MailServerService {

    String sendMail(List<MailInfoDto> mailInfoDtoList) throws JsonProcessingException;

}
