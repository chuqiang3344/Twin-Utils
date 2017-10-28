import com.tyaer.elasticsearch.counselor.EsBulkHandler;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.HashMap;

/**
 * Created by Twin on 2017/9/20.
 */
public class EsBulkHandler_Test {

    static String es_hosts = "192.168.2.116:9300,192.168.2.115:9300,192.168.2.116:9400";
    static String esClusterName = "izhonghong";

    static String index = "zcq_test";
    static String type = "t_test";

    public static void main(String[] args) {
        EsBulkHandler esBulkHandler = new EsBulkHandler(es_hosts, esClusterName);

        for (int i = 0; i < 100; i++) {
            HashMap<String, Object> map = new HashMap<>();
            int id = i + 10000;
            map.put("_id", id);
            map.put("name", "bbbbbbb");
            map.put("fansnum", 100);
            map.put("updatetime", new Timestamp(System.currentTimeMillis()));
//            esBulkHandler.updateChangeToBuilder(index,type,map);
//            map.put("_id", id);
//            esBulkHandler.updateChangeToBuilder(index,"t_test2",map);
//            esBulkHandler.deleteFieldChangeToBuilder(index,type,"10002","updatetime");
//            esBulkHandler.deleteFieldChangeToBuilder(index,type,"10001","fansnum");
            esBulkHandler.deleteChangeToBuilder(index, type, id + "");
            esBulkHandler.deleteChangeToBuilder(index, "t_test2", id + "");
        }
//        System.exit(0);
    }

    @Test
    public void t1() {
        EsBulkHandler esBulkHandler = new EsBulkHandler(es_hosts, esClusterName);
        HashMap<String, Object> map = new HashMap<>();
        int id = 10000;
        map.put("_id", id);
        map.put("name", "bbbbbbb");
        map.put("fansnum", 100);
        map.put("updatetime", new Timestamp(System.currentTimeMillis()));
//            esBulkHandler.updateChangeToBuilder(index,"t_test2",map);
        esBulkHandler.deleteFieldChangeToBuilder(index, type, "10001", "updatetime");
        esBulkHandler.deleteFieldChangeToBuilder(index, type, "10001", "updatetime");
//            esBulkHandler.deleteFieldChangeToBuilder(index,type,"10001","fansnum");
        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



}
