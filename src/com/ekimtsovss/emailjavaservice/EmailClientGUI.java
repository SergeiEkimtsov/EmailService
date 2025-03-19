package com.ekimtsovss.emailjavaservice;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

import java.util.*;
import java.util.List;

public class EmailClientGUI extends JFrame {
    private final static Account account = new Account();
    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField= new JPasswordField(20);
    private final JTextArea emailContent = new JTextArea("Email content");
    private final JTextField amountEmailsField = new JTextField("Amount loaded emails");
    private final DefaultListModel<String> emailListModel = new DefaultListModel<>();
    private int amountEmails = 0;

    private EmailSessionManager emailSessionManager;

    private Message [] messages;
    volatile boolean ready = false;
    int emailCounter = 0;
    private String textContent = "";
    private static final Color BACKGROUND_COLOR = new Color(200, 200, 200);
    private static final Color BACKGROUND_COLOR_PANEL = new Color(200, 220, 240);
    private static final Color BACKGROUND_COLOR_BUTTON = new Color(165, 172, 178);
    private static final Font FONT_CONTENT_REPLY= new Font("SansSerif", Font.ITALIC, 14);

    public EmailClientGUI() throws MessagingException {
        login();
        initUI();
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {

                System.out.println(emailSessionManager);

                    try {
                        if(emailSessionManager!=null)
                            emailSessionManager.closeSession();
                    } catch (MessagingException ex) {
                        throw new RuntimeException(ex);
                    }
                }
        });

    }

    private void initUI() {

        setTitle("Java Email Client");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setOneTouchExpandable(true);


        JList<String> emailList = new JList<>(emailListModel);
        emailListModel.add(0,"List of emails");
        emailList.setBackground(BACKGROUND_COLOR);
        emailList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        emailList.setFont(FONT_CONTENT_REPLY);

        JScrollPane listScrollPane = new JScrollPane(emailList);
        listScrollPane.setBackground(BACKGROUND_COLOR);

        emailContent.setEditable(false);
        emailContent.setBackground(BACKGROUND_COLOR);
        emailContent.setFont(FONT_CONTENT_REPLY);

        JScrollPane contentScrollPane = new JScrollPane(emailContent);

        splitPane.setLeftComponent(listScrollPane);
        splitPane.setRightComponent(contentScrollPane);

        getContentPane().setBackground(BACKGROUND_COLOR);
        getContentPane().add(splitPane, BorderLayout.CENTER);

        emailList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount()==2){
                    emailContent.setText("");
                    var indexMessage = emailList.getSelectedIndex();
                    System.out.println("list index: "+indexMessage);
                    try {
                        String date = messages[messages.length-1-indexMessage].getSentDate().toString();
                        String subject = messages[messages.length-1-indexMessage].getSubject();
                        String from = InternetAddress.toString(messages[messages.length-1-indexMessage].getFrom());
                        String content = getContentFromEmail(messages[messages.length-1-indexMessage]);

                        var handledContent = handlerContent(content);

                        emailContent.append("Date: "+date+"\n"+"Subject: "+subject+"\n"
                                +"From: " + from+"\n"+"\n"
                                +"Content: "+handledContent);

                    } catch (MessagingException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(BACKGROUND_COLOR_PANEL);

        JButton getAllEmailsButton = new JButton("show emails");
        getAllEmailsButton.setBackground(BACKGROUND_COLOR_BUTTON);
        getAllEmailsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                emailListModel.clear();
                System.out.println("list "+emailListModel);
                emailListModel.add(0,
                        "Loading all emails will take around "+ messages.length / 300 +" minutes");
                //messages = emailSessionManager.refreshEmails();
                System.out.println("list "+emailListModel);
                amountEmailsField.setText(messages.length +" loaded emails");

                System.out.println("Am "+amountEmails);

                emailCounter=0;
                ready = false;
                emailListModel.clear();
               showProgress();


                for (int i = messages.length-1; i >= messages.length-amountEmails; i--){

                    try {
                        emailListModel.add(emailCounter,messages[i].getSentDate()+"-"+messages[i].getSubject()
                                +" From: " + Arrays.toString(messages[i].getFrom()));
                    }
                    catch (MessagingException ex) {
                        throw new RuntimeException(ex);
                    }

//                    var end = LocalTime.now();
//                    var diff = ChronoUnit.MILLIS.between(start, end);
//                    System.out.println(diff);

                    emailCounter++;
                    System.out.println("Q "+emailCounter);

                }
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                ready = true;
            }
        });


        JButton replyButton = new JButton("Reply");
        replyButton.setBackground(BACKGROUND_COLOR_BUTTON);
        replyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (emailList.getSelectedIndex()!=-1) {
                    try {
                        composeReply(messages[messages.length-1-emailList.getSelectedIndex()]);
                    } catch (MessagingException ex) {
                        throw new RuntimeException(ex);
                    }
                    System.out.println(emailList.getSelectedIndex());
                }
            }
        });

        JButton clearButton = new JButton("clear");
        clearButton.setBackground(BACKGROUND_COLOR_BUTTON);
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                emailListModel.clear();
                emailListModel.add(0,"List of emails");
                emailContent.setText("Email content");
            }
        });
        Integer [] array = new Integer[messages.length];
        for (int i=1;i<messages.length;i++){
            array[i-1] = i;

        }


        JComboBox<Integer> listNumbers = new JComboBox<>(array); //pop up list of numbers
        bottomPanel.add(listNumbers);
        bottomPanel.add(getAllEmailsButton);
        bottomPanel.add(replyButton);
        bottomPanel.add(clearButton);
        add(bottomPanel, BorderLayout.SOUTH);
        add(amountEmailsField,BorderLayout.NORTH);

        listNumbers.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                amountEmails = listNumbers.getSelectedIndex()+1;
            }
        });
    }

    private String handlerContent(String content) {
       // content = content.replaceAll("\\s+"," ");
        content = content.replaceAll("'�'+","");

        var arrayContent = content.split("\\s");
        StringBuilder sbContent = new StringBuilder();
        for (int i=0;i<arrayContent.length;i++){
            if (i%11==0)
                sbContent.append(arrayContent[i]).append("\n");
            else sbContent.append(arrayContent[i]).append(" ");
        }
        return sbContent.toString();
    }
    private void composeReply(Message message) throws MessagingException {
        List<File> fileList = new ArrayList<>();
        JFrame frameCompose = new JFrame("Reply Form");
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR_PANEL);

        JLabel toLabel = new JLabel("To");
        JLabel fromLabel = new JLabel("From");
        JLabel textLabel = new JLabel("To");

        JTextField toField = new JTextField();
        JTextField fromField = new JTextField();
        JTextField textField = new JTextField();

        fromField.setEditable(false);
        toField.setFont(FONT_CONTENT_REPLY);
        textField.setFont(FONT_CONTENT_REPLY);

        var to = Arrays.toString(message.getFrom());
        var arrayTo = to.substring(to.indexOf('<') + 1, to.indexOf('>'));

        if (arrayTo.contains("reply"))
            toField.setText("Email has status 'NoReply'");
        else toField.setText(arrayTo);

        fromField.setText(account.getName());
        textField.setText(textContent);

        JButton sendButton = new JButton("Send");
        sendButton.setBackground(BACKGROUND_COLOR_BUTTON);
        JButton attachButton = new JButton("Attach");
        attachButton.setBackground(BACKGROUND_COLOR_BUTTON);
        JButton backButton = new JButton("Back");
        backButton.setBackground(BACKGROUND_COLOR_BUTTON);

        panel.add(toLabel);
        panel.add(toField);
        panel.add(fromLabel);
        panel.add(fromField);
        panel.add(textLabel);
        panel.add(textField);
        panel.add(sendButton);
        panel.add(attachButton);
        panel.add(backButton);

        frameCompose.add(panel);
        frameCompose.setSize(600, 400);
        frameCompose.setVisible(true);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    emailSessionManager.closeSession();
                } catch (MessagingException ex) {
                    throw new RuntimeException(ex);
                }
                EmailSender.send(arrayTo, textField.getText(), account, fileList);
                frameCompose.setVisible(false);
            }
        });
        attachButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileList.clear();
                File[] attachFiles = AttachmentChooser.chooseAttachment();
                System.out.println(Arrays.toString(attachFiles));
                fileList.addAll(List.of(attachFiles));
            }
        });
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textContent = textField.getText();
                frameCompose.setVisible(false);
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                textContent = textField.getText();
            }
        });
    }
    private String getContentFromEmail(Message message) throws MessagingException, IOException {

        if (message.isMimeType("text/plain")) {
            System.out.println("Email is plain text");
            return (String) message.getContent();
        }
       else if (message.isMimeType("text/html")) {
            System.out.println("Email is html text");
            Document document = Jsoup.parse(message.getContent().toString());
            System.out.println(document.text());
            return  document.text();
        }
        else if (message.isMimeType("multipart/*")) {
            System.out.println("Email is multipart");
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            for (int i=0; i<mimeMultipart.getCount();i++){
                BodyPart bodyPart = mimeMultipart.getBodyPart(i);
                if (bodyPart.isMimeType("text/plain"))
                    return (String) bodyPart.getContent();
            }
        }
        return "Content has no text";
    }
    public void login()  {

        JPanel panel = new JPanel(new GridLayout(0,1));
        panel.add(new JLabel("Email"));
        panel.add(this.usernameField);
        panel.add(new JLabel("Password"));
        panel.add(this.passwordField);

        var result = JOptionPane.showConfirmDialog(null,panel,"Login",JOptionPane.OK_CANCEL_OPTION);

        if (result==JOptionPane.OK_OPTION && usernameField.getText().equals("")){
            System.out.println(account.getName());
            System.out.println(account.getPassword());
            try {
                if (emailSessionManager==null)
                    emailSessionManager = new EmailSessionManager(account.getName(), account.getPassword());
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        }
        else if (result==JOptionPane.OK_OPTION && !usernameField.getText().equals("")){

            try {
                emailSessionManager = new EmailSessionManager(usernameField.getText(),
                        Arrays.toString(passwordField.getPassword()));
            } catch (MessagingException e) {

                JOptionPane.showMessageDialog(null,"AuthenticationFailedException: [ALERT] Invalid credentials (Failure)");
                login();
                throw new RuntimeException(e);
            }
        }
        messages = emailSessionManager.refreshEmails();
        amountEmailsField.setText("["+messages.length + "] emails in the INBOX."
                + " Loading all emails will take around " + messages.length / 300 + " minutes");

//
//        int amountEmails; //variable defines amount of emails will be display
//        if (messages.length>20) //if amount of emails greater 20 it will be display 20 emails
//            amountEmails = 2;
//        else amountEmails = messages.length;
//        amountEmailsField.setText("List with last "+amountEmails +" loaded emails."
//                +" Summary:"+ messages.length + " emails in box."
//                +" Loading all emails will take around "+ messages.length / 300 +" minutes");
//
//        showProgress(amountEmails); //method launches progress bar during extracting emails
//        ready = false;
//
//        for (int i = messages.length-1; i > messages.length-1-amountEmails; i--){
//            try {
//                emailListModel.add(emailCounter,messages[i].getSentDate()+"-"+messages[i].getSubject()
//                        +" From: " + Arrays.toString(messages[i].getFrom()));
//            } catch (MessagingException e) {
//                throw new RuntimeException(e);
//            }
//            emailCounter++;
//        }
//        ready = true;

    //    JPanel panelNorth = new JPanel();
       // panelNorth.setLayout(new BoxLayout(panelNorth,1));

  //      JButton getButton = new JButton("get");
  //      panelNorth.add(getButton);
//        Integer [] array = new Integer[messages.length];
//        for (int i=1;i<messages.length;i++){
//            array[i-1] = i;
//
//        }
//        JComboBox<Integer> listNumbers = new JComboBox<>(array);
     //   panelNorth.add(listNumbers);

   //     add(panelNorth,BorderLayout.SOUTH);
    }
    public void showProgress(){

        Thread thread = new Thread(() -> {
            JFrame frame = new JFrame("Loading");
            JProgressBar progressBar = new JProgressBar();
            JPanel panel = new JPanel();

            progressBar.setIndeterminate(true);
            progressBar.setVisible(true);
            panel.add(progressBar);
            frame.add(panel);
            frame.setSize(300,100);
            frame.setVisible(true);

            while (!ready) {
                System.out.println("! "+emailCounter);
            }
            frame.setVisible(false);
        });
        thread.start();
        ready = false;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new EmailClientGUI();
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}