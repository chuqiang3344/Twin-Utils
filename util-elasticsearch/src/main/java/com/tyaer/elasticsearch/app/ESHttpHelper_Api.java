package com.tyaer.elasticsearch.app;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tyaer.elasticsearch.bean.ScrollResult;
import com.tyaer.elasticsearch.manage.ESHttpHelper;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by Twin on 2017/12/5.
 */
public class ESHttpHelper_Api {
    static ESHttpHelper esHttpHelper = new ESHttpHelper();

    public static void main(String[] args) throws IOException {
        String url = args[0];
        String dsl = FileUtils.readFileToString(new File("./file/dsl.txt"), "utf-8");
        String query = esHttpHelper.query(url, dsl);
        System.out.println(query);
    }

    @Test
    public void t1() {
        String url = "http://test16:9200/i_ams_total_data/_search";
        String form = "{\n" +
                "  \"size\" : 20,\n" +
                "  \"query\" : {\n" +
                "    \"bool\" : {\n" +
                "      \"must\" : [ {\n" +
                "        \"exists\" : {\n" +
                "          \"field\" : \"events_tag\"\n" +
                "        }\n" +
                "      }, {\n" +
                "        \"match\" : {\n" +
                "          \"events_tag\" : {\n" +
                "            \"query\" : \"100683\",\n" +
                "            \"type\" : \"boolean\"\n" +
                "          }\n" +
                "        }\n" +
                "      }, {\n" +
                "        \"range\" : {\n" +
                "          \"created_at\" : {\n" +
                "            \"from\" : \"2017-06-23 17:25:04\",\n" +
                "            \"to\" : \"2100-08-12 15:43:45\",\n" +
                "            \"format\" : \"yyyy-MM-dd HH:mm:ss\",\n" +
                "            \"include_lower\" : true,\n" +
                "            \"include_upper\" : true\n" +
                "          }\n" +
                "        }\n" +
                "      } ],\n" +
                "      \"must_not\" : {\n" +
                "        \"exists\" : {\n" +
                "          \"field\" : \"article_type\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"sort\" : [ {\n" +
                "    \"created_at\" : {\n" +
                "      \"order\" : \"desc\"\n" +
                "    }\n" +
                "  } ]\n" +
                "}";
        String curl = esHttpHelper.query(url, form);
        System.out.println(curl);
    }

    @Test
    public void t11() {
        String url = "http://test16:9200/i_ams_total_data/_search";
        String form = "{\n" +
                "  \"size\" : 20,\n" +
                "  \"query\" : {\n" +
                "    \"bool\" : {\n" +
                "      \"must\" : [ {\n" +
                "        \"exists\" : {\n" +
                "          \"field\" : \"events_tag\"\n" +
                "        }\n" +
                "      }, {\n" +
                "        \"match\" : {\n" +
                "          \"events_tag\" : {\n" +
                "            \"query\" : \"100683\",\n" +
                "            \"type\" : \"boolean\"\n" +
                "          }\n" +
                "        }\n" +
                "      }, {\n" +
                "        \"range\" : {\n" +
                "          \"created_at\" : {\n" +
                "            \"from\" : \"2017-06-23 17:25:04\",\n" +
                "            \"to\" : \"2100-08-12 15:43:45\",\n" +
                "            \"format\" : \"yyyy-MM-dd HH:mm:ss\",\n" +
                "            \"include_lower\" : true,\n" +
                "            \"include_upper\" : true\n" +
                "          }\n" +
                "        }\n" +
                "      } ],\n" +
                "      \"must_not\" : {\n" +
                "        \"exists\" : {\n" +
                "          \"field\" : \"article_type\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"sort\" : [ {\n" +
                "    \"created_at\" : {\n" +
                "      \"order\" : \"desc\"\n" +
                "    }\n" +
                "  } ]\n" +
                "}";

        ScrollResult scrollResult = new ScrollResult();
        while (true) {
            scrollResult = esHttpHelper.queryScrollPaging(url, form, 10, scrollResult);
            if (scrollResult.getScroll_id() == null) {
                break;
            }
            System.out.println(scrollResult);
            System.out.println();
        }
    }

    @Test
    public void t2() {
        String url = "http://test16:9200/i_ams_total_data/_search?scroll=1m";
        String form = "{\n" +
                "    \"query\": { \"match_all\": {}},\n" +
                "    \"sort\" : [\"_doc\"],\n" +
                "    \"size\":  10\n" +
                "}";
        String curl = esHttpHelper.query(url, form);
        System.out.println(curl);

        JSONObject jsonObject = JSON.parseObject(curl);
        String scroll_id = jsonObject.getString("_scroll_id");
        while (true) {
            JSONObject scroll_json = new JSONObject();
            scroll_json.put("scroll", "1m");
            scroll_json.put("scroll_id", scroll_id);
            String form2 = scroll_json.toJSONString();

            String url2 = "http://test16:9200/_search/scroll";
            String curl2 = esHttpHelper.query(url, form);
            System.out.println(curl2);

            break;
        }


    }
}
