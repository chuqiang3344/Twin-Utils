package com.tyaer.util;

/**
 * Created by Twin on 2016/10/19.
 */
public class Base62Util {
    static String[] str62key = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    public Base62Util() {
    }

    public static String url2mid(String url) {
        String mid = "";
        String k = url.toString().substring(3, 4);
        int i;
        int offset1;
        int offset2;
        String str;
        if(!k.equals("0")) {
            for(i = url.length() - 4; i > -4; i -= 4) {
                offset1 = i < 0?0:i;
                offset2 = i + 4;
                str = url.toString().substring(offset1, offset2);
                str = str62to10(str);
                if(offset1 > 0) {
                    while(str.length() < 7) {
                        str = '0' + str;
                    }
                }

                mid = str + mid;
            }
        } else {
            for(i = url.length() - 4; i > -4; i -= 4) {
                offset1 = i < 0?0:i;
                offset2 = i + 4;
                if((offset1 <= -1 || offset1 >= 1) && offset1 <= 4) {
                    str = url.toString().substring(offset1 + 1, offset2);
                    str = str62to10(str);
                    if(offset1 > 0) {
                        while(str.length() < 7) {
                            str = '0' + str;
                        }
                    }

                    mid = str + mid;
                } else {
                    str = url.toString().substring(offset1, offset2);
                    str = str62to10(str);
                    if(offset1 > 0) {
                        while(str.length() < 7) {
                            str = '0' + str;
                        }
                    }

                    mid = str + mid;
                }
            }
        }

        return mid;
    }

    public static String mid2url(String mid) {
        String url = "";

        for(int j = mid.length() - 7; j > -7; j -= 7) {
            int offset3 = j < 0?0:j;
            int offset4 = j + 7;
            String num;
            if(48 == mid.charAt(offset3)) {
                num = mid.substring(offset3 + 1, offset4);
                num = int10to62((double)Integer.valueOf(num).intValue());
                if(num.length() < 4) {
                    url = 0 + num + url;
                } else {
                    url = num + url;
                }

                if(url.length() == 9) {
                    url = url.substring(1, url.length());
                }
            } else {
                num = mid.substring(offset3, offset4);
                num = int10to62((double)Integer.valueOf(num).intValue());
                url = num + url;
            }
        }

        return url;
    }

    public static String str62to10(String str) {
        String i10 = "0";
        int c = 0;

        for(int i = 0; i < str.length(); ++i) {
            int n = str.length() - i - 1;
            String s = str.substring(i, i + 1);

            for(int k = 0; k < str62key.length; ++k) {
                if(s.equals(str62key[k])) {
                    c += (int)((double)k * Math.pow(62.0D, (double)n));
                    break;
                }
            }

            i10 = String.valueOf(c);
        }

        return i10;
    }

    public static String int10to62(double int10) {
        String s62 = "";
        int w = (int)int10;
        boolean r = false;

        int a1;
        for(boolean a = false; w != 0; w = (int)Math.floor((double)a1)) {
            int r1 = w % 62;
            s62 = str62key[r1] + s62;
            a1 = w / 62;
        }

        return s62;
    }

    public static void main(String[] args) throws Exception {
//        String aa = url2mid("ezFiiwu0PYZ");
        String aa = url2mid("eC5gXSmwxPr");
        System.out.println(aa);
        String bb = mid2url("4011230160706976");
        System.out.println(bb);

        System.out.println(url2mid("F2IR644gx"));
        System.out.println(mid2url("4106490840969713"));

        System.out.println(mid2url("4162136668820995"));
    }

}
