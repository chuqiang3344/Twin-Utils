package com.tyaer.util.redis.single;

import com.tyaer.util.redis.dao.RedisListDao;
import org.junit.After;
import org.junit.Test;

/**
 * Created by Twin on 2017/3/24.
 */
public class RedisListDao_Test {

    RedisListDao redisListDao = new RedisListDao("192.168.2.234",6379,"rediszcq");


    @Test
    public void a(){
//        System.out.println(redisListDao.queryCount("user"));
    }

    @After
    public void after(){
        redisListDao.closeJedisPool();
    }
}
