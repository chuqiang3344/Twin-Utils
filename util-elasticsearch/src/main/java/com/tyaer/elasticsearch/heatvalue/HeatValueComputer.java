package com.tyaer.elasticsearch.heatvalue;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Twin on 2017/12/15.
 */
public class HeatValueComputer {
    private static Map<String, Integer> sourcePriorities = new HashMap<String, Integer>();

    static {
        InputStream inputStream = HeatValueComputer.class.getResourceAsStream("/source_priority.txt");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] fields = line.split("\t");
//                System.out.println(line);
                sourcePriorities.put(fields[1], Integer.parseInt(fields[2]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {

    }

    public static int computeValue(int reposts_count) {
        if (reposts_count == 1) {
            return reposts_count;
        }
//        double grade = comments_count * 3 + reposts_count * 1.2 + zan_count / 3 + read_count / 4;
//        double grade = reposts_count * 1.2;
        int gradeAll = reposts_count;
        int MAX = 50000;
        if (gradeAll > MAX) {
            gradeAll = MAX;
        }
        if (gradeAll == 0) {
            gradeAll = 1;
        }
//        System.out.println(Math.log(gradeAll)/Math.log(MAX));
//         decay =1;
//        gradeAll = (int) (1 * (0.3 * 100 + 25 + 45 * Math.log(gradeAll) / Math.log(MAX)));
        gradeAll = (int) (1 * (100 * Math.log(gradeAll) / Math.log(MAX)));
        if (reposts_count < gradeAll) {
            return reposts_count;
        }
        return gradeAll;
    }

    public static int computeValue(HeatValueBean heatValueBean) {
        String uid = heatValueBean.getId();
        String sourceUid = uid;
        if (StringUtils.isNotBlank(uid) && uid.contains("_")) {
            sourceUid = uid.substring(uid.indexOf("_") + 1);
        }
        Timestamp create_time = heatValueBean.getCreate_time();
        double decay = 0;
        if (create_time != null) {
            long createTime = create_time.getTime();
            long now = Calendar.getInstance().getTimeInMillis();
            long second = (now - createTime) / 1000;
            if (second < 0) {
                second = 0;
            }
            //每小时衰减9%,每天衰减90%，返回 e 的指定次幂。
            decay = Math.exp(-(0.000026) * second);
        } else {
            decay = 1;
        }
        Integer sourcePriority = sourcePriorities.get(sourceUid);
        if (sourcePriority == null) {
            sourcePriority = 0;
        }
        if (sourcePriority > 100) {
            sourcePriority = 100;
        }
        int emotionValue = 0;
        String emotion = heatValueBean.getEmotion();
        if (null != emotion && emotion.contains("负")) {
            emotionValue = 25;
        } else if (null != emotion && emotion.contains("正")) {
            emotionValue = 15;
        }
        int gradeAll = competer(heatValueBean.getComments_count(), heatValueBean.getReposts_count(), heatValueBean.getZan_count(), heatValueBean.getRead_count());
        int MAX = 200000;
        if (gradeAll > MAX) {
            gradeAll = MAX;
        }
        if (gradeAll == 0) {
            gradeAll = 1;
        }
//        System.out.println(Math.log(gradeAll)/Math.log(MAX));
//         decay =1;
        gradeAll = (int) (decay * (0.3 * sourcePriority + emotionValue + 45 * Math.log(gradeAll) / Math.log(MAX)));
        if (isWeatherInfo(heatValueBean.getText())) {
            gradeAll = 0;
        }
        heatValueBean.setHeatValue(gradeAll);
        return gradeAll;
    }

    private static int competer(Integer comments_count, Integer reposts_count, Integer zan_count, Integer read_count) {
        comments_count = comments_count == null ? 0 : comments_count;
        reposts_count = reposts_count == null ? 0 : reposts_count;
        zan_count = zan_count == null ? 0 : zan_count;
        read_count = read_count == null ? 0 : read_count;

        double grade = comments_count * 3 + reposts_count * 1.2 + zan_count / 3 + read_count / 4;
        return (int) grade;
    }

    public static boolean isWeatherInfo(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        if ((text.contains("多云") || text.contains("晴") || text.contains("阴") || text.contains("雨")) && (text.contains("最低") || text.contains("最高") || text.contains("天气")) && (text.contains("度") || text.contains("℃"))) {
            return true;
        } else if (text.contains("空气质量") && text.contains("PM2.5")) {
            return true;
        }
        return false;
    }

}
