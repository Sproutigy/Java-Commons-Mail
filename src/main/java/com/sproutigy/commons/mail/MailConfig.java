package com.sproutigy.commons.mail;

public interface MailConfig {
    String getHost();

    Integer getPort();

    Protocol getProtocol();

    Encryption getEncryption();

    String getUsername();

    String getPassword();

    enum Encryption {
        None, TLS, SSL
    }

    enum Protocol {
        SMTP, POP3, IMAP
    }
}
