package com.ekimtsovss.emailjavaservice;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class EmailSender {
    public static void send(String to, String text, Account account, List<File> files) {

        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host","smtp.gMail.com");
        properties.setProperty("mail.smtp.auth", "true");
        //properties.put("mail.smtp.port", 587);
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
        properties.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(account.getName(), account.getPassword());
            }
        });
        MimeMessage message = new MimeMessage(session);
        try {
            message.addRecipient(Message.RecipientType.TO,new InternetAddress(to));
            Multipart multipart = new MimeMultipart();
            MimeBodyPart textPart = new MimeBodyPart();

            textPart.setText(text);
            multipart.addBodyPart(textPart);
            MimeBodyPart attachmentPart = new MimeBodyPart();
            for (File file:files) {
                attachmentPart.attachFile(file);
                multipart.addBodyPart(attachmentPart);
            }
            message.setContent(multipart);
            Transport.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
