package com.tyaer.util;

/**
 * Created by Twin on 2017/5/5.
 */
public class Test64 {
    public static void main(String[] args) {
        int hashMax = 65535;
//        String start = get4(Integer.toHexString(0));
        String start = get4(Integer.toHexString(hashMax/2));
        System.out.println(start);
    }

    private static String get4(String start) {
        switch (start.length()) {
            case 1:
                start = "000" + start;
                break;
            case 2:
                start = "00" + start;
                break;
            case 3:
                start = "0" + start;
                break;
            default:
                break;
        }
        return start;
    }
}
