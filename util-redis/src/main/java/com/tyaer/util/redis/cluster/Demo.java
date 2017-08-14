package com.tyaer.util.redis.cluster;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import redis.clients.jedis.JedisCluster;

/**
 * Created by Twin on 2017/3/24.
 */
public class Demo {
    private static JedisCluster jedisCluster;
    private static ApplicationContext ctx;

    static {
        ctx = new ClassPathXmlApplicationContext("classpath:spring-redisCluster.xml");
        jedisCluster =(JedisCluster) ctx.getBean("jedisCluster");

//        jedisCluster.exists(weChatResult1.getMid());
    }
}
