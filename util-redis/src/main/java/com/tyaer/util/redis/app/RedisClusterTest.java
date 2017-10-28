package com.tyaer.util.redis.app;

import com.tyaer.util.redis.cluster.JedisClusterUtils;
import redis.clients.jedis.JedisCluster;

import java.io.IOException;

/**
 * Created by Twin on 2017/10/25.
 */
public class RedisClusterTest {
    public static void main(String[] args) {
        String password = null;
        if (args.length == 2) {
            password = args[1];
        }
        JedisClusterUtils jedisClusterUtils = new JedisClusterUtils(args[0], password);
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
