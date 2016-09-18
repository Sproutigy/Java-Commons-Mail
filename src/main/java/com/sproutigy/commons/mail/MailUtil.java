package com.sproutigy.commons.mail;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Store;

public final class MailUtil {
    public static final String INBOX = "INBOX";

    private MailUtil() {
    }

    public static Folder[] folders(Store store) throws MessagingException {
        return store.getDefaultFolder().list("*");
    }

    public static Folder openInbox(Store store, boolean writeable) throws MessagingException {
        Folder inbox = store.getFolder(INBOX);
        if (!inbox.exists()) {
            inbox = store.getDefaultFolder();
            if (!inbox.exists() || !holdsMessages(inbox)) {
                for (Folder folder : folders(store)) {
                    if (holdsMessages(folder)) {
                        inbox = folder;
                        break;
                    }
                }
            }
        }

        if (inbox != null) {
            openFolder(inbox, writeable);
        }

        return inbox;
    }

    public static Folder openFolder(Folder folder, boolean writeable) throws MessagingException {
        folder.open(writeable ? Folder.READ_WRITE : Folder.READ_ONLY);
        return folder;
    }

    public static Folder createFolder(Folder parentFolder, String name, boolean holdMessages, boolean holdFolders) throws MessagingException {
        Folder folder = parentFolder.getFolder(name);
        if (!folder.exists()) {
            createFolder(folder, holdMessages, holdFolders);
        }
        return folder;
    }

    public static boolean createFolder(Folder folder, boolean holdMessages, boolean holdFolders) throws MessagingException {
        int type = 0;

        if (holdFolders && holdMessages) {
            type = Folder.HOLDS_FOLDERS & Folder.HOLDS_MESSAGES;
        } else if (holdFolders) {
            type = Folder.HOLDS_FOLDERS;
        } else if (holdMessages) {
            type = Folder.HOLDS_MESSAGES;
        }

        return folder.create(type);
    }

    public static boolean holdsFolders(Folder folder) throws MessagingException {
        return (folder.getType() & Folder.HOLDS_FOLDERS) == Folder.HOLDS_FOLDERS;
    }

    public static boolean holdsMessages(Folder folder) throws MessagingException {
        return (folder.getType() & Folder.HOLDS_MESSAGES) == Folder.HOLDS_MESSAGES;
    }
}
