package com.tyaer.util.zookeeper.app;

import com.tyaer.util.zookeeper.jp.SimpleZKClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Twin on 2017/10/28.
 */
public class ZookeeperHelper {
    public static void main(String[] args) throws Exception {
//        getPartitionOffsetStatus("test12:2181,test13:2181","/consumers");
//        getPartitionOffsetStatus("test12:2181,test13:2181", "/consumers/zcq_test/offsets/topic_test");
        getPartitionOffsetStatus("test12:2181,test13:2181", "/consumers/article/offsets/hanming_data");
    }

    public static Map<Integer, Long> getPartitionOffsetStatus(String zk_address, String path) throws Exception {
////		String zkKafkaOffsetPath = SimpleZKClient.getCfg().get(SimpleZKClient.ZK_KAFKA_OFFSET_PATH);
//        String zkKafkaOffsetPath = "/consumers";
        CuratorFramework zkClient = SimpleZKClient.getZKClient(zk_address);
//        String zkPath = zkKafkaOffsetPath+"/"+group+"/offsets/"+topic;
        System.out.println("topic path:" + path);
        Stat stat = zkClient.checkExists().forPath(path);
        if (null == stat) {
            return null;
        }
        Map<Integer, Long> partitionOffsets = new HashMap<Integer, Long>();
        List<String> childPaths = zkClient.getChildren().forPath(path);
        long sum = 0L;
        for (String childPath : childPaths) {
//            System.out.println(childPath);
            String s = path + "/" + childPath;
            System.out.println(s);
            byte[] bytes = zkClient.getData().forPath(s);
            if (bytes != null) {
                String x = new String(bytes);
                System.out.println(x);
                try {
                    sum += Integer.valueOf(x);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        zkClient.close();
        System.out.println("###sum=" + sum);

        return partitionOffsets;
    }

    @Test
    public void t1(){
        try {
            System.out.println(getPartitionOffset("article", "hanming_data"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<Integer, Long> getPartitionOffset(String group, String topic) throws Exception {
//		String zkKafkaOffsetPath = SimpleZKClient.getCfg().get(SimpleZKClient.ZK_KAFKA_OFFSET_PATH);
        String zkKafkaOffsetPath = "/consumers";
        CuratorFramework zkClient = SimpleZKClient.getZKClient();
        String zkPath = zkKafkaOffsetPath + "/" + group + "/offsets/" + topic;
        System.out.println("topic path:" + zkPath);
        Stat stat = zkClient.checkExists().forPath(zkPath);
        if (null == stat) {
            return null;
        }
        Map<Integer, Long> partitionOffsets = new HashMap<Integer, Long>();
        List<String> childPaths = zkClient.getChildren().forPath(zkPath);
        for (String childPath : childPaths) {
            Integer partition = Integer.parseInt(childPath);
            childPath = zkPath + "/" + childPath;
            System.out.println("childPath:" + childPath);
            byte[] data = zkClient.getData().forPath(childPath);
            String pluginNodeStr = new String(data, Charset.forName("utf-8"));
            System.out.println(pluginNodeStr);
//			ByteArrayInputStream in = new ByteArrayInputStream(data);
//
//			DataInputStream oi = new DataInputStream(in);

            Long offset = Long.parseLong(pluginNodeStr);
            partitionOffsets.put(partition, offset);

        }


        return partitionOffsets;
    }


    public static void writeOffsetToZookeeper(String group, String topic, Map<Integer, Long> partitionOffsets) throws Exception {

//		String zkKafkaOffsetPath = SimpleZKClient.getCfg().get(SimpleZKClient.ZK_KAFKA_OFFSET_PATH);
        String zkKafkaOffsetPath = "/consumers";
        CuratorFramework zkClient = SimpleZKClient.getZKClient();
        for (Integer partition : partitionOffsets.keySet()) {
            Long offset = partitionOffsets.get(partition);
            String zkPath = zkKafkaOffsetPath + "/" + group + "/offsets/" + topic + "/" + partition;
            if (null != zkClient) {
                Stat stat = zkClient.checkExists().forPath(zkPath);
                if (null == stat) {
                    //节点不存在
                    zkClient.create().creatingParentsIfNeeded().forPath(zkPath, offset.toString().getBytes());
                } else {
                    //节点已经存在，则将数据更新到节点中
                    zkClient.setData().forPath(zkPath, offset.toString().getBytes());
                }
                System.out.println("offset updated:" + zkPath + ",offset:" + offset);
//                logger.info("register offset node" + zkPath + " success.");
            } else {
//                logger.warn("get zk client fail.");
            }

        }


    }
}
