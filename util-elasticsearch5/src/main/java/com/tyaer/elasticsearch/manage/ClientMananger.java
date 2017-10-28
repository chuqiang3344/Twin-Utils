package com.tyaer.elasticsearch.manage;

import com.tyaer.elasticsearch.bean.ESConstants;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsRequest;
import org.elasticsearch.action.admin.cluster.stats.ClusterStatsResponse;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ClientMananger {
	static TransportClient client = null;

	public static TransportClient getClient() {
		if (client == null) {
			try {
				Settings settings = Settings.builder().put("cluster.name", ESConstants.ESClusterName).build();
				client = new PreBuiltTransportClient(settings);
//				Settings settings = Settings.settingsBuilder().put("cluster.name", ESConstants.ESClusterName)
//						.put("tclient.transport.sniff", true).build();
//				client = TransportClient.builder().settings(settings).build();
//				String es_host = "192.168.0.199:9300";
				client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("192.168.0.199"), 9300));

//				String [] ips = es_host.split(",");
//				for(String ip : ips){
//					String temp[] = ip.split(":");
//					if(temp.length!=2){
//						System.out.println("ES ip set worng ");
//					}
//					String host = temp[0];
//					int port = Integer.valueOf(temp[1]);
//					client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
//				}
			
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return client;
	}
	
	public static void main(String[] args) {

		TransportClient client2 = ClientMananger.getClient();
		System.out.println(client2);
		ClusterAdminClient cluster = client2.admin().cluster();
		ActionFuture<ClusterStatsResponse> stats = cluster.clusterStats(new ClusterStatsRequest());
		ClusterStatsResponse clusterStatsResponse = stats.actionGet();
		String name = clusterStatsResponse.getStatus().name();
		System.out.println(name);

//		ClientMananger t = new ClientMananger();
//		long begin = System.currentTimeMillis();
//		for(int i=0;i<10000;i++){
//		TransportClient client2 = ClientMananger.getClient();
//		if(client2!=null){
//		//	System.out.println(111);
//			//client2.close();
//		}else{
//			System.out.println("ok ");
//		}
//		}
//		long end = System.currentTimeMillis();
//
//		System.out.println(end -begin );
	}
}
