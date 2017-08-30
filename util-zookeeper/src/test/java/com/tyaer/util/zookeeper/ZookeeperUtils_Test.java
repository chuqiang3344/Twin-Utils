package com.tyaer.util.zookeeper;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.junit.Test;

import java.io.IOException;

public class ZookeeperUtils_Test {
    @Test
    public void test1() throws IOException, InterruptedException, KeeperException {
        String CONNECTION_STRING = "192.168.157.100:2181";
        ZookeeperUtils zookeeperUtils = new ZookeeperUtils();
        ZooKeeper connection = zookeeperUtils.createConnection(CONNECTION_STRING, 10000, null);
        while (true){
            System.out.println(connection.exists("/disLocks", true));
            Thread.sleep(200);
        }
//        Thread.sleep(100000);
//        zookeeperUtils.releaseConnection(connection);
    }
}
