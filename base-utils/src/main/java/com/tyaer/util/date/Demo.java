package com.tyaer.util.date;

import org.junit.Test;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    @Test
    public void t1() throws ParseException {
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        System.out.println(simpleDateFormat.parse("2017-11-03 19:46:25.0"));

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String source = "2017-11-03 19:46:25.012";
        System.out.println(source);
        Date date = simpleDateFormat.parse(source);
        System.out.println(date);
        System.out.println(date.getTime());
    }
}
