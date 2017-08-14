package com.tyaer.util.email;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * Created by Twin on 2017/7/12.
 */
public class MyAuthenticator extends Authenticator {

    String userName = "";
    String password = "";

    public MyAuthenticator() {

    }

    public MyAuthenticator(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(userName, password);
    }
}
