import com.alibaba.fastjson.JSON;
import com.tyaer.elasticsearch.manage.ElasticSearchHelper;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.After;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.*;

/**
 * Created by Twin on 2017/8/15.
 */
public class ElasticSearchHelper_Test {
    static String es_hosts = "192.168.2.116:9300,192.168.2.115:9300,192.168.2.116:9400";
    static String esClusterName = "izhonghong";
    //    static String es_hosts = "192.168.2.231:9200,192.168.2.233:9200,192.168.2.234:9200";
    static ElasticSearchHelper elasticSearchHelper = new ElasticSearchHelper(es_hosts,esClusterName);

    public static void main(String[] args) {

    }

    String index = "zcq_test";
    String type = "t_user_test";

    @Test
    public void t1() {
        System.out.println(elasticSearchHelper.getIndicies());
        System.out.println(elasticSearchHelper.isExistsType("i_ams_total_data", "t_user"));
    }

    @Test
    public void batchUpdate(){
        List<Map<String, Object>> mapList = new ArrayList<>();
        HashMap<String, Object> map = new HashMap<>();
        map.put("_id",10009);
        map.put("name",null);
//        map.put("name","abc");
        map.put("fansnum",100);
        Timestamp timestamp = new Timestamp(Calendar.getInstance().getTimeInMillis());
        System.out.println(timestamp);
        map.put("updatetime", timestamp);
//        map.put("updatetime",new Timestamp(System.currentTimeMillis()));
//        map.put("updatetime",Calendar.getInstance().getTimeInMillis());
        mapList.add(map);

//        HashMap<String, Object> map2 = new HashMap<>();
//        map2.put("_id",10002);
//        map2.put("name","b");
//        map2.put("updatetime",new Timestamp(System.currentTimeMillis()));
//        mapList.add(map2);

        elasticSearchHelper.batchUpdate(index, type,mapList);
    }

    @Test
    public void deleteField(){
//        elasticSearchHelper.delete("zcq_test","t_user_test",10001+"");
//        elasticSearchHelper.delete("zcq_test","t_user_test",10001+"","fansnum");
        elasticSearchHelper.bulkDelete("zcq_test","t_user_test","name","10001","10002");
    }


    @Test
    public void insert() {
        String es_hosts = "192.168.2.116:9300,192.168.2.115:9300,192.168.2.116:9400";
        ElasticSearchHelper helper = new ElasticSearchHelper(es_hosts,"izhonghong");
        String str = "{\"data\":[{\"weibo_url\":\"http://weibo.com/2212959090/FkiHvcUva?from=page_1005052212959090_profile&wvr=6&mod=weibotime\",\"reports_count\":0,\"created_at\":1504535399000,\"mid\":\"693b2017090422290_4148384493077132222\",\"pic\":\"//wx1.sinaimg.cn/thumb150/83e71372gy1fj7wg10w3aj20ie0ptmy5.jpg,//wx2.sinaimg.cn/thumb150/83e71372gy1fj7wgm6mdsj20ll0uxq7d.jpg,//wx4.sinaimg.cn/thumb150/83e71372gy1fj7wh8jx3fj20qo0zkwtt.jpg,//wx4.sinaimg.cn/thumb150/83e71372gy1fj7wheqm7oj20qo0zkk1l.jpg\",\"source\":\"OPPO R9 Plus\",\"reposts_depth\":0,\"uid\":\"29f40_2212959090\",\"grade_all\":0,\"zan_count\":0,\"text_loc_country\":\"中国\",\"text\":\"高兴得失去了色彩 \u200B\u200B\u200B\u200B\",\"reposts_count\":0,\"text_loc\":\"中国\",\"profileImageUrl\":\"//tva4.sinaimg.cn/crop.0.0.996.996.50/83e71372jw8fcsaa8qjtnj20ro0ro77l.jpg\",\"key\":\"693b2017090422290_4148384493077132\",\"sourceMid\":\"0_4148384493077132\",\"verified_type\":\"\",\"emotion\":\"中性\",\"comments_count\":0,\"download_type\":\"5\",\"name\":\"我在人间举火\",\"site_id\":0,\"created_date\":\"20170904\",\"updatetime\":\"2017-09-05 03:22:37.33\"}],\"type\":\"weibo\"}";
        String data = JSON.parseObject(str).getString("data");
//        List<FTStatusWeibo> list = JSON.parseArray(data, FTStatusWeibo.class);
//        System.out.println(list.size());
//        helper.BulkIndex("i_ams_total_data", "t_status_weibo", list);
//        helper.close();
    }

    @After
    public void close() {
        elasticSearchHelper.close();
    }
}
