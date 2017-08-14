package com.tyaer.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Twin on 2017/5/24.
 */
public class CalendarUtils {
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -7);
        long timeInMillis = c.getTimeInMillis();
        System.out.println(sdf.format(timeInMillis));
    }
}
