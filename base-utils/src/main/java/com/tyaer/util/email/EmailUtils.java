package com.tyaer.util.email;


import org.apache.commons.lang3.StringUtils;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

public class EmailUtils {
    @SuppressWarnings("static-access")
    public static void sendMessage(String smtpHost, String from,
                                   String fromUserPassword, List<String> tos, List<String> ccs, String subject,
                                   String messageText, String messageType) throws MessagingException {
        // 第一步：配置javax.mail.Session对象  
        System.out.println("为" + smtpHost + "配置mail session对象");

        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        //props.put("mail.smtp.starttls.enable","true");//使用 STARTTLS安全连接  
        //props.put("mail.smtp.port", "25");             //google使用465或587端口  
        props.put("mail.smtp.auth", "true");

        //props.put("mail.debug", "true");  
        Session mailSession = Session.getInstance(props, new MyAuthenticator(from, fromUserPassword));

        // 第二步：编写消息  


        InternetAddress fromAddress = new InternetAddress(from);


        MimeMessage message = new MimeMessage(mailSession);

        message.setFrom(fromAddress);
        for (String to : tos) {
            InternetAddress toAddress = new InternetAddress(to);
            message.setRecipient(RecipientType.TO, toAddress);
        }
        if (null != ccs) {
            for (String cc : ccs) {
                if (StringUtils.isNotBlank(cc)) {
                    InternetAddress ccAddress = new InternetAddress(cc);
//                    message.addRecipient(RecipientType.CC, ccAddress);
                    message.setRecipient(RecipientType.CC, ccAddress);
                }
            }
        }


        message.setSentDate(Calendar.getInstance().getTime());
        message.setSubject(subject);
        message.setContent(messageText, messageType);

        // 第三步：发送消息
        Transport transport = mailSession.getTransport("smtp");
        //transport.connect(smtpHost,from, fromUserPassword);
//        transport.send(message, message.getRecipients(RecipientType.TO));
        transport.send(message, message.getAllRecipients());//全部收件人
        System.out.println("message yes");
    }


    public static StringBuilder buildStyle() {
        StringBuilder sb = new StringBuilder();
        StringBuilder style = new StringBuilder("<style type=\"text/css\">").append("\n")
                .append("table td {").append("\n")
                .append("border-width: 1px;").append("\n")
                .append("padding: 2px;").append("\n")
                .append("border-style: solid;").append("\n")
                .append("border-color: #a9c6c9;}").append("\n")

                .append("table th {").append("\n")
                .append("background-color:#c3dde0;").append("\n")
                .append("border-width: 1px;").append("\n")
                .append("padding: 2px;").append("\n")
                .append("border-style: solid;").append("\n")
                .append("border-color: #a9c6c9;}").append("\n")

                .append("table tr {").append("\n")
                .append("background-color:#d4e3e5;}").append("\n")

                .append("table tr.old{").append("\n")
                .append("background-color: #E0E0E0;}").append("\n")

                .append("table tr.error{").append("\n")
                .append("background-color: #C80000;}").append("\n")

                .append("table {").append("\n")
                .append("font-family: verdana,arial,sans-serif;").append("\n")
                .append(" color:#333333;").append("\n")
                .append("border-width: 1px;").append("\n")
                .append("border-color: #999999;").append("\n")
                .append("border-collapse: collapse;}").append("\n")

                .append("span {").append("\n")
                .append("font-family: \"Microsoft YaHei\";").append("\n")
                .append(" color:#8d8d8d;").append("\n")
                .append("font-size:12px;").append("\n")
                .append("}").append("\n")

                .append("#content {").append("\n")
                .append("padding:20px 60px;").append("\n")

                .append("}").append("\n")

                .append("</style>")
                .append("<meta charset=\"utf-8\">     ");


        sb.append("<html>")
                .append(style)
                .append("<div id=\"content\" style=\"padding:20px 60px;\">");
        return sb;
    }


    public static void main(String[] args) {

        List<String> tos = new ArrayList<String>();
        List<String> ccs = new ArrayList<String>();
//        for (String to : Config.get(Config.EMAIL_TOS).split("\\,")) {
//            tos.add(to);
//        }
//        for (String cc : Config.get(Config.EMAIL_CCS).split("\\,")) {
//            ccs.add(cc);
//        }
//        String s1 = "";
        String s1 = "490350431@qq.com";
        String s2 = "zhoucq@izhonghong.com";
        for (String to : s1.split("\\,")) {
            tos.add(to);
        }
        for (String cc : s2.split("\\,")) {
            ccs.add(cc);
        }
        try {
            String userPassword = "Cf2ysjd";
            EmailUtils.sendMessage("smtp.exmail.qq.com", "data-platform-monitor@izhonghong.com",
                    userPassword, tos, ccs, "IGET监控-数据源入口地址检测",
                    "test",
                    "text/html;charset=utf-8");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

}



