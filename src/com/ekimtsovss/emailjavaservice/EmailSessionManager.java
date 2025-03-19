package com.ekimtsovss.emailjavaservice;

import javax.mail.*;
import java.util.*;

public class EmailSessionManager {
    private Store store;
    private Folder emailFolder;
    private static EmailSessionManager instance;
    private String name;
    private String password;
//    private  final int numberOfEmails=1000;
    private Message[] messages;
    public EmailSessionManager( String name, String password) throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "imaps");
        properties.put("mail.imaps.host", "imap.gmail.com");
        properties.put("mail.imaps.port", "993");
        properties.put("mail.imaps.ssl.enable", "true");
        Session session = Session.getDefaultInstance(properties);
        this.store = session.getStore("imaps");
        this.store.connect(name, password);
        this.name = name;
        this.password = password;
    }
    public EmailSessionManager getInstance(){
        if (instance==null)
            throw new IllegalStateException("Email is not initialized/" +
                    "Please login");
        return instance;
    }
    public EmailSessionManager getInstance(String username, String password) throws MessagingException {
        if (instance==null)
            instance = new EmailSessionManager(username, password);
        return instance;
    }
    public String getName() {
        return name;
    }
    public String getPassword() {
        return password;
    }
    public Message[] getMessages() {
        return messages;
    }



    public Message[] refreshEmails() { //Fetching all emails
        try {
            if (emailFolder==null){
                emailFolder = store.getFolder("INBOX");
                emailFolder.open(Folder.READ_ONLY);
            }
            messages = emailFolder.getMessages();
            System.out.println(Arrays.toString(messages));
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return messages;
    }
    public void closeSession() throws MessagingException {
        if (emailFolder!=null){
            emailFolder.close();
            emailFolder=null;
        }
        if (store!=null){
            store.close();
            store=null;
        }
        instance=null;
        name = "";
        password = "";
    }
}
