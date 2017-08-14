package com.tyaer.util.redis.single;

import com.tyaer.util.redis.lock.RedissonUtils;
import org.junit.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import redis.clients.jedis.Jedis;

import java.util.concurrent.TimeUnit;

/**
 * Created by Twin on 2017/1/4.
 */
public class FbssTest extends Thread {
//    private static RedissonClient redissonClient;

    static {
        //redisson配置
        Config config = new Config();
        SingleServerConfig singleSerververConfig = config.useSingleServer();
        singleSerververConfig.setAddress("192.168.2.234:6379");
        singleSerververConfig.setPassword("foobared");
        //redisson客户端
//        redissonClient = RedissonUtils.getInstance().getRedisson(config);

//        RBucket<Object> rBucket = RedissonUtils.getInstance().getRBucket(redissonClient, "key");
//        rBucket.set("wangnian");
//        System.out.println(rBucket.get());
    }

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 2; i++) {
            new FbssTest().start();
        }
    }

    @Test
    public void insert(){

    }

    @Override
    public void run() {
        fbs();
//        tbs();
//        bjs();
    }

    private void fbs() {
        //redisson配置
        Config config = new Config();
        SingleServerConfig singleSerververConfig = config.useSingleServer();
        singleSerververConfig.setAddress("192.168.2.234:6379");
        singleSerververConfig.setPassword("rediszcq");
        RedissonClient redissonClient = RedissonUtils.getInstance().getRedisson(config);
        System.out.println(Thread.currentThread().getName() + " " + Thread.currentThread().getId());
        for (int i = 0; i < 100; i++) {
            RLock lock = redissonClient.getLock("lock");
            try {
                lock.tryLock(1, 5, TimeUnit.SECONDS);//第一个参数代表等待时间，第二是代表超过时间释放锁，第三个代表设置的时间制
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                Jedis jedis = RedisPool.getJedis();
                System.out.println(Thread.currentThread().getName() + " " + jedis.lpop("zxc"));
            } finally {
                lock.unlock();
            }
        }
    }

    String lock="lock";
    private void tbs() {
        System.out.println(Thread.currentThread().getName() + " " + Thread.currentThread().getId());
        Jedis jedis = RedisPool.getJedis();
        for (int i = 0; i < 100; i++) {
            synchronized (lock) {
                System.out.println(Thread.currentThread().getName() + " " + jedis.lpop("zxc"));
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        }
    }

    private void bjs() {
        System.out.println(Thread.currentThread().getName() + " " + Thread.currentThread().getId());
        Jedis jedis = RedisPool.getJedis();
        for (int i = 0; i < 100; i++) {
            System.out.println(Thread.currentThread().getName() + " " + jedis.lpop("zxc"));
        }
    }
}
