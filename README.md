# Java-Commons-Mail
Convenient mailing using JavaMail API

## About
Simplifies mailing (both sending and reading) by adding object-oriented configuration with unspecified properties autodetection capability (such as guessing encryption type from port number and vice versa) and utility to use JavaMail API functionality more straightforward.
What is more, it allows non-Java EE applications to use JavaMail API with default implementation by aggregating all required dependencies.

## Dependencies
- JavaMail API v1.5.6 (javax.mail-api)
- JavaMail implementation v1.5.6 (javax.mail)
- JavaBeans Activation Framework v1.1.1 (javax.activation)

## API

### Mailer

```java
public interface Mailer {
    Message prepare(MailConfig configuration) throws MessagingException;
    Message prepare(MailConfig configuration, String recipient) throws MessagingException;

    void send(Message message) throws MessagingException;

    Store store(MailConfig configuration) throws MessagingException;
}
```

### MailUtil
```java
public final class MailUtil {
    public static Folder[] folders(Store store) throws MessagingException;

    public static Folder openInbox(Store store, boolean writeable) throws MessagingException;
    public static Folder openFolder(Folder folder, boolean writeable) throws MessagingException;

    public static Folder createFolder(Folder parentFolder, String name, boolean holdMessages, boolean holdFolders) throws MessagingException;
    public static boolean createFolder(Folder folder, boolean holdMessages, boolean holdFolders) throws MessagingException;

    public static boolean holdsFolders(Folder folder) throws MessagingException;
    public static boolean holdsMessages(Folder folder) throws MessagingException;
}
```


## Examples

### Configuration

#### SMTP (for sending e-mail)
```java
    MailConfig smtpConfig = StandardMailConfig.builder()
        .host("smtp.test.net")
        .protocol(MailConfig.Protocol.SMTP)
        .encryption(MailConfig.Encryption.SSL)
        .username("test@sproutigy.com")
        .password("testpswd")
        .build();
        //default SSL port for SMTP: 465
```

#### POP3 (for receiving e-mails)
```java
    MailConfig pop3Config = StandardMailConfig.builder()
        .protocol(MailConfig.Protocol.POP3)
        .host("pop3.test.net")
        .encryption(MailConfig.Encryption.SSL)
        .username("test@sproutigy.com")
        .password("testpswd")
        .build();
        //default SSL port for POP3: 995
```

#### IMAP (for browsing folders and receiving e-mails)
```java
    MailConfig imapConfig = StandardMailConfig.builder()
        .protocol(MailConfig.Protocol.IMAP)
        .host("imap.test.net")
        .port(993)
        .username("test@sproutigy.com")
        .password("testpswd")
        .build();
        //default encryption for IMAP port 993 is SSL
```

### Sending e-mails
```java
    Mailer mailer = new DefaultMailer();
    Message msg = mailer.prepare(smtpConfig, "receiver@test.net");
    msg.setSubject("TEST MESSAGE");
    msg.setText("This is just a test message");
    mailer.send(msg);
```

### Browsing INBOX
```java
    Store store = mailer.store(imapConfig);
    Folder folder = MailUtil.openInbox(store, false);
    System.out.println(folder.getMessageCount());

    Message message = folder.getMessage(1); //fetch first message
    System.out.println(message.getSubject());

    store.close();
```


## Maven

To use as a dependency add to your `pom.xml` into `<dependencies>` section:
```xml
<dependency>
    <groupId>com.sproutigy.commons</groupId>
    <artifactId>mail</artifactId>
    <version>RELEASE</version>
</dependency>
```


## More
For more information and commercial support visit [Sproutigy](http://www.sproutigy.com/opensource)
