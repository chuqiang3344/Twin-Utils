package com.tyaer.util.redis.single;

import com.tyaer.util.redis.dao.RedisStrDao;

/**
 * Created by Twin on 2017/5/17.
 */
public class RedisStrDao_Test {
    static RedisStrDao redisSetDao = new RedisStrDao("192.168.2.234",6379,"rediszcq");

    public static void main(String[] args) {
        System.out.println(redisSetDao.saveData("test", "321"));
        String x = redisSetDao.scanDatas("test");
        System.out.println(x);

        System.out.println(redisSetDao.scanDatas("REDIRECT_UID"));
        redisSetDao.saveData("REDIRECT_UID","1000000000");
        System.out.println(redisSetDao.scanDatas("REDIRECT_UID"));


        redisSetDao.closeJedisPool();
    }

}
