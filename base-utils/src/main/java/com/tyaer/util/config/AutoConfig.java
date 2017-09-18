package com.tyaer.util.config;

import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by Twin on 2017/9/18.
 */
public class AutoConfig {
    private static final Logger LOGGER = Logger.getLogger(AutoConfig.class);

    private static Map<String, String> configs = new HashMap<String, String>();

    static {
        refreshConfig();
    }

    public static void refreshConfig() {
        Properties pps = new Properties();
        BufferedInputStream bufferedInputStream = null;
        try {
//            bufferedInputStream = new BufferedInputStream(BaseConfig.class.getResourceAsStream("/pro.properties"));
            bufferedInputStream = new BufferedInputStream(BaseConfig.class.getClassLoader().getResourceAsStream("pro.properties"));
//            bufferedInputStream = new BufferedInputStream(new FileInputStream(new File("./configure/pro.properties")));
            pps.load(bufferedInputStream);
            Set<String> set = pps.stringPropertyNames();
            for (String key : set) {
                configs.put(key, pps.getProperty(key));
            }
            LOGGER.info(AutoConfig.class.getSimpleName() + ":" + configs);
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

    public static String getConfigValue(String key) {
        if (configs.containsKey(key)) {
            return configs.get(key);
        } else {
            LOGGER.warn("无配置信息：" + key);
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println(AutoConfig.getConfigValue("kafka.brokers"));
    }
}
