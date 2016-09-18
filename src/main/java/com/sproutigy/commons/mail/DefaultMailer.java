package com.sproutigy.commons.mail;

import lombok.Getter;
import lombok.Setter;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.PrintStream;
import java.util.Properties;

public class DefaultMailer implements Mailer {

    public static final int DEFAULT_SMTP_PORT = 25;
    public static final int DEFAULT_SMTP_TLS_PORT = 587;
    public static final int DEFAULT_SMTP_SSL_PORT = 465;

    public static final int DEFAULT_POP3_PORT = 110;
    public static final int DEFAULT_POP3_SSL_PORT = 995;

    public static final int DEFAULT_IMAP_PORT = 143;
    public static final int DEFAULT_IMAP_SSL_PORT = 993;

    @Getter
    @Setter
    private Properties defaultProperties = (Properties) System.getProperties().clone();

    @Getter
    @Setter
    private boolean debug = false;

    @Getter
    @Setter
    private PrintStream debugOut = System.out;

    public static boolean isAuthenticated(MailConfig configuration) {
        return (configuration.getUsername() != null && !configuration.getUsername().isEmpty());
    }

    protected Properties prepareRecvProperties(MailConfig configuration) {
        if (configuration.getProtocol() != null && configuration.getProtocol() == MailConfig.Protocol.SMTP) {
            throw new IllegalArgumentException("Invalid configuration - protocol " + configuration.getProtocol() + " not supported for receiving e-mails");
        }

        Properties props = (Properties) defaultProperties.clone();

        Integer port = configuration.getPort();
        MailConfig.Encryption encryption = configuration.getEncryption();
        MailConfig.Protocol protocol = configuration.getProtocol();

        if (protocol == null) {
            if (port != null) {
                if (port == DEFAULT_POP3_PORT || port == DEFAULT_POP3_SSL_PORT) {
                    protocol = MailConfig.Protocol.POP3;
                }
                if (port == DEFAULT_IMAP_PORT || port == DEFAULT_IMAP_SSL_PORT) {
                    protocol = MailConfig.Protocol.IMAP;
                }
            }

            if (protocol == null) {
                throw new IllegalArgumentException("Invalid configuration - cannot infer protocol");
            }
        }

        if (port == null && encryption != null) {
            if (encryption == MailConfig.Encryption.SSL) {
                if (protocol == MailConfig.Protocol.POP3) {
                    port = DEFAULT_POP3_SSL_PORT;
                } else {
                    port = DEFAULT_IMAP_SSL_PORT;
                }
            }
        }

        if (encryption == null && port != null) {
            if (port == DEFAULT_POP3_SSL_PORT || port == DEFAULT_IMAP_SSL_PORT) {
                encryption = MailConfig.Encryption.SSL;
            } else {
                encryption = MailConfig.Encryption.TLS;
            }
        }

        if (port == null) {
            if (protocol == MailConfig.Protocol.POP3) {
                port = DEFAULT_POP3_PORT;
            } else if (protocol == MailConfig.Protocol.IMAP) {
                port = DEFAULT_IMAP_PORT;
            }

            if (port == null) {
                throw new IllegalArgumentException("Invalid configuration - cannot infer default port from protocol " + configuration.getProtocol());
            }
        }

        String protocolStr = configuration.getProtocol().toString().toLowerCase(); // "pop3" || "smtp"

        if (encryption == MailConfig.Encryption.TLS) {
            props.put("mail." + protocolStr + ".starttls.enable", "true");
        }
        if (encryption == MailConfig.Encryption.SSL) {
            props.put("mail." + protocolStr + ".ssl.enable", "true");
        }

        props.put("mail." + protocolStr + ".host", configuration.getHost());
        props.put("mail." + protocolStr + ".port", port);

        props.put("mail.store.protocol", protocolStr);

        return props;

    }

    protected Properties prepareSendProperties(MailConfig configuration) {
        if (configuration.getProtocol() != null && configuration.getProtocol() != MailConfig.Protocol.SMTP) {
            throw new IllegalArgumentException("Invalid configuration - protocol " + configuration.getProtocol() + " not supported for sending e-mails");
        }

        Properties props = (Properties) defaultProperties.clone();

        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", configuration.getHost());

        if (isAuthenticated(configuration)) {
            props.put("mail.smtp.auth", "true");
        }

        Integer port = configuration.getPort();
        MailConfig.Encryption encryption = configuration.getEncryption();

        if (encryption == null && port != null) {
            if (port == DEFAULT_SMTP_SSL_PORT) {
                encryption = MailConfig.Encryption.SSL;
            } else if (port == DEFAULT_SMTP_TLS_PORT) {
                encryption = MailConfig.Encryption.TLS;
            } else {
                encryption = MailConfig.Encryption.None;
            }
        }

        if (encryption == MailConfig.Encryption.TLS) {
            props.put("mail.smtp.starttls.enable", "true");
            if (port == null) {
                port = DEFAULT_SMTP_TLS_PORT;
            }
        }
        if (encryption == MailConfig.Encryption.SSL) {
            props.put("mail.smtp.ssl.enable", "true");
            if (port == null) {
                port = DEFAULT_SMTP_SSL_PORT;
            }
        }

        if (port == null) {
            port = DEFAULT_SMTP_PORT;
        }

        props.put("mail.smtp.port", port);

        props.put("mail.smtp.debug", isDebug());

        return props;
    }

    @Override
    public Message prepare(final MailConfig configuration) throws MessagingException {
        Properties props = prepareSendProperties(configuration);
        Session session;
        if (isAuthenticated(configuration)) {
            session = Session.getDefaultInstance(props,
                    new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(configuration.getUsername(), configuration.getPassword());
                        }
                    });
        } else {
            session = Session.getDefaultInstance(props);
        }

        setupSession(session);

        Message message = new MimeMessage(session);
        if (isAuthenticated(configuration)) {
            message.setFrom(new InternetAddress(configuration.getUsername()));
        }
        return message;
    }

    @Override
    public Message prepare(MailConfig configuration, String recipient) throws MessagingException {
        Message message = prepare(configuration);
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient, false));
        return message;
    }

    @Override
    public void send(Message message) throws MessagingException {
        Transport.send(message);
    }

    @Override
    public Store store(MailConfig configuration) throws MessagingException {
        Properties props = prepareRecvProperties(configuration);

        Session session = Session.getDefaultInstance(props);
        setupSession(session);

        String protocolStr = props.get("mail.store.protocol").toString();

        try {
            Store store = session.getStore(protocolStr);
            if (!store.isConnected()) {
                if (isAuthenticated(configuration)) {
                    store.connect(configuration.getUsername(), configuration.getPassword());
                } else {
                    store.connect();
                }
            }
            return store;
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    protected void setupSession(Session session) {
        session.setDebug(isDebug());
        session.setDebugOut(getDebugOut());
    }
}
