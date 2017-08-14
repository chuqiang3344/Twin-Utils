package com.tyaer.elasticsearch;

import com.tyaer.elasticsearch.bean.ESConstants;
import org.apache.log4j.Logger;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class EsClientMananger {
    private static final Logger logger = Logger.getLogger(EsClientMananger.class);
    static String es_hosts = "192.168.2.116:9300,192.168.2.115:9300,192.168.2.116:9400";
    static TransportClient transportClient = null;

    static {
        getEsClient();
        refreshClient();
    }

    public static TransportClient getEsClient() {
        if (transportClient == null) {
            try {
                Settings settings = Settings.settingsBuilder().put("cluster.name", ESConstants.ESClusterName)
                        .put("tclient.transport.sniff", true).build();//自动嗅探整个集群的状态，把集群中其它机器的ip地址加到客户端中
                transportClient = TransportClient.builder().settings(settings).build();
//				String [] ips = Config.get(Config.KEY_ES_HOST).split(",");
                String[] ips = es_hosts.split(",");
                for (String ip : ips) {
                    String temp[] = ip.split(":");
                    if (temp.length != 2) {
                        System.out.println("ES ip set worng ");
                    }
                    String host = temp[0];
                    int port = Integer.valueOf(temp[1]);
                    transportClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        return transportClient;
    }

    public static void refreshClient() {
        new Thread() {
            String oldNodes = null;
            @Override
            public void run() {
                while (true) {
                    try {
                        TransportClient client = CheckESStatus.refreshClient();
                        if (null != client) {
                            String availableNodes = CheckESStatus.getAvailableNodes();
                            if (!availableNodes.equals(oldNodes)) {
                                TransportClient oldClient = transportClient;
                                transportClient = client;
                                Thread.sleep(5000);
                                oldClient.close();
                                logger.info("##es nodes change,oldNodes:" + oldNodes + ",newNodes:" + availableNodes);
                                oldNodes = availableNodes;
                            } else {
                                client.close();
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(120 * 1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }

        }.start();
    }

    public static void main(String[] args) {
        EsClientMananger t = new EsClientMananger();
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            TransportClient client2 = EsClientMananger.getEsClient();
            if (client2 != null) {
                //	System.out.println(111);
                //client2.close();
            } else {
                System.out.println("ok ");
            }
        }
        long end = System.currentTimeMillis();

        System.out.println(end - begin);
    }
}
