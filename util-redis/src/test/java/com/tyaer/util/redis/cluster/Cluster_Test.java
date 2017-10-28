package com.tyaer.util.redis.cluster;

import org.junit.Test;
import redis.clients.jedis.JedisCluster;

import java.io.IOException;

/**
 * Created by Twin on 2017/10/11.
 */
public class Cluster_Test {
    @Test
    public void t1() {
//        String redisURLs = "192.168.2.114:7000,192.168.2.114:7001,192.168.2.114:7002";
        String redisURLs = "192.168.2.116:7000,192.168.2.116:7001,192.168.2.116:7002,192.168.2.116:7005";
        JedisClusterUtils jedisClusterUtils = new JedisClusterUtils(redisURLs);
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {
            System.out.println(jedisClusterUtils.isExists(i + "", ""));
        }
        long end = System.currentTimeMillis();
        System.out.println((end - begin) / 1000.0);
    }

    @Test
    public void t2() {
//        String redisURLs = "192.168.2.114:7000,192.168.2.114:7001,192.168.2.114:7002";
        String redisURLs = "192.168.2.116:7000,192.168.2.116:7001,192.168.2.116:7002,192.168.2.116:7005";
        JedisClusterUtils jedisClusterUtils = new JedisClusterUtils(redisURLs);
        JedisCluster jedis = jedisClusterUtils.getJedis();
        String key = "REDIRECT_UID";

        jedisClusterUtils.setStrVar(key, "111");
        System.out.println(jedisClusterUtils.getStrVar(key));

        jedis.set(key, "1383006803");
        System.out.println(jedis.get(key));

        try {
            jedis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void t3() {
//        JedisClusterUtils jedisClusterUtils = new JedisClusterUtils("192.168.2.116:7000,192.168.2.116:7001,192.168.2.116:7002,192.168.2.116:7005",null);
        JedisClusterUtils jedisClusterUtils = new JedisClusterUtils("192.168.2.234:6379","rediszcq");
        JedisCluster jedis = jedisClusterUtils.getJedis();

        String sina_uid = "sina_uid";
        boolean deduplication = jedisClusterUtils.deduplication(sina_uid, "4");
        System.out.println(deduplication);
        System.out.println(jedis.smembers(sina_uid));

        jedis.del(sina_uid);

        try {
            jedis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
