package com.tyaer.util.redis.single;

import com.tyaer.util.redis.dao.RedisSetDao;
import org.junit.After;
import org.junit.Test;

/**
 * Created by Twin on 2017/3/24.
 */
public class RedisSetDao_Test {

    RedisSetDao redisSetDao = new RedisSetDao("192.168.2.234",6379,"rediszcq");


    @Test
    public void a(){
//        System.out.println(redisSetDao.checkRepeat("user", "who"));
//        System.out.println(redisSetDao.checkRepeat("user", "liuling"));
//        System.out.println(redisSetDao.checkRepeat("zxc", "liuling"));
//        redisSetDao.del("sina_uid");
        System.out.println(redisSetDao.queryCount("sina_uid"));
        System.out.println(redisSetDao.queryCount("sina_uid_url"));
//        System.out.println(redisSetDao.saveData("sina_uid", "1866402485"));
//        redisSetDao.del("sina_uid_url");
//        System.out.println(redisSetDao.scanDatas("sina_uid"));
//        System.out.println(redisSetDao.queryCount("sina_uid_url"));
//        System.out.println(redisSetDao.queryCount("test_set"));
    }

    @Test
    public void save(){
        for (int i = 0; i < 100; i++) {
            System.out.println(i);
            redisSetDao.saveData("test_set",i+"");
        }
    }

    @Test
    public void deduplication(){
        for (int i = 0; i < 100000000; i++) {
            System.out.println(i);
            boolean b = redisSetDao.deduplication("sina_uid_url", "http://weibo.com/5151684920/EBTeacRmf?ref=feedsdk");
            System.out.println(b);
        }
    }

    @After
    public void after(){
        redisSetDao.closeJedisPool();
    }
}
