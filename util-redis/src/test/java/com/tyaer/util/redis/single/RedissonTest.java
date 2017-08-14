package com.tyaer.util.redis.single;

import com.tyaer.util.redis.lock.RedissonUtils;
import org.junit.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;

import java.util.concurrent.TimeUnit;

/**
 * Created by wangnian on 2016/5/2.
 *博客地址：http://my.oschina.net/wangnian
 */
public class RedissonTest {

    @Test
    public void test() throws InterruptedException {
        //redisson配置
        Config config = new Config();
        SingleServerConfig singleSerververConfig = config.useSingleServer();
        singleSerververConfig.setAddress("192.168.2.234:6379");
        singleSerververConfig.setPassword("foobared");
        //redisson客户端
        RedissonClient redissonClient = RedissonUtils.getInstance().getRedisson(config);
        RBucket<Object> rBucket = RedissonUtils.getInstance().getRBucket(redissonClient, "key");
        //rBucket.set("wangnian");
        System.out.println(rBucket.get());

        while (true) {
            RLock lock = redissonClient.getLock("lock");
            lock.tryLock(0, 1, TimeUnit.SECONDS);//第一个参数代表等待时间，第二是代表超过时间释放锁，第三个代表设置的时间制
            try {
                System.out.println("执行");
            } finally {
                lock.unlock();
            }
        }
    }


}
