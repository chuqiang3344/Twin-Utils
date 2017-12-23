package com.tyaer.util.algorithm.heatvalue;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Created by Twin on 2017/12/15.
 */
public class demo {
    @Test
    public void t3() {
        int i = HeatValueComputer.computeValue(40000);
        System.out.println(i);

//        System.out.println((int) (1 * (100 * Math.log(100) / Math.log(50000))));
//        int o = HeatValueComputer.competer(0, 100000, 0, 0);
//        System.out.println(o);
    }

    @Test
    public void t2() {
        HeatValueBean heatValueBean = new HeatValueBean(null, 0, 2, 0, 0, new Timestamp(Calendar.getInstance().getTimeInMillis()), null, null);
        int i = HeatValueComputer.computeValue(heatValueBean);
        System.out.println(i);

//        int o = HeatValueComputer.competer(0, 100000, 0, 0);
//        System.out.println(o);
    }


    @Test
    public void t0() {
        HeatValueBean heatValueBean = new HeatValueBean("1604159432", 100000, 100000, 100000, 100000, new Timestamp(Calendar.getInstance().getTimeInMillis()), "正面", null);
        int i = HeatValueComputer.computeValue(heatValueBean);
        System.out.println(i);
    }

    @Test
    public void t1() {
        String json = "{\n" +
                "          \"weibo_url\": \"http://weibo.com/5058729697/Fkq6cj2YT##\",\n" +
                "          \"events_tag\": \"101366,100523\",\n" +
                "          \"reports_count\": \"110\",\n" +
                "          \"mid\": \"e3e02017090517200_4148668884539695\",\n" +
                "          \"created_at\": \"2017-09-05 17:20:03.0\",\n" +
                "          \"sourcemid\": \"0_4148668884539695\",\n" +
                "          \"pic\": \"http://ww3.sinaimg.cn/thumbnail/006y0MJdgw1f6oq252m1wj3050050q3e.jpg\",\n" +
                "          \"source\": \"<a href=\\\"sinaweibo://customweibosource\\\" rel=\\\"nofollow\\\">吃饼干吗iPad</a>\",\n" +
                "          \"uid\": \"7f070_5058729697\",\n" +
                "          \"reposts_depth\": \"0\",\n" +
                "          \"grade_all\": \"212\",\n" +
                "          \"text_loc_country\": \"中国\",\n" +
                "          \"text\": \"腾讯视频王者荣耀频道官方主持人招募大赛， 全国海选现已开启， 快来报名， 最强王者就是你！\",\n" +
                "          \"orgs_event_tag\": \"10006,10017\",\n" +
                "          \"text_loc\": \"中国\",\n" +
                "          \"zans_count\": \"242\",\n" +
                "          \"crawler_time\": \"2017-12-16 12:02:11.185\",\n" +
                "          \"crawler_site_id\": \"0\",\n" +
                "          \"emotion\": \"中性\",\n" +
                "          \"comments_count\": \"0\",\n" +
                "          \"download_type\": \"3\",\n" +
                "          \"text_subject\": \"王者荣耀,哦！王者\",\n" +
                "          \"site_id\": \"0\",\n" +
                "          \"name\": \"我是一块油猫饼咿呀咿呀哟\",\n" +
                "          \"created_date\": \"20170905\",\n" +
                "          \"updatetime\": \"2017-12-16 12:02:11.185\",\n" +
                "          \"hanmingCode\": \"嘀亀亀一一一亀一一丄一亀一一倀嘂倀丐帀一丐丄一丐伀一一一一倀丄丠\",\n" +
                "          \"indextime\": \"2017-12-14 13:52:45.027\",\n" +
                "          \"verifiedtype\": \"微博个人认证 \",\n" +
                "          \"profileimageurl\": \"//tvax1.sinaimg.cn/crop.0.0.1536.1536.50/005wlUGtly8fkjdlbia0vj316o16otcs.jpg\",\n" +
                "          \"mid_f\": \"4148373185395197\"\n" +
                "        }";
        JSONObject jsonObject = JSON.parseObject(json);
        jsonObject.put("reposts_count", jsonObject.getIntValue("reports_count"));
        jsonObject.put("zan_count", jsonObject.getIntValue("zans_count"));
        jsonObject.put("create_time", jsonObject.getDate("created_at"));
        HeatValueBean heatValueBean1 = JSON.parseObject(jsonObject.toJSONString(), HeatValueBean.class);
        int gradeAll = HeatValueComputer.computeValue(heatValueBean1);
        System.out.println(gradeAll);
    }

}
