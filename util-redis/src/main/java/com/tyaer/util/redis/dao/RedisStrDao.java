package com.tyaer.util.redis.dao;

import com.tyaer.util.redis.single.RedisHelper;
import redis.clients.jedis.Jedis;

/**
 * Created by Twin on 2017/5/17.
 */
public class RedisStrDao extends RedisHelper{

    public RedisStrDao(String redis_ip, int redis_port, String redis_psw) {
        super(redis_ip, redis_port, redis_psw);
    }

    public String scanDatas(String key) {
        Jedis jedis = getJedis();//获取连接
        String value = jedis.get(key);
        jedis.close();//关闭连接
        return value;
    }

    public String saveData(String key, String value) {
        Jedis jedis = getJedis();//获取连接
        String set = jedis.set(key, value);
        jedis.close();//关闭连接
        return set;
    }
}
