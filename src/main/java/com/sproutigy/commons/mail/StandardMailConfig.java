package com.sproutigy.commons.mail;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StandardMailConfig implements MailConfig {
    private String host;
    private Integer port;
    private Protocol protocol;
    private Encryption encryption;
    private String username;
    private String password;
}
