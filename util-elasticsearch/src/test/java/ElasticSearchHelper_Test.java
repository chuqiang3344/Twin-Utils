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

    @Test
    public void t1() {
        System.out.println(elasticSearchHelper.getIndicies());
        System.out.println(elasticSearchHelper.isExistsType("i_ams_total_data", "t_user"));
    }

    @Test
    public void batchUpdate(){
        List<Map<String, Object>> mapList = new ArrayList<>();
        HashMap<String, Object> map = new HashMap<>();
        map.put("_id",10001);
        map.put("name","abc");
        map.put("updatetime",new Timestamp(System.currentTimeMillis()));
        mapList.add(map);

        HashMap<String, Object> map2 = new HashMap<>();
        map2.put("_id",10002);
        map2.put("name","b");
        mapList.add(map2);

        elasticSearchHelper.batchUpdate("zcq_test","t_user_test",mapList);
    }

    @After
    public void close() {
        elasticSearchHelper.close();
    }
}
