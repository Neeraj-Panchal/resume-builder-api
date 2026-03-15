package com.neeraj.resumebuilderapi.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    @Value("${spring.mail.properties.mail.smtp.from}")
     private String fromEmail;

     private final JavaMailSender mailSender;

     //MIME = MULTUPURPOSE INTERNET MAIL EXTENSION (used as an envelope to get the message inside it,rather then sending only text ,we will send the html,images and attachements by using the mimeMessage)
    //it is a object which holds the email data (to,from, subject,body)
     public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
         try {
             log.info("Inside EmailService sendHtmlEmail: {}, {}, {}", to, subject, htmlContent);
             MimeMessage message = mailSender.createMimeMessage();
             MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
             helper.setFrom(fromEmail);
             helper.setTo(to);
             helper.setSubject(subject);
             helper.setText(htmlContent, true);
             mailSender.send(message);
         } catch (Exception e) {
             log.error("💥 SMTP ERROR: ", e); // Ye line zaroori hai actual error janne ke liye
             throw e;
         }
     }

     public void sendEmailWithAttachment(String to, String subject, String body,byte[] attachment, String filename) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body);
        helper.addAttachment(filename, new ByteArrayResource(attachment));
        mailSender.send(message);
     }

    public void sendSimpleEmail(String to, String subject, String text) throws MessagingException {
         try{
             SimpleMailMessage message = new SimpleMailMessage();
             message.setTo(to);
             message.setFrom(fromEmail);
             message.setSubject(subject);
             message.setText(text);

             mailSender.send(message);
             log.info("Contact us mail send successfully to : "+ to);
         }catch (Exception e){
             log.info("Contact us mail send failed to : "+ to);
             System.out.println(e.getMessage());

         }

    }
}
