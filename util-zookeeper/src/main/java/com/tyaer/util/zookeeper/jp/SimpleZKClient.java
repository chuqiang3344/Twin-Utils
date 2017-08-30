package com.tyaer.util.zookeeper.jp;

import java.util.Map;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.webant.ApplicationContext;
import org.webant.utils.PropertiesUtil;

public class SimpleZKClient {
	
	private static Map<String, String> configs;
	private static volatile CuratorFramework zkClient = null;
	
	private SimpleZKClient() {
		
	}
	
	public static CuratorFramework getZKClient(){
		if(null==zkClient){
			synchronized (SimpleZKClient.class) {
				if(null == zkClient){
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
	
	public static Map<String, String> initCfg(){
		//有问题，获取到的是当前启动项目的classpath路径，如果打包成jar包之后由其他程序启动，则此处获取到的是其他程序的classpath路径
		//修复方法：使用SimpleZKClient.class.getClassLoader().getResourceAsStream方法
		//String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();//有问题
		configs = PropertiesUtil.parsePropertiesFromInputStream(
				SimpleZKClient.class.getClassLoader().getResourceAsStream(ZK_CONFIG_FILE));
		configs.putAll(ApplicationContext.getInstance().getGlobalProps());
		return configs;
	}
	
	
	private static final String ZK_CONFIG_FILE = "zk/zk-config.properties"; 
	private static final String ZK_BASE_SLEEP_TIME = "zk.baseSleepTimeMs"; 
	private static final String ZK_MAX_RETRIES = "zk.maxRetries"; 
	private static final String ZK_CONNECTION_TIMEOUT = "zk.connectionTimeoutMs"; 
	private static final String ZK_SESSION_TIMEOUT = "zk.sessionTimeoutMs"; 

}
