package com.tyaer.util.zookeeper.curator;

import com.tyaer.util.zookeeper.jp.SimpleZKClient;
import com.tyaer.util.zookeeper.jp.ZookeeperFactoryBean;
import org.apache.commons.io.FileUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by Twin on 2017/12/16.
 */
public class ZkCuratorTools {
    static CuratorFramework curatorFramework = null;

    static {
//        String zkhost = "192.168.157.100:2181";//zk的host
        String zkhost = "test12:2181,test13:2181";//zk的host
        RetryPolicy rp = new ExponentialBackoffRetry(1000, 3);//重试机制
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder().connectString(zkhost)
                .connectionTimeoutMs(5000)
                .sessionTimeoutMs(5000)
                .retryPolicy(rp);
//        builder.namespace(nameSpace);
        CuratorFramework zclient = builder.build();
        curatorFramework = zclient;
        curatorFramework.start();// 放在这前面执行
//        curatorFramework.newNamespaceAwareEnsurePath(nameSpace);
//        curatorFramework.new
//        zclient.newWatcherRemoveCuratorFramework();
    }

    public CuratorFramework getZKClient(String zk_address) throws IOException {
        if (null == curatorFramework) {
            synchronized (SimpleZKClient.class) {
                if (null == curatorFramework) {
//                    Map<String, String> props = initCfg();
                    int baseSleepTimeMs = 1000;
                    int maxRetries = 3;
                    int connectionTimeoutMs = 1000;
                    int sessionTimeoutMs = 1000;
                    curatorFramework = ZookeeperFactoryBean.createWithOptions(zk_address,
                            new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries), connectionTimeoutMs, sessionTimeoutMs);
                }
            }
        }

        return curatorFramework;
    }

    public static void main(String[] args) throws Exception {
        ZkCuratorTools zkCuratorTools = new ZkCuratorTools();
        //ct.getListChildren("/zk/bb");
        //ct.upload("/jianli/123.txt", "D:\\123.txt");
        //ct.createrOrUpdate("/zk/cc334/zzz","c");
        //ct.delete("/qinb/bb");
        //ct.checkExist("/zk");
//        String path = "/jianli/123.txt";
//        zkCuratorTools.checkExist(path);
//        zkCuratorTools.read(path);

        zkCuratorTools.createrOrUpdate("/testzcq/1.txt", "abcd");

        zkCuratorTools.getListChildren("/consumers/article/offsets/hanming_data");

        zkCuratorTools.close();
    }

    public void close(){
        curatorFramework.close();
    }

    /**
     * 创建或更新一个节点
     *
     * @param path    路径
     * @param content 内容
     **/
    public void createrOrUpdate(String path, String content) throws Exception {
//        curatorFramework.setData().forPath(path, content.getBytes());//直接这样也可以
        Stat stat = curatorFramework.checkExists().forPath(path);
        if (null == stat) {
            //节点不存在
            curatorFramework.create().creatingParentsIfNeeded().forPath(path, content.getBytes());
        } else {
            //节点已经存在，则将数据更新到节点中
            curatorFramework.setData().forPath(path, content.getBytes());
        }
        System.out.println("添加成功！！！");
    }

    /**
     * 删除zk节点
     *
     * @param path 删除节点的路径
     **/
    public void delete(String path) throws Exception {
        curatorFramework.delete().guaranteed().deletingChildrenIfNeeded().forPath(path);
        System.out.println("删除成功!");
    }

    /**
     * 判断路径是否存在
     *
     * @param path
     **/
    public void checkExist(String path) throws Exception {
        if (curatorFramework.checkExists().forPath(path) == null) {
            System.out.println("路径不存在!");
        } else {
            System.out.println("路径已经存在!");
        }
    }

    /**
     * 读取的路径
     *
     * @param path
     **/
    public void read(String path) throws Exception {
        String data = new String(curatorFramework.getData().forPath(path), "gbk");
        System.out.println("读取的数据:" + data);
    }

    /**
     * @param path 路径
     *             获取某个节点下的所有子文件
     */
    public void getListChildren(String path) throws Exception {
        List<String> paths = curatorFramework.getChildren().forPath(path);
        for (String p : paths) {
            System.out.println(p);
            read(path + "/" + p);
        }
    }

    /**
     * @param zkPath    zk上的路径
     * @param localpath 本地上的文件路径
     **/
    public void upload(String zkPath, String localpath) throws Exception {
        createrOrUpdate(zkPath, "");//创建路径
        byte[] bs = FileUtils.readFileToByteArray(new File(localpath));
        curatorFramework.setData().forPath(zkPath, bs);
        System.out.println("上传文件成功！");
    }
}
