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
    private String path;
    private float versionNumber;
    private String recipient;
    private String subject;
    private String body;
    private LocalDateTime sendTime;
    private Boolean isHtml;

    @Override
    public String toString() {
        return "MailInfoDto{" +
                "id=" + id +
                ", path='" + path + '\'' +
                ", versionNumber=" + versionNumber +
                ", recipient='" + recipient + '\'' +
                ", subject='" + subject + '\'' +
                ", body='" + body + '\'' +
                ", sendTime=" + sendTime +
                '}';
    }
}
