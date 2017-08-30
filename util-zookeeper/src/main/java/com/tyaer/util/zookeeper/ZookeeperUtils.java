package com.tyaer.util.zookeeper;

import com.tyaer.util.zookeeper.tbs.DistributedLock;
import org.apache.log4j.Logger;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.junit.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ZookeeperUtils {
    private static final Logger LOGGER = Logger.getLogger(DistributedLock.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        ZookeeperUtils zookeeperUtils = new ZookeeperUtils();
        String CONNECTION_STRING = "192.168.157.100:2181";
        ZooKeeper zooKeeper = zookeeperUtils.createConnection(CONNECTION_STRING, 10000, null);
        LOGGER.info("------------");
        try {
            String path = "/test";
//            zooKeeper.exists(path, true);
//            zooKeeper.delete(path,-1);
            zookeeperUtils.delete(zooKeeper, path, -1);
            zooKeeper.create(path, "test".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            zooKeeper.exists(path, true);
            zooKeeper.setData(path, "abc".getBytes(), -1);

            Stat stat = new Stat();
            System.out.println(zooKeeper.getData(path, true, stat));
            System.out.println(stat);

            zooKeeper.getChildren(path, true);
            zooKeeper.create(path + "/1", "test".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            System.out.println("--------------");
            zooKeeper.getChildren(path, true);
//            zooKeeper.exists(path + "/1", true);
            zooKeeper.delete(path + "/1",-1);

//            zooKeeper.create(path,"test".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

            zooKeeper.exists(path, true);
//            Thread.sleep(100000);
        } catch (KeeperException e) {
            e.printStackTrace();
        }

        zookeeperUtils.releaseConnection(zooKeeper);

    }

    @Test
    public void test() throws IOException, InterruptedException, KeeperException, NoSuchAlgorithmException {

        /**
         * 连接zookeeper
         */
        ZooKeeper zk = new ZooKeeper("localhost:2182", 5000, new Watcher() {
            public void process(WatchedEvent event) {
                String groupNode="/test";
                if (event.getType() == Event.EventType.NodeChildrenChanged && ("/" + groupNode).equals(event.getPath())) {
                    try {
                        System.out.println("此处监听");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        while(zk.getState() != ZooKeeper.States.CONNECTED ){
            Thread.sleep(3000);
        }
        //zk链接的用户
        zk.addAuthInfo("digest", "admin:admin123".getBytes());
        //创建开放节点，允许任意操作
        zk.create("/xxx", "xxx".getBytes("utf-8"), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        //创建只读节点
        zk.create("/yyy", "yyy".getBytes("utf-8"), ZooDefs.Ids.READ_ACL_UNSAFE, CreateMode.PERSISTENT);
        //创建者全部权限
        zk.create("/zzzs", "zzz".getBytes("utf-8"), ZooDefs.Ids.CREATOR_ALL_ACL, CreateMode.PERSISTENT);

        //设置访问权限列表
        /**
         * world  有独立id，anyone，代表任何用户。
         auth 不使用任何id，代表任何已经认证过的用户
         digest 之前使用了格式为username:pathasowrd的字符串来生成一个MD5哈希表作为ACL ID标识。在空文档中发送username:password来完成认证。现在的ACL表达式格式为username:base64, 用SHA1编码密码。
         ip 用客户端的ip作为ACL ID标识。ACL表达式的格式为addr/bits，addr中最有效的位匹配上主机ip最有效的位。
         */
        List<ACL> lists = new ArrayList<ACL>();
        Id id1 = new Id("digest", DigestAuthenticationProvider.generateDigest("admin:admin123"));
        Id id3 = new Id("digest", DigestAuthenticationProvider.generateDigest("readadmin:admin123"));
        lists.add(new ACL(ZooDefs.Perms.CREATE,id1));  //创建权限
        lists.add(new ACL(ZooDefs.Perms.READ,id3));	   //只读权限
        //testa 节点将根据权限列表进行acl
        zk.create("/testa", "testacl".getBytes("utf-8"), lists, CreateMode.PERSISTENT);


        byte[] value = zk.getData("/testa", null, new Stat());
        System.out.println(value);


    }

    private boolean delete(ZooKeeper zooKeeper, String path, int version) throws KeeperException, InterruptedException {
        LOGGER.info("delete path:" + path);
        Stat stat = zooKeeper.exists(path, true);
        if (stat == null) {
            return true;
        } else {
            if (stat.getNumChildren() == 0) {
                zooKeeper.delete(path, version);
            } else {
                List<String> children = zooKeeper.getChildren(path, true);
                for (String child : children) {
                    delete(zooKeeper, path + "/" + child, version);
                }
                zooKeeper.delete(path, version);
            }
        }
        return false;
    }

    //确保连接zk成功；
    private CountDownLatch connectedSemaphore = new CountDownLatch(1);

    /**
     * 创建ZK连接
     *
     * @param connectString  ZK服务器地址列表
     * @param sessionTimeout Session超时时间
     */
    public ZooKeeper createConnection(String connectString, int sessionTimeout, Watcher watcher) throws IOException, InterruptedException {
        ZooKeeper zk;
        if (watcher == null) {
            long timeInMillis = Calendar.getInstance().getTimeInMillis();
            zk = new ZooKeeper(connectString, sessionTimeout, new DefaultWatcher(timeInMillis));
        } else {
            zk = new ZooKeeper(connectString, sessionTimeout, watcher);
        }
        connectedSemaphore.await();
        return zk;
    }

    /**
     * 关闭ZK连接
     */
    public void releaseConnection(ZooKeeper zk) {
        if (zk != null) {
            try {
                zk.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        LOGGER.info("释放连接");
    }

    private class DefaultWatcher implements Watcher {
        long timeInMillis = Calendar.getInstance().getTimeInMillis();

        public DefaultWatcher(long timeInMillis) {
            this.timeInMillis = timeInMillis;
        }

        @Override
        public void process(WatchedEvent watchedEvent) {
            if (watchedEvent == null) {
                LOGGER.info(null);
                return;
            }
            LOGGER.info(watchedEvent);
            Event.KeeperState keeperState = watchedEvent.getState();
            if (Event.KeeperState.SyncConnected == keeperState) {
                if (Event.EventType.None == watchedEvent.getType()) {
                    LOGGER.info("成功连接上ZK服务器，耗时：" + (Calendar.getInstance().getTimeInMillis() - timeInMillis));
                    connectedSemaphore.countDown();
                } else {
                    String path = watchedEvent.getPath();
                    if (Event.EventType.NodeCreated == watchedEvent.getType()) {
                        LOGGER.info("###NodeCreated：" + path);
                    } else if (Event.EventType.NodeDeleted == watchedEvent.getType()) {
                        LOGGER.info("###NodeDeleted：" + path);
                    } else if (Event.EventType.NodeDataChanged == watchedEvent.getType()) {
                        LOGGER.info("###NodeDataChanged：" + path);
                    } else if (Event.EventType.NodeChildrenChanged == watchedEvent.getType()) {
                        LOGGER.info("###NodeChildrenChanged：" + path);
                    }
                }
            } else if (Event.KeeperState.Disconnected == keeperState) {
                LOGGER.warn("与ZK服务器断开连接");
            } else if (Event.KeeperState.AuthFailed == keeperState) {
                LOGGER.warn("权限检查失败");
            } else if (Event.KeeperState.Expired == keeperState) {
                LOGGER.warn("会话失效");
            }

        }
    }

    private class SustainWatcher implements Watcher {

        @Override
        public void process(WatchedEvent event) {

        }
    }
}
