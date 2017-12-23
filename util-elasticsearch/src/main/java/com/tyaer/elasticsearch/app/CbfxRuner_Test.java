package com.tyaer.elasticsearch.app;

import org.junit.Test;

/**
 * Created by Twin on 2017/12/13.
 */
public class CbfxRuner_Test {
    CbfxRuner cbfxRuner = new CbfxRuner();

    @Test
    public void t2() {
        String mid = "aa511238002_77139fa44d37be940c1f0f9b13251f5e";
        String domainId = mid.substring(4, mid.indexOf("_"));
        System.out.println(domainId);
    }

    @Test
    public void CbfxRuner_Test_One() {
        String event_id = "100948";
        new CbfxRuner("",event_id).run();

        /*使用sql语句优化数据*/
        analysis6sql();

        /*将传统网站的name赋予站点名称*/
        analysis7name();
    }

    @Test
    public void analysis4fmid() {
//        cbfxRuner.analysis5pathCount("101410");
        cbfxRuner.analysis4fmid("101410");
    }


    @Test
    public void analysis6sql() {
//        cbfxRuner.analysis5pathCount("101410");
        cbfxRuner.analysis6sql();
    }

    @Test
    public void analysis7name() {
//        cbfxRuner.analysis5pathCount("101410");
        cbfxRuner.analysis7name();
    }
}
