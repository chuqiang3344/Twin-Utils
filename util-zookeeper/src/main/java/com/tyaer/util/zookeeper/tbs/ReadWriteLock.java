//package com.tyaer.util.zookeeper.tbs;
//
//import org.apache.curator.RetryPolicy;
//import org.apache.curator.framework.CuratorFramework;
//import org.apache.curator.framework.CuratorFrameworkFactory;
//import org.apache.curator.framework.recipes.locks.InterProcessMutex;
//import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
//import org.apache.curator.retry.ExponentialBackoffRetry;
//
//public class ReadWriteLock {
//
//    /**
//     * @param args
//     * @throws Exception
//     */
//    public static void main(String[] args) throws Exception {
//        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3);
//        CuratorFramework client = CuratorFrameworkFactory
//                .newClient("192.168.1.103:2181", retryPolicy);
//        client.start();
//
//        InterProcessReadWriteLock readWriteLock = new InterProcessReadWriteLock(client, "/read-write-lock");
//
//        //读锁
//        final InterProcessMutex readLock = readWriteLock.readLock();
//        //写锁
//        final InterProcessMutex writeLock = readWriteLock.writeLock();
//
//        try {
//            readLock.acquire();
//            System.out.println(Thread.currentThread() + "获取到读锁");
//
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        //在读锁没释放之前不能读取写锁。
//                        writeLock.acquire();
//                        System.out.println(Thread.currentThread() + "获取到写锁");
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    } finally {
//                        try {
//                            writeLock.release();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }).start();
//            //停顿3000毫秒不释放锁，这时其它线程可以获取读锁，却不能获取写锁。
//            Thread.sleep(3000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            readLock.release();
//        }
//
//        Thread.sleep(1000000);
//        client.close();
//    }
//
//}
