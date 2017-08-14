package com.tyaer.elasticsearch;

import com.tyaer.elasticsearch.bean.ESConstant;
import com.tyaer.elasticsearch.bean.Paginator;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CheckESStatus {
    private static Logger logger = Logger.getLogger(CheckESStatus.class);
    private static TransportClient transportClient;
    private static String[] availableNodes;

    public static TransportClient initClient(String[] nodes) {


        try {

            System.out.println("####### init client #####");


            //es 客户端设置
            Settings settings = Settings.settingsBuilder().put("cluster.name", "izhonghong")//设置集群名称
                    .put("tclient.transport.sniff", true).build();//自动嗅探整个集群的状态，把集群中其它机器的ip地址加到客户端中
            transportClient = TransportClient.builder().settings(settings).build();


            //将节点加入到客户端中
            for (String node : nodes) {
                //跳过为空的node.
                if (node.length() > 0) {
                    String host_port[] = node.split(":");
                    transportClient.addTransportAddress(
                            new InetSocketTransportAddress(InetAddress.getByName(host_port[0]),
                                    Integer.parseInt(host_port[1])));
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return transportClient;

    }


    public static void idsSearch(String orgId, String INDEX_AMS, String[] index_types, String[] ids, Paginator paginator, String... fields) throws Exception {
        SearchResponse searchResponse = null;
        SearchRequestBuilder searchRequest = null;
        StringBuffer result = new StringBuffer();
//	        Map<String,String> emotionMap = null;

//	        if("t_status_weibo".equals(index_type)&&null!=orgId&&(null==fields||Arrays.asList(fields).contains("emotion"))){     
//	        	 
//	         
//	        	emotionMap=hbaseAPI.getEmotions(orgId, Arrays.asList(ids)); 
//	        	System.out.println(emotionMap);
//	        }


        List<String> verifiedIds = new ArrayList<String>();
        for (String id : ids) {
            if (null != id) {
                verifiedIds.add(id);
            }
        }
        if (verifiedIds.size() == 0) {
            throw new Exception("ids can not be null,ids:" + ids);
        }
        String[] idArray = new String[verifiedIds.size()];
        verifiedIds.toArray(idArray);
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.idsQuery().ids(idArray)).must(QueryBuilders.existsQuery("text"));


        searchRequest = transportClient.prepareSearch(new String[]{INDEX_AMS});
        searchRequest.setTypes(index_types)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFetchSource(fields, null)
                .setQuery(queryBuilder) //采用filter查询，不计算结果。
        ;
        if (null != paginator) {
            searchRequest.setFrom(paginator.getFrom()).setSize(paginator.getSize());
        } else {
            searchRequest.setFrom(0).setSize(100);
        }


        // .setQuery(QueryBuilders.termQuery("content", "黑社交"))

        if (null != paginator && paginator.getSortOrder() != null) {
            String orderField = paginator.getField();
//						if("reports_count".equals(orderField)){
//							orderField = "reposts_count";
//						}
            searchRequest.addSort(orderField, paginator.getSortOrder());
        }

        //logger.info("searchRequest:"+searchRequest);

        searchResponse = searchRequest.execute().actionGet();//执行请求

        // 获取命中
        SearchHits hits = searchResponse.getHits();


        logger.info("hits:" + hits.getHits().length + ",took:" + searchResponse.getTook());


    }


    public static TransportClient refreshClient() {
        Properties props = new Properties();
        List<String> availableESServers = new ArrayList<String>();
        try {
//			  props = PropertiesLoaderUtils.loadAllProperties("elasticsearch.properties");
            props.load(CheckESStatus.class.getResourceAsStream("elasticsearch.properties"));
            String es_hosts = props.getProperty("es.hosts").trim();
            String nodes[] = es_hosts.split(",");

            for (String node : nodes) {
                TransportClient tempClient = null;
                try {
                    tempClient = initClient(new String[]{node});

                    idsSearch(null, ESConstant.INDEX_AMS.toString(), new String[]{"t_status_weibo", "t_article"}, new String[]{"fbf42017011120010_4062823561138890"}, null, null);
                    availableESServers.add(node);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (null != tempClient) {
                        tempClient.close();
                    }
                }

            }


        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        TransportClient tempClient = null;
        try {

            availableNodes = new String[availableESServers.size()];
            int i = 0;
            for (String availableNode : availableESServers) {
                availableNodes[i] = availableNode;
                i++;
            }
            logger.info("availableESServers###:" + getAvailableNodes());
            if (availableNodes.length > 0) {
                tempClient = initClient(availableNodes);
                idsSearch(null, ESConstant.INDEX_AMS.toString(), new String[]{"t_status_weibo", "t_article"}, new String[]{"a1b4203205_bd4dc512a34e0b5bc3ced94e771d9d8c"}, null, null);
                logger.info("double check is OK!");
                return transportClient;
            } else {

                logger.warn("no available ES　Nodes");
                return null;
            }


        } catch (Exception e) {
            if (null != tempClient) {
                tempClient.close();
            }
            logger.warn("double check is ERROR!");
            return null;
        }
    }

    public static String getAvailableNodes() {
        StringBuilder nodes = new StringBuilder();
        if (null != availableNodes && availableNodes.length > 0) {

            for (String node : availableNodes) {
                if (nodes.length() > 0) {
                    nodes.append(",");
                }
                nodes.append(node);
            }
        }
        return nodes.toString();
    }

    public static void main(String[] args) {


        refreshClient();

    }


}
