package com.globalpbx.mailserver.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MailServerInfoDto {

    private long id;
    private String mailAddress;
    private String mailPassword;
    private String smtpServerAddress;
    private String smtpServerPort;
    private String securityLayer;
    private Boolean isActive;
}
