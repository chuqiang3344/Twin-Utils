package com.tyaer.util.email;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Created by Twin on 2017/7/12.
 */
public class Test {
    public static void main(String[] args) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host","smtp.exmail.qq.com");
        //props.put("mail.smtp.starttls.enable","true");//使用 STARTTLS安全连接
        //props.put("mail.smtp.port", "25");             //google使用465或587端口
        props.put("mail.smtp.auth", "true");

        //props.put("mail.debug", "true");
        String from="data-platform-monitor@izhonghong.com";
        String fromUserPassword="Cf2ysjd";
        Session mailSession = Session.getInstance(props, new MyAuthenticator(from, fromUserPassword));

        // 第二步：编写消息


        InternetAddress fromAddress = new InternetAddress(from);


        MimeMessage message = new MimeMessage(mailSession);

        message.setFrom(fromAddress);

        // 第三步：发送消息
        Transport transport = mailSession.getTransport("smtp");
        //transport.connect(smtpHost,from, fromUserPassword);
//        transport.send(message, message.getRecipients(RecipientType.TO));
        transport.send(message, message.getAllRecipients());//全部收件人
        System.out.println("message yes");
    }
}
