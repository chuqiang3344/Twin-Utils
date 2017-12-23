package com.tyaer.util.zookeeper.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * Created by Twin on 2017/12/16.
 */
public class TestCuratorLock {
    /**
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        // TODO Auto-generated method stub
        CountDownLatch latch = new CountDownLatch(5);
//        String zookeeperConnectionString = "localhost:2181,localhost:2182,localhost:2183";
        String zookeeperConnectionString = "test12:2181,test13:2181";//zk的host
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.newClient(
                zookeeperConnectionString, retryPolicy);
        client.start();
        System.out.println("客户端启动。。。。");
        ExecutorService exec = Executors.newCachedThreadPool();
        for (int i = 0; i < 5; i++) {
            exec.submit(new MyLock("client" + i, client, latch));
        }
        exec.shutdown();
        latch.await();
        System.out.println("所有任务执行完毕");
        client.close();
        System.out.println("客户端关闭。。。。");
    }

    static class MyLock implements Runnable {
        private String name;
        private CuratorFramework client;
        private CountDownLatch latch;

        String path = "/test_group";

        public MyLock(String name, CuratorFramework client, CountDownLatch latch) {
            this.name = name;
            this.client = client;
            this.latch = latch;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            InterProcessMutex lock = new InterProcessMutex(client, path);
            try {
                if (lock.acquire(120, TimeUnit.SECONDS)) {
                    try {
                        // do some work inside of the critical section here
                        System.out.println("----------" + this.name
                                + "获得资源----------");
                        System.out.println("----------" + this.name
                                + "正在处理资源----------");
                        Thread.sleep(10 * 1000);
                        System.out.println("----------" + this.name
                                + "资源使用完毕----------");
                        latch.countDown();
                    } finally {
                        lock.release();
                        System.out.println("----------" + this.name
                                + "释放----------");
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
