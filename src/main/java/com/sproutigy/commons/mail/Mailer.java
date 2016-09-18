package com.sproutigy.commons.mail;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;

public interface Mailer {
    Message prepare(MailConfig configuration) throws MessagingException;

    Message prepare(MailConfig configuration, String recipient) throws MessagingException;

    void send(Message message) throws MessagingException;

    Store store(MailConfig configuration) throws MessagingException;
}
