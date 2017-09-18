package com.tyaer.util.redis.cluster;

import com.tyaer.util.redis.single.RedisHelper;
import org.apache.log4j.Logger;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.Set;

public class JedisClusterManager {
    protected static final Logger LOGGER = Logger.getLogger(JedisClusterManager.class);

    public JedisCluster getRedisCluster(String redisURLs) {
        Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
//		String redisURLs = Config.get(Config.KEY_REDIS_URL);
//        String redisURLs = "";
        String[] redisURLArray = redisURLs.split("\\,");
        for (String redisURL : redisURLArray) {
            String ip = redisURL.split(":")[0];
            String port = redisURL.split(":")[1];
            jedisClusterNodes.add(new HostAndPort(ip, Integer.parseInt(port)));
        }
//		jedisClusterNodes.add(new HostAndPort("10.248.161.7", 7000));
//		jedisClusterNodes.add(new HostAndPort("10.248.161.7", 7001));
//		jedisClusterNodes.add(new HostAndPort("10.248.161.7", 7002));
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        JedisCluster jedisCluster = new JedisCluster(jedisClusterNodes, 1000, 10,jedisPoolConfig);
        LOGGER.info("jedisCluster连接成功！");

        return jedisCluster;
    }
}
