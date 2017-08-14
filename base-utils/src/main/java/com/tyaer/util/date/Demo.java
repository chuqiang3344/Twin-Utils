package com.tyaer.util.date;

import org.junit.Test;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Twin on 2017/7/21.
 */
public class Demo {
    public static void main(String[] args) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR,-4);
        System.out.println(calendar.getTime());
        System.out.println(Calendar.getInstance().getTime());
    }

    @Test
    public void tiemstamp(){
        long timeInMillis = Calendar.getInstance().getTimeInMillis();
        System.out.println(timeInMillis);
        Timestamp timestamp = new Timestamp(timeInMillis);
//        Timestamp timestamp = new Timestamp(1502692448937);
        System.out.println(timestamp);
        System.out.println(timestamp.getTime());
        Date time = new Date(timestamp.getTime());
        System.out.println(time);
    }
}
