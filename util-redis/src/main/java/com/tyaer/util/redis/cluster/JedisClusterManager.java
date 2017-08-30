package com.tyaer.util.redis.cluster;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.HashSet;
import java.util.Set;

public class JedisClusterManager {

    public JedisCluster getRedisCluster() {
        Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();

//		String redisURLs = Config.get(Config.KEY_REDIS_URL);
        String redisURLs = "";
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
