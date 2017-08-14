package com.tyaer.util.redis.dao;

import com.tyaer.util.redis.single.RedisHelper;
import redis.clients.jedis.Jedis;

import java.util.Set;

/**
 * Created by Twin on 2017/3/24.
 */
public class RedisListDao extends RedisHelper {

    public RedisListDao(String redis_ip, int redis_port, String redis_psw) {
        super(redis_ip, redis_port, redis_psw);
    }

    public Set<String> scanDatas(String key) {
        return null;
    }


    /**
     * 查询redis队列的长度
     *
     * @param str_key
     */
    public long queryCount(String str_key) {
        Jedis jedis = getJedis();
        Long len = jedis.llen(str_key);
        return len;
    }

    /**
     * 清空redis队列
     *
     * @param str_key
     */
    public void emptyRedis(String str_key) {
        Jedis jedis = getJedis();
//        jedis.lrem(key, 0, null);
        long len = queryCount(str_key);
        for (int i = 0; i < len; i++) {
            jedis.lpop(str_key);
        }
    }
}
