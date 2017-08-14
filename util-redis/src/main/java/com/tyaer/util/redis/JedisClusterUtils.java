package com.tyaer.util.redis;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.HashSet;
import java.util.Set;

import static com.tyaer.util.redis.JedisClusterUtils.redis_url;


public class JedisClusterUtils {
    static String redis_url = "10.248.161.7:7000,10.248.161.7:7001,10.248.161.7:7002";
    static JedisCluster jc = null;
    static int expireTime = 60 * 60 * 24 * 7;

    public static boolean isExists(String key, String value) {
        try {
            JedisCluster jedis = getJedis();
            if (!jedis.exists(key)) {
                jedis.setex(key, expireTime, value);
                return false;
            } else {
                jedis.expire(key, expireTime);
                return true;
            }
        } catch (Exception e) {
            System.out.println("  redis can't work well ");
            e.printStackTrace();
            return false;
        }
    }

    public static JedisCluster getJedis() {
        if (jc == null) {
            synchronized (JedisClusterUtils.class) {
                if (jc == null) {
                    JedisClusterManager jcManager = new JedisClusterManager();
                    jc = jcManager.getRedisCluster();
                }
            }

        }
        return jc;
    }

    public static void main(String[] args) {
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {


            System.out.println(JedisClusterUtils.isExists(i + "", ""));
        }
        long end = System.currentTimeMillis();
        System.out.println((end - begin) / 1000.0);
    }
}

class JedisClusterManager {
    public JedisCluster getRedisCluster() {
        Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();

//        String redisURLs = Config.get(Config.KEY_REDIS_URL);
        String redisURLs = redis_url;
        String[] redisURLArray = redisURLs.split("\\,");
        for (String redisURL : redisURLArray) {
            String ip = redisURL.split(":")[0];
            String port = redisURL.split(":")[1];
            jedisClusterNodes.add(new HostAndPort(ip, Integer.parseInt(port)));
        }
//		jedisClusterNodes.add(new HostAndPort("10.248.161.7", 7000));
//		jedisClusterNodes.add(new HostAndPort("10.248.161.7", 7001));
//		jedisClusterNodes.add(new HostAndPort("10.248.161.7", 7002));


        return new JedisCluster(jedisClusterNodes, 1000, 10);
    }
}