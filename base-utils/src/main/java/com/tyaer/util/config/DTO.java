package com.tyaer.util.config;

import org.apache.log4j.Logger;

/**
 * Created by Twin on 2017/9/18.
 */
public class DTO {
    private static final Logger LOGGER = Logger.getLogger(DTO.class);

    public static ConfigFileReader configFileReader = new ConfigFileReader("pro.properties");

    public static void main(String[] args) {
//        System.out.println(AutoConfig.getConfigValue("kafka.brokers"));
        System.out.println(DTO.configFileReader.getConfigValue("es.hosts"));
    }


}
