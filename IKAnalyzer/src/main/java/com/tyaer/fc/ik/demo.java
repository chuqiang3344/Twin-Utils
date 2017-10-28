package com.tyaer.fc.ik;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.*;

/**
 * Created by Twin on 2017/10/12.
 */
public class demo {
    public static void main(String[] args) {
        String str = "最希望从企业得到的是独家的内容或销售信息，获得打折或促销信息等；最不希望企业进行消息或广告轰炸及访问用户的个人信息等。这值得使用社会化媒体的企业研究";

        IKAnalysis(str);
    }

    public static String IKAnalysis(String str) {
        System.out.println(str);
        StringBuffer sb = new StringBuffer();
        try {
// InputStream in = new FileInputStream(str);//
            byte[] bt = str.getBytes();// str
            InputStream ip = new ByteArrayInputStream(bt);
            Reader read = new InputStreamReader(ip);
            IKSegmenter iks = new IKSegmenter(read, true);
            Lexeme t;
            while ((t = iks.next()) != null) {
                sb.append(t.getLexemeText() + " , ");

            }
            sb.delete(sb.length() - 1, sb.length());
        } catch (IOException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(sb.toString());
        return sb.toString();

    }

}
