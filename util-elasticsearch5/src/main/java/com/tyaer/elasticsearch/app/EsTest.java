package com.tyaer.elasticsearch.app;

import org.apache.http.HttpHost;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Twin on 2017/10/26.
 */
public class EsTest {
    public static void main(String[] args) {

        RestClient restClient = RestClient.builder(
                new HttpHost("192.168.10.5", 9200, "http"),
                new HttpHost("192.168.10.6", 9200, "http"),
                new HttpHost("192.168.10.7", 9200, "http")).build();

        try {
            // （1） 执行一个基本的方法，验证es集群是否搭建成功
            Response response = null;
            response = restClient.performRequest("GET", "/", Collections.singletonMap("pretty", "true"));
            System.out.println(EntityUtils.toString(response.getEntity()));

            // （2）验证es的某个索引是否存在

            Response response2 = restClient.performRequest("HEAD", "/product/pdt", Collections.<String, String>emptyMap());
            System.out.println(response2.getStatusLine().getReasonPhrase().equals("OK"));

            // (3) 删除某个索引的指定条件的数据
            Map<String, String> paramMap = new HashMap<String, String>();
            String id = "qwe";
            paramMap.put("q", "id:" + id);
            paramMap.put("pretty", "true");
            Response response3 = restClient.performRequest("DELETE", "product/pdt/_query", paramMap);
            System.out.println(EntityUtils.toString(response3.getEntity()));

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
