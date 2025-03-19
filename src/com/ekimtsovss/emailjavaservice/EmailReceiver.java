package com.ekimtsovss.emailjavaservice;

import javax.mail.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class EmailReceiver {
    private  final int numberOfEmails=20;
    private Message[] messages= new Message[numberOfEmails];
    private  List<String> emailList = new ArrayList<>();

    public Message [] getEmails(String username, String password) {
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "imaps");
        properties.put("mail.imaps.host", "imap.gmail.com");
        properties.put("mail.imaps.port", "993");
        properties.put("mail.imaps.ssl.enable", "true");
        properties.put("mail.imaps.socketFactory.fallback","false");

        try {
            Session emailSession = Session.getDefaultInstance(properties);
            Store store = emailSession.getStore("imaps");
            store.connect("imap.gmail.com", username, password);
            Folder emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);

            var allMessages = emailFolder.getMessages();
            System.out.println("Number of emails: " + allMessages.length);

            //Fetching emails which are defined variable numberOfEmails
            for (int i=allMessages.length-1;i>(allMessages.length-1-numberOfEmails);i--){
                messages[allMessages.length-1-i] = allMessages[i];
            }

            System.out.println(Arrays.toString(messages));
            emailFolder.close(false);
            store.close();

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return messages;
    }

}

