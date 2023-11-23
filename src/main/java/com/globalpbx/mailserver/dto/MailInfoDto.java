package com.globalpbx.mailserver.dto;

import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MailInfoDto{
    private long id;
    private float versionNumber;
    private String recipient;
    private String subject;
    private String body;
    private LocalDateTime sendTime;
    private Boolean isHtml;
    private String mailAddress;
    private String mailPassword;
    private String smtpServerAddress;
    private String smtpServerPort;
    private String securityLayer;

    @Override
    public String toString() {
        return "MailInfoDto{" +
                "id=" + id +
                ", versionNumber=" + versionNumber +
                ", recipient='" + recipient + '\'' +
                ", subject='" + subject + '\'' +
                ", body='" + body + '\'' +
                ", sendTime=" + sendTime +
                ", isHtml=" + isHtml +
                ", mailAddress='" + mailAddress + '\'' +
                ", mailPassword='" + mailPassword + '\'' +
                ", smtpServerAddress='" + smtpServerAddress + '\'' +
                ", smtpServerPort='" + smtpServerPort + '\'' +
                ", securityLayer='" + securityLayer + '\'' +
                '}';
    }
}
