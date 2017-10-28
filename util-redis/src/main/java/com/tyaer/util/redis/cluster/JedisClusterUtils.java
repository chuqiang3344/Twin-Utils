package com.tyaer.util.redis.cluster;

import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.exceptions.JedisDataException;

import java.io.IOException;

/**
 * http://doc.redisfans.com/index.html
 */
public class JedisClusterUtils {
    protected static final Logger logger = Logger.getLogger(JedisClusterUtils.class);

    static JedisCluster jc = null;
    static int expireTime = 60 * 60 * 24 * 7;//7天
    String redisURLs;
    String passWord;

    public JedisClusterUtils(String redisURLs) {
        this.redisURLs = redisURLs;
    }

    public JedisClusterUtils(String redisURLs,String passWord) {
        this.redisURLs = redisURLs;
        this.passWord=passWord;
    }

    public JedisCluster getJedis() {
        if (jc == null) {
            synchronized (JedisClusterUtils.class) {
                if (jc == null) {
                    JedisClusterManager jcManager = new JedisClusterManager();
                    jc = jcManager.getRedisCluster(redisURLs,passWord);
                }
            }
        }
        return jc;
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
            logger.info("  redis can't work well ");
            e.printStackTrace();
            return false;
        }
    }

    public String getStrVar(String key) {
        JedisCluster jedis = getJedis();
        return jedis.get(key);
    }

    public boolean setStrVar(String key, String value) {
        try {
            JedisCluster jedis = getJedis();
            if (!jedis.exists(key)) {
                logger.info("update kv:" + key);
                jedis.set(key, value);
            } else {
                logger.info("insert kv:" + key);
                jedis.set(key, value);
            }
            return true;
        } catch (Exception e) {
            logger.info("  redis can't work well ");
            e.printStackTrace();
            return false;
        }
    }



    public boolean checkRepeat(String setName, String key) {
        JedisCluster jedis = getJedis();
        try {
            Boolean sismember = jedis.sismember(setName, key);
            return sismember;
        } catch (JedisDataException e) {
            logger.warn("请检查集合名称是否已存在，且非set类型！");
            e.printStackTrace();
        }finally {
//            jedis.close();
        }
        return false;
    }

    public boolean deduplication(String setName, String value) {
        if (!checkRepeat(setName, value)) {
            saveData(setName, value);
            return true;
        } else {
            return false;
        }
    }

    public Long saveData(String key, String data) {
        JedisCluster jedis = getJedis();//获取连接
        Long sadd = jedis.sadd(key, data);
        return sadd;
    }

    public void close() {
        if (jc != null) {
            try {
                jc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
