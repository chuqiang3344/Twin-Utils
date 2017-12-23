package com.tyaer.elasticsearch.conf;

import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by Twin on 2017/11/20.
 */
public class ConfigFileReader {
    private static final Logger LOGGER = Logger.getLogger(ConfigFileReader.class);
    private String fileName;
    private Map<String, String> configs = new HashMap<String, String>();

    public ConfigFileReader(String fileName) {
        this.fileName = fileName;
        refreshConfig(fileName);
    }

    public void refreshConfig(String fileName) {
        Properties pps = new Properties();
        BufferedInputStream bufferedInputStream = null;
        try {
//            bufferedInputStream = new BufferedInputStream(BaseConfig.class.getResourceAsStream("/pro.properties"));
            bufferedInputStream = new BufferedInputStream(DTO.class.getClassLoader().getResourceAsStream(fileName));
//            bufferedInputStream = new BufferedInputStream(new FileInputStream(new File("./configure/pro.properties")));
            pps.load(bufferedInputStream);
            Set<String> set = pps.stringPropertyNames();
            for (String key : set) {
                configs.put(key, pps.getProperty(key));
            }
            LOGGER.info("###"+ConfigFileReader.class.getSimpleName() + ":" + configs);
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

    public String getConfigValue(String key) {
        if (configs.containsKey(key)) {
            return configs.get(key);
        } else {
            LOGGER.warn("无配置信息：" + key);
            return null;
        }
    }
}
