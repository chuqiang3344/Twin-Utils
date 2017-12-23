package com.tyaer.elasticsearch.app.cbfx;

import com.tyaer.database.mysql.MySQLHelperPool;
import com.tyaer.database.mysql.MySQLHelperSingleton;
import com.tyaer.elasticsearch.app.CbfxRuner;
import com.tyaer.elasticsearch.conf.DTO;
import com.tyaer.elasticsearch.manage.EsClientMananger;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Twin on 2017/12/18.
 */
public class EventImport {
    private static final Logger logger = Logger.getLogger(CbfxRuner.class);
    private static String url_jdbc;
    private static String user_name;
    private static String password;
    private static MySQLHelperSingleton mySQLHelperPool;
    private static EsClientMananger esClientMananger;
    private static String tableName = "ams_cbfx";
    private static String tableNameEvent = "ams_cbfx_event";

    static {
        Properties pps = new Properties();
        try {
            pps.load(CbfxRuner.class.getResourceAsStream("/elasticsearch.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        url_jdbc = pps.getProperty("jdbc.mysql.url");
        user_name = pps.getProperty("jdbc.mysql.username");
        password = pps.getProperty("jdbc.mysql.password");
        mySQLHelperPool = new MySQLHelperSingleton(user_name, password, url_jdbc);

        String es_hosts = DTO.configFileReader.getConfigValue("es.hosts");
        String esClusterName = DTO.configFileReader.getConfigValue("es.cluster.name");
//        esClientMananger = new EsClientMananger(es_hosts, esClusterName);
    }
    public static void main(String[] args) {
        MySQLHelperSingleton mySQLHelperPool2 = new MySQLHelperSingleton(user_name, password, "jdbc:mysql://10.248.161.10:8080/ams?useUnicode=true&characterEncoding=UTF-8");
        List<Map<String, Object>> modeResult2 = mySQLHelperPool2.findModeResult("select * from vt_subject where organizationid=10006");
        for (Map<String, Object> stringObjectMap : modeResult2) {

        }

//        List<Map<String, Object>> modeResult = mySQLHelperPool.findModeResult("select * from " + tableNameEvent);

    }
}
