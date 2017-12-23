package com.tyaer.elasticsearch.manage;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tyaer.elasticsearch.bean.ScrollResult;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

/**
 * elasticsearch 分页 (from+size)(scroll scan) (search after) 详解-->解决深分页 （持续更新） - CSDN博客
 http://blog.csdn.net/feifantiyan/article/details/54096138
 elasticsearch-利用游标查询 'Scroll'来做分页查询 - CSDN博客
 http://blog.csdn.net/chuan442616909/article/details/55195024
 * Created by Twin on 2017/11/21.
 */
public class ESHttpHelper {
    private static HttpUtils httpUtils = new HttpUtils();

    public String query(String url, String form) {
        String data = httpUtils.curl(url, form);
        return data;
    }

    /**
     *
     * @param url1
     * @param dsl
     * @param size
     * @param scrollResult
     * @return
     */
    public ScrollResult queryScrollPaging(String url1, String dsl, int size, ScrollResult scrollResult) {
        String url = url1 + "?scroll=1m";
        String result = null;
        String scroll_id;
        if (scrollResult == null || (scrollResult.getScroll_id() == null && scrollResult.getResult() == null)) {
            JSONObject jsonObject1 = JSON.parseObject(dsl);
            ArrayList<String> list = new ArrayList<>();
            list.add("_doc");
            jsonObject1.put("sort", list);
            jsonObject1.put("size", size);
            result = httpUtils.curl(url, jsonObject1.toJSONString());
            JSONObject jsonObject = JSON.parseObject(result);
            scroll_id = jsonObject.getString("_scroll_id");
        } else {
            scroll_id = scrollResult.getScroll_id();
            JSONObject scroll_json = new JSONObject();
            scroll_json.put("scroll", "1m");
            scroll_json.put("scroll_id", scroll_id);
            String form2 = scroll_json.toJSONString();
            String url2 = "http://test16:9200/_search/scroll";
//            String url2 = url1 + "/scroll";
            result = query(url2, form2);
            long total = JSON.parseObject(result).getJSONObject("hits").getLong("total");
            if (total > 0) {
                String scroll_id1 = JSON.parseObject(result).getString("_scroll_id");
                if (!scroll_id.equals(scroll_id1)) {
                    System.out.println("scroll_id 发生变化！");
                }
                scroll_id = scroll_id1;
            } else {
                scroll_id = null;
            }
        }
        ScrollResult scrollResult2 = new ScrollResult(scroll_id, result);
        return scrollResult2;
    }


    static class HttpUtils {
        private CloseableHttpClient client = createSSLClientDefault();

        /**
         * 描述：针对https采用SSL的方式创建httpclient
         *
         * @return
         */
        public CloseableHttpClient createSSLClientDefault() {
            try {
                SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                    //信任所有
                    public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        return true;
                    }
                }).build();
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
                return HttpClients.custom().setSSLSocketFactory(sslsf).build();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            }
            return HttpClients.createDefault();
        }

        public String curl(String loginUrl, String json) {
            HttpPost post = new HttpPost(loginUrl);
            StringEntity reqEntity = null;
            reqEntity = new StringEntity(json,"utf-8");
            post.setEntity(reqEntity);

            String result = "";
            try {
                HttpResponse response = client.execute(post);
                HttpEntity entity = response.getEntity();
                result = IOUtils.toString(entity.getContent(), "utf-8");
//            System.out.println("result :"+result);
                EntityUtils.consume(entity);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                post.releaseConnection();
            }
            return result;
        }
    }
}
