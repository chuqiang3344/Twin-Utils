package com.tyaer.util.email;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

/**
 * 邮件多人发送，可设置发送，抄送，密送
 *
 * @author zhutongyu
 */
public class SendMail {

    private static SendMail instance = null;

    private SendMail() {

    }

    public static SendMail getInstance() {
        if (instance == null) {
            instance = new SendMail();
        }
        return instance;
    }

    public static void main(String args[]) {
        SendMail send = SendMail.getInstance();
        String to[] = {"zhoucq@izhonghong.com"};
        String cs[] = {"zhoucq@izhonghong.com"};
        String ms[] = {"490350431@qq.com"};
        String subject = "测试一下";
        String content = "这是邮件内容，仅仅是测试，不需要回复";
        String formEmail = "data-platform-monitor@izhonghong.com";
        String[] arrArchievList = null;
//        String[] arrArchievList = new String[4];
//        arrArchievList[0] = "c:\\2012052914033429140297.rar";
//        arrArchievList[1] = "c:\\topSearch.html";
//        arrArchievList[2] = "c:\\topSearch2.html";
//        arrArchievList[3] = "c:\\logo_white.png";
        String userPassword = "Cf2ysjd";
        // 2.保存多个附件
        send.send(to, cs, ms, subject, content, formEmail, userPassword, arrArchievList);
    }

    public void send(String to[], String cs[], String ms[], String subject, String content, String formEmail, String fromUserPassword, String fileList[]) {
        try {
            Properties props = new Properties();
            // Properties p = System.getProperties();
            props.put("mail.smtp.auth", "true");
//            p.put("mail.smtp.host", "smtp.asia-media.cn");
            props.put("mail.smtp.host", "smtp.exmail.qq.com");
//            p.put("mail.transport.protocol", "smtp");
//            p.put("mail.smtp.port", "25");

            //props.put("mail.smtp.starttls.enable","true");//使用 STARTTLS安全连接
            //props.put("mail.smtp.port", "25");             //google使用465或587端口
// 建立会话
//            Session session = Session.getInstance(p);
            Session mailSession = Session.getInstance(props, new MyAuthenticator(formEmail, fromUserPassword));
            MimeMessage msg = new MimeMessage(mailSession); // 建立信息
            msg.setFrom(new InternetAddress(formEmail)); // 发件人

            String toList = null;
            String toListcs = null;
            String toListms = null;

//发送,
            if (to != null) {
                toList = getMailList(to);
                InternetAddress[] iaToList = new InternetAddress().parse(toList);
                msg.setRecipients(Message.RecipientType.TO, iaToList); // 收件人
            }

//抄送
            if (cs != null) {
                toListcs = getMailList(cs);
                InternetAddress[] iaToListcs = new InternetAddress().parse(toListcs);
                msg.setRecipients(Message.RecipientType.CC, iaToListcs); // 抄送人
            }

//密送
            if (ms != null) {
                toListms = getMailList(ms);
                InternetAddress[] iaToListms = new InternetAddress().parse(toListms);
                msg.setRecipients(Message.RecipientType.BCC, iaToListms); // 密送人
            }
            msg.setSentDate(new Date()); // 发送日期
            msg.setSubject(subject); // 主题
            msg.setText(content); // 内容
//显示以html格式的文本内容
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(content, "text/html;charset=utf-8");
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

// 2.保存多个附件
            if (fileList != null) {
                addTach(fileList, multipart);
            }

            msg.setContent(multipart);
// 邮件服务器进行验证
            Transport tran = mailSession.getTransport("smtp");
//            tran.connect("smtp.asia-media.cn", formEmail, fromUserPassword);
            tran.connect();
            tran.sendMessage(msg, msg.getAllRecipients()); // 发送sendMessage ，先connect
//            tran.send(msg, msg.getAllRecipients()); // 发送，封装了connect
            System.out.println("邮件发送成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //添加多个附件
    public void addTach(String fileList[], Multipart multipart) throws MessagingException, UnsupportedEncodingException {
        for (int index = 0; index < fileList.length; index++) {
            MimeBodyPart mailArchieve = new MimeBodyPart();
            FileDataSource fds = new FileDataSource(fileList[index]);
            mailArchieve.setDataHandler(new DataHandler(fds));
            mailArchieve.setFileName(MimeUtility.encodeText(fds.getName(), "GBK", "B"));
            multipart.addBodyPart(mailArchieve);
        }
    }

    private String getMailList(String[] mailArray) {

        StringBuffer toList = new StringBuffer();
        int length = mailArray.length;
        if (mailArray != null && length < 2) {
            toList.append(mailArray[0]);
        } else {
            for (int i = 0; i < length; i++) {
                toList.append(mailArray[i]);
                if (i != (length - 1)) {
                    toList.append(",");
                }

            }
        }
        return toList.toString();

    }

}
