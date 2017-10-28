package com.tyaer.util.zookeeper.jp;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SimpleZKClient {

    public static final String ZK_LOCATION = "zk.location";
    public static final String ZK_KAFKA_OFFSET_PATH = "zk.kafkaOffsetPath";
    private static final String ZK_CONFIG_FILE = "/zk-config.properties";
    private static final String ZK_BASE_SLEEP_TIME = "zk.baseSleepTimeMs";
    private static final String ZK_MAX_RETRIES = "zk.maxRetries";
    private static final String ZK_CONNECTION_TIMEOUT = "zk.connectionTimeoutMs";
    private static final String ZK_SESSION_TIMEOUT = "zk.sessionTimeoutMs";
    private static Map<String, String> configs;
    private static volatile CuratorFramework zkClient = null;
    private SimpleZKClient() {

    }

    public static CuratorFramework getZKClient(String zk_address) throws IOException {
        if (null == zkClient) {
            synchronized (SimpleZKClient.class) {
                if (null == zkClient) {
                    Map<String, String> props = initCfg();
                    int baseSleepTimeMs = 1000;
                    int maxRetries = 3;
                    int connectionTimeoutMs = 1000;
                    int sessionTimeoutMs = 1000;
                    zkClient = ZookeeperFactoryBean.createWithOptions(zk_address,
                            new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries), connectionTimeoutMs, sessionTimeoutMs);
                }
            }
        }

        return zkClient;
    }

    public static CuratorFramework getZKClient() throws IOException {
        if (null == zkClient) {
            synchronized (SimpleZKClient.class) {
                if (null == zkClient) {
                    Map<String, String> props = initCfg();
                    String connectionString = props.get(ZKConstants.ZK_LOCATION);
                    int baseSleepTimeMs = Integer.valueOf(props.get(ZK_BASE_SLEEP_TIME)).intValue();
                    int maxRetries = Integer.valueOf(props.get(ZK_MAX_RETRIES)).intValue();
                    int connectionTimeoutMs = Integer.valueOf(props.get(ZK_CONNECTION_TIMEOUT)).intValue();
                    int sessionTimeoutMs = Integer.valueOf(props.get(ZK_SESSION_TIMEOUT)).intValue();
                    zkClient = ZookeeperFactoryBean.createWithOptions(connectionString,
                            new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries), connectionTimeoutMs, sessionTimeoutMs);
                }
            }
        }

        return zkClient;
    }

    public static Map<String, String> initCfg() throws IOException {
        //有问题，获取到的是当前启动项目的classpath路径，如果打包成jar包之后由其他程序启动，则此处获取到的是其他程序的classpath路径
        //修复方法：使用SimpleZKClient.class.getClassLoader().getResourceAsStream方法
        //String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();//有问题
//		configs = PropertiesUtil.parsePropertiesFromInputStream(
//				SimpleZKClient.class.getClassLoader().getResourceAsStream(ZK_CONFIG_FILE));
//		configs.putAll(ApplicationContext.getInstance().getGlobalProps());
//		return configs;
        Properties pps = new Properties();
        pps.load(SimpleZKClient.class.getResourceAsStream(ZK_CONFIG_FILE));
        configs = new HashMap<String, String>();
        configs.put(ZK_BASE_SLEEP_TIME, pps.getProperty(ZK_BASE_SLEEP_TIME));
        configs.put(ZK_MAX_RETRIES, pps.getProperty(ZK_MAX_RETRIES));
        configs.put(ZK_CONNECTION_TIMEOUT, pps.getProperty(ZK_CONNECTION_TIMEOUT));
        configs.put(ZK_SESSION_TIMEOUT, pps.getProperty(ZK_SESSION_TIMEOUT));
        configs.put(ZK_LOCATION, pps.getProperty(ZK_LOCATION));
        configs.put(ZK_KAFKA_OFFSET_PATH, pps.getProperty(ZK_KAFKA_OFFSET_PATH));
        return configs;
    }

}
