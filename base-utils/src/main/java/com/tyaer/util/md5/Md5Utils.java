package com.tyaer.util.md5;

import org.junit.Test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Md5Utils {
    /*private static MessageDigest md = null;
    static{
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
        }
    }*/
    private final static String[] charDigits;

    private static ThreadLocal<MessageDigest> threadLocal;

    static {
        charDigits = new String[]{"0", "1", "2", "3", "4", "5",
                "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

        threadLocal = ThreadLocal.withInitial(() -> {
            try {
                return MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public static MessageDigest getMD() {
        return threadLocal.get();
    }

    private static String byteToArrayString(byte bByte) {
        int iRet = bByte;
        if (iRet < 0) {
            iRet += 256;
        }
        int iD1 = iRet / 16;
        int iD2 = iRet % 16;
        return charDigits[iD1] + charDigits[iD2];
    }

    /**
     * 转换字节数组为16进制字串
     *
     * @param bytes
     * @return
     */
    private static String byteToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(byteToArrayString(bytes[i]));
        }
        return sb.toString();
    }

    public static String getMd5Str(String str, int n) {
        String md5Str = getMd5Str(str.getBytes());
        return md5Str.substring(0, n);
    }

    public static String getMd5Str(String str) {
        //return  byteToString(md.digest(str.getBytes()));
        return getMd5Str(str.getBytes());
    }

    public static String getMd5Str(byte[] bytes) {
        //return  byteToString(md.digest(bytes));
        return byteToString(getMD().digest(bytes));
    }

    public static void main(String[] args) {
        System.out.println(Md5Utils.getMd5Str("aba4306befee2ddc7f18e8daee286aba4300_76e727f756befee2ddc7f18e8daee286aba4300_76e727f756befee2ddc7f18e8daee286"));
        System.out.println(Md5Utils.getMd5Str("5483049796",4));//ff6f_5483049796
        System.out.println(Md5Utils.getMd5Str("8001_26521383523",4));//ff6f_5483049796
        //System.out.println(UUID.nameUUIDFromBytes("359441051550393".getBytes()));
    }

    @Test
    public void md5(){
        System.out.println(Md5Utils.getMd5Str("奉贤 逼 纳税人无视"));
    }

    @Test
    public void sgwxmid(){
        String mid="8001_"+"22474836671";
        String res = Md5Utils.getMd5Str(mid, 4) + mid;
        System.out.println(res);
    }

    @Test
    public void slwbUidRowkey(){
        String mid="3134851002";
        String res = Md5Utils.getMd5Str(mid, 4) +"_"+ mid;
        System.out.println(res);
    }
}
