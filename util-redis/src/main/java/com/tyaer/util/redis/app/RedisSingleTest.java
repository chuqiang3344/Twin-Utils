package com.tyaer.util.redis.app;

import com.tyaer.util.redis.dao.RedisStrDao;

/**
 * Created by Twin on 2017/5/17.
 */
public class RedisSingleTest {

    public static void main(String[] args) {
        RedisStrDao redisSetDao = new RedisStrDao(args[0],Integer.valueOf(args[1]),args[2]);

        System.out.println(redisSetDao.saveData("test", "321"));
        String x = redisSetDao.scanDatas("test");
        System.out.println(x);
        System.out.println(redisSetDao.scanDatas("REDIRECT_UID"));
//        redisSetDao.saveData("REDIRECT_UID","1000000000");
        System.out.println(redisSetDao.scanDatas("REDIRECT_UID"));

        redisSetDao.closeJedisPool();
    }

}
