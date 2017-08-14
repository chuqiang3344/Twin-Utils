package com.tyaer.util.redis.dao;

import com.tyaer.util.redis.single.RedisHelper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisDataException;

import java.util.List;
import java.util.Set;

/**
 * Created by Twin on 2017/3/24.
 */
public class RedisSetDao extends RedisHelper {

    public RedisSetDao(String redis_ip, int redis_port, String redis_psw) {
        super(redis_ip, redis_port, redis_psw);
    }


    public Set<String> scanDatas(String key) {
        Jedis jedis = getJedis();//获取连接
        Set<String> smembers = jedis.smembers(key);
        jedis.close();//关闭连接
        return smembers;
    }

    public Long saveData(String key, String data) {
        Jedis jedis = getJedis();//获取连接
        Long sadd = jedis.sadd(key, data);
        jedis.close();//关闭连接
        return sadd;
    }

    public Long saveData(String key, List<String> dataList) {
        if (dataList != null && dataList.size() > 0) {
            Jedis jedis = getJedis();
            String[] array = dataList.toArray(new String[dataList.size()]);
            Long sadd = jedis.sadd(key, array);
            jedis.close();
            return sadd;
        } else {
            return 0L;
        }
    }

    public boolean checkRepeat(String setName, String key) {
        Jedis jedis = getJedis();
        try {
            Boolean sismember = jedis.sismember(setName, key);
            return sismember;
        } catch (JedisDataException e) {
            LOGGER.warn("请检查集合名称是否已存在，且非set类型！");
        }finally {
            jedis.close();
        }
        return false;
    }

    public boolean deduplication(String setName, String key) {
        if (!checkRepeat(setName, key)) {
            saveData(setName, key);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 查询redis队列的长度
     *
     * @param str_key
     */
    public long queryCount(String str_key) {
        Jedis jedis = getJedis();
        Long len = jedis.scard(str_key);
        jedis.close();
        return len;
    }

}
