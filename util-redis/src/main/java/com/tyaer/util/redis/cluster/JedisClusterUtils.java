package com.tyaer.util.redis.cluster;

import redis.clients.jedis.JedisCluster;


public class JedisClusterUtils {

    static JedisCluster jc = null;
    static int expireTime = 60 * 60 * 24 * 7;//7天
    String redisURLs;

    public JedisClusterUtils(String redisURLs) {
        this.redisURLs = redisURLs;
    }

    public boolean isExists(String key, String value) {
        try {
            JedisCluster jedis = getJedis();
            if (!jedis.exists(key)) {
                jedis.setex(key, expireTime, value);
                return false;
            } else {
                jedis.expire(key, expireTime);//设置键key的过期时间
                return true;
            }
        } catch (Exception e) {
            System.out.println("  redis can't work well ");
            e.printStackTrace();
            return false;
        }
    }

    public JedisCluster getJedis() {
        if (jc == null) {
            synchronized (JedisClusterUtils.class) {
                if (jc == null) {
                    JedisClusterManager jcManager = new JedisClusterManager();
                    jc = jcManager.getRedisCluster(redisURLs);
                }
            }

        }
        return jc;
    }

    public static void main(String[] args) {
        JedisClusterUtils jedisClusterUtils=new JedisClusterUtils("192.168.2.114:7000,192.168.2.114:7001,192.168.2.114:7002");
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {
            System.out.println(jedisClusterUtils.isExists(i + "", ""));
        }
        long end = System.currentTimeMillis();
        System.out.println((end - begin) / 1000.0);
    }
}
