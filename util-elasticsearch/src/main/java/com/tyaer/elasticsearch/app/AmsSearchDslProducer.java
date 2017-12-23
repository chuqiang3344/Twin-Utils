package com.tyaer.elasticsearch.app;

import com.tyaer.elasticsearch.search.CommonSearcher;
import com.tyaer.elasticsearch.search.ExpressionTransform;

/**
 * Created by Twin on 2017/11/17.
 */
public class AmsSearchDslProducer {
    public static void main(String[] args) {
        CommonSearcher commonSearcher = new CommonSearcher();
        String expression;
//		String expression = format("未*(征收|补偿)*数目");
        //System.out.println(expression);
//		 expression="盐田|沙头角|沙头角*(田心|桥东|中英街|东和)|海山街道|海山*(田东|梧桐|鹏湾|海涛)|梅沙|梅沙*(滨海|东海岸|盐田港|三洲田)|深圳*海山|深圳+郭永航)";
//		 expression="黄牛|(衡阳-火车票)";
//		 expression="(茂名|信宜) (食堂|饭堂|饭菜) 老鼠";
        expression="(茂名|信宜) (食堂|饭堂|饭菜) 老鼠 一中阿萨德*";
//		expression="深圳宝安";
//		 expression="深圳 宝安";
//		 expression="深圳+宝安";
//		 expression="深圳*宝安";
        System.out.println("format:"+commonSearcher.format(expression));

        System.out.println(commonSearcher.builderQuery(expression,null));

        System.out.println("k:"+ ExpressionTransform.resolveExpression(expression));

    }
}
