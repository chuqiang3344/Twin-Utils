package com.tyaer.util.config;

import org.junit.Test;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Twin on 2017/4/1.
 */
public class BaseConfig {

    public static final String OPERATING_ENVIRONMENT = "operating.environment";
    public static final String KEY_MYSQL_URL = "MYSQLURL";
    public static final String KEY_MYSQL_USERNAME = "MYSQLUSERNAME";
    public static final String KEY_MYSQL_PASSWORD = "MYSQLPASSWORD";

    public static final String KEY_KAFKA_BROKERS = "kafka.brokers";
    public static final String KAFKA_BACKER_GROUP_ID = "kafka.backer.group_id";
    public static final String HBASE_TABLE_NAME = "hbase.table.name";
    public static final String SCAN_INTERVAL_NUM = "scan.interval.num";

    public static Map<String, String> configs = new HashMap<String, String>();

    static {
        refreshConfig();
    }

    public static void refreshConfig() {
        Properties pps = new Properties();
        BufferedInputStream bufferedInputStream = null;
        try {
//            bufferedInputStream = new BufferedInputStream(BaseConfig.class.getResourceAsStream("./configure/pro.properties"));
            bufferedInputStream = new BufferedInputStream(new FileInputStream(new File("./configure/pro.properties")));
            pps.load(bufferedInputStream);
            configs.put(KEY_MYSQL_URL, pps.getProperty(KEY_MYSQL_URL));
            configs.put(KEY_MYSQL_USERNAME, pps.getProperty(KEY_MYSQL_USERNAME));
            configs.put(KEY_MYSQL_PASSWORD, pps.getProperty(KEY_MYSQL_PASSWORD));
            configs.put(KEY_KAFKA_BROKERS, pps.getProperty(KEY_KAFKA_BROKERS));
            configs.put(KAFKA_BACKER_GROUP_ID, pps.getProperty(KAFKA_BACKER_GROUP_ID));
            configs.put(HBASE_TABLE_NAME, pps.getProperty(HBASE_TABLE_NAME));
            configs.put(OPERATING_ENVIRONMENT, pps.getProperty(OPERATING_ENVIRONMENT));
            configs.put(SCAN_INTERVAL_NUM, pps.getProperty(SCAN_INTERVAL_NUM));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            pps.clear();
        }
    }

    public static String get(String key) {
        return configs.get(key);
    }

    public static void main(String[] args) {
        System.out.println(BaseConfig.get(KEY_MYSQL_URL));
    }

    @Test
    public void test(){
        File file = new File("./");
        System.out.println(file.getAbsolutePath());
    }
}
