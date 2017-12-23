package com.tyaer.elasticsearch.manage;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsResponse;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkItemResponse.Failure;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptService;
import org.elasticsearch.search.SearchHits;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public class ElasticSearchHelper {
    private static final Logger logger = Logger.getLogger(ElasticSearchHelper.class);
    private ArrayBlockingQueue<UpdateRequestBuilder> arrayBlockingQueue = new ArrayBlockingQueue(8192);
    private EsClientMananger esClientMananger;

    public ElasticSearchHelper(String es_hosts, String esClusterName) {
        esClientMananger = new EsClientMananger(es_hosts, esClusterName);
        new Thread(() -> {
            while (esClientMananger.REFRESH_CLIENT_SWITCH && arrayBlockingQueue.size() == 0) {
                try {
                    UpdateRequestBuilder updateRequestBuilder = arrayBlockingQueue.take();
                    TransportClient client = esClientMananger.getEsClient();
                    int retryNum = 5;
                    boolean isOk = false;
                    while (!isOk && retryNum > 0) {
                        BulkRequestBuilder bulkRequest = client.prepareBulk();
                        bulkRequest.add(updateRequestBuilder);
                        BulkResponse bulkResponse = bulkRequest.get();
                        logger.info("retryNum:" + retryNum + " retry id cause==>:" + bulkResponse.getItems()[0].getId());
                        if (bulkResponse.hasFailures()) {
                            for (BulkItemResponse itemResponse : bulkResponse.getItems()) {
                                if (itemResponse.isFailed()) {
                                    Failure failure = itemResponse.getFailure();
                                    logger.error("retry failure cause==>>>> " + failure.getCause());
                                    retryNum--;
                                }
                            }
                        } else {
                            isOk = true;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static UpdateRequestBuilder changeToBuilder(BulkRequestBuilder bulkRequest, TransportClient client, String index,
                                                        String type, Map<String, Object> obj) {
        try {
            String _id = obj.get("_id") + "";
            obj.remove("_id");
            Map<String, String> map = new HashMap();
            for (String key : obj.keySet()) {
                Object o = obj.get(key);
                String value;
                if (o != null) {
                    value = o + "";
                } else {
                    value = null;
                }
//                if (StringUtils.isNotBlank(value) && !"null".equals(value)) {
                map.put(key, value);
//                }
            }
            return client.prepareUpdate(index, type, _id).setDoc(map).setUpsert(map);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getter(Object obj, String att) {
        try {
            Method method = obj.getClass().getMethod("get" + att);
            String value = method.invoke(obj) + "";
            return value;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private UpdateRequestBuilder changeToBuilder(BulkRequestBuilder bulkRequest, TransportClient client, String index,
                                                 String type, Object obj) {
        try {
            Class<?> demo = obj.getClass();
            Method method[] = demo.getMethods();
            String key = obj.getClass().getMethod("getKey").invoke(obj) + "";
            //System.out.println("upsert to es:"+key);


            Map<String, String> map = new HashMap();
            for (int i = 0; i < method.length; ++i) {
                String name = method[i].getName();
                if (name.startsWith("get") && (!"getClass".equals(name)) && (!"getKey".equals(name))) {
                    name = name.substring(3);
                    String value = getter(obj, name);
                    if (null != value && !"".equals(value.trim()) && !"null".equals(value)) {
                        name = name.toLowerCase();
                        map.put(name, value);
                    }
                }
            }
            return client.prepareUpdate(index, type, key).setDoc(map).setUpsert(map);
        } catch (Exception e) {
            System.out.println("changed!!!!!");
            e.printStackTrace();
            return null;
        }
    }

    public void BulkIndex(String index, String type, List lists) {
        TransportClient client = esClientMananger.getEsClient();
        try {
            ArrayList<UpdateRequestBuilder> updateRequestBuilders = new ArrayList<>();
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            for (int i = 0; i < lists.size(); i++) {
                Object obj = lists.get(i);
                if (obj == null) {
                    continue;
                }
                UpdateRequestBuilder request = changeToBuilder(bulkRequest, client, index, type, obj);
                bulkRequest.add(request);
                updateRequestBuilders.add(request);
            }
            BulkResponse bulkResponse = bulkRequest.get();
            if (bulkResponse.hasFailures()) {
                BulkItemResponse[] items = bulkResponse.getItems();
                for (int i = 0; i < items.length; i++) {
                    BulkItemResponse itemResponse = items[i];
                    if (itemResponse.isFailed()) {
                        Failure failure = itemResponse.getFailure();
                        Throwable cause = failure.getCause();
                        logger.error(i + " cause==>>>> " + cause);
//                        if (!cause.toString().contains("AlreadyExistsException")) {
//                        UpdateRequestBuilder requestBuilder = updateRequestBuilders.get(i);
//                        boolean add = arrayBlockingQueue.add(requestBuilder);
//                        logger.info("arrayBlockingQueue.size():" + arrayBlockingQueue.size());
//                        if (!add) {
//                            arrayBlockingQueue.put(requestBuilder);
//                        }
//                        }
                    }
                }
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                for (BulkItemResponse bulkItemResponse : bulkResponse.getItems()) {
                    String id = bulkItemResponse.getId();
                    stringBuilder.append(id).append(",");
                }
                logger.info("存入ES成功:" + stringBuilder);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(index + " ==>>  type : " + type + "  size: " + lists.size());
        }
    }

    // ---------------------- 索引相关 -----------------------

    public void delete(String index_name, String index_type, String... ids) {
        TransportClient transportClient = esClientMananger.getEsClient();
        BulkRequestBuilder bulkRequest = transportClient.prepareBulk();
        BulkResponse bulkResponse = null;
        String json_result = "";
        try {
            if (true) {
                //构建批量删除请求.
                for (String id : ids) {
                    DeleteRequestBuilder deleteBuilder = transportClient.prepareDelete(index_name, index_type, id);

                    bulkRequest.add(deleteBuilder);

                }

                // 批量删除.
                bulkResponse = bulkRequest.execute().actionGet();

                // 批量错误信息.
                if (bulkResponse.hasFailures()) {
                    // 获取错误索引列表信息.
                    String failure_indices = "";
                    for (BulkItemResponse itemResponse : bulkResponse.getItems()) {
                        failure_indices += itemResponse.getFailure().getIndex() + ",";
                    }

                } else {

                }

                // 释放请求.
                bulkRequest.request().requests().clear();//释放请求.
                bulkRequest.setTimeout(TimeValue.timeValueMinutes(20000)); //设置请求超时时间
            }
            System.out.println("delete result:" + json_result);

        } catch (ElasticsearchException e) {
            e.printStackTrace();
        }
    }


    /**
     * @param INDEX_AMS  索引名;
     * @param index_type 索引类型;
     * @return 更新失败或者成功.
     * @desc 更新索引字段信息.
     */
    public boolean delete(String INDEX_AMS, String index_type, String index_id, String field) {
        TransportClient transportClient = esClientMananger.getEsClient();
        transportClient.prepareUpdate(INDEX_AMS, index_type, index_id).setScript(new Script("ctx._source.remove(\"" + field + "\")", ScriptService.ScriptType.INLINE, null, null)).get();
        return true;
    }

    public void bulkDelete(String index_name, String index_type, String field, String... ids) {
        TransportClient transportClient = esClientMananger.getEsClient();
        BulkRequestBuilder bulkRequest = transportClient.prepareBulk();
        BulkResponse bulkResponse = null;
        String json_result = "";
        try {
            if (true) {
                //构建批量删除请求.
                for (String id : ids) {
//                    DeleteRequestBuilder deleteBuilder = transportClient.prepareDelete(index_name, index_type, id);
                    UpdateRequestBuilder deleteBuilder = transportClient.prepareUpdate(index_name, index_type, id).setScript(new Script("ctx._source.remove(\"" + field + "\")", ScriptService.ScriptType.INLINE, null, null));
                    bulkRequest.add(deleteBuilder);
                }

                // 批量删除.
                bulkResponse = bulkRequest.execute().actionGet();

                // 批量错误信息.
                if (bulkResponse.hasFailures()) {
                    // 获取错误索引列表信息.
                    String failure_indices = "";
                    for (BulkItemResponse itemResponse : bulkResponse.getItems()) {
                        failure_indices += itemResponse.getFailure().getIndex() + ",";
                    }

                } else {

                }

                // 释放请求.
                bulkRequest.request().requests().clear();//释放请求.
                bulkRequest.setTimeout(TimeValue.timeValueMinutes(20000)); //设置请求超时时间
            }
            System.out.println("delete result:" + json_result);

        } catch (ElasticsearchException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return 索引列表;
     * @desc 获取所有索引.
     */
    public List<String> getIndicies() {
        TransportClient transportClient = esClientMananger.getEsClient();
        String indices[] = null;
        try {
            ClusterStateResponse clusterStateResponse = transportClient.admin().cluster().prepareState()
                    .execute().actionGet();
            indices = clusterStateResponse.getState().getMetaData().getConcreteAllIndices();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Arrays.asList(indices);
    }

    /**
     * @param indexName 索引名;
     * @return true or false;
     * @desc 判断某个索引是否存在
     */
    public boolean isExistsIndex(String indexName) {
        TransportClient transportClient = esClientMananger.getEsClient();
        IndicesExistsResponse indicesExistsResponse = null;
        try {
            indicesExistsResponse = transportClient.admin().indices()
                    .exists(new IndicesExistsRequest().indices(new String[]{indexName})).actionGet();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return indicesExistsResponse.isExists();
    }

    /**
     * @param indexName 索引名;
     * @param indexType 索引类型;
     * @return true or false;
     * @desc 判断索引类型是否存在.
     */
    public boolean isExistsType(String indexName, String indexType) {
        TransportClient transportClient = esClientMananger.getEsClient();
        TypesExistsResponse response = null;
        response = transportClient.admin().indices()
                .typesExists(new TypesExistsRequest(new String[]{indexName}, indexType)
                ).actionGet();

        return response.isExists();
    }

    private XContentBuilder jsonBuilder() {
        try {
            return XContentFactory.jsonBuilder();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    private UpdateRequestBuilder changeToBuilderForUser(BulkRequestBuilder bulkRequest, TransportClient client,
                                                        String index, String type, Object obj) {
        try {
            Class<?> demo = obj.getClass();
            Method method[] = demo.getMethods();
            String key = obj.getClass().getMethod("getKey").invoke(obj) + "";
            Map<String, String> map = new HashMap();
            for (int i = 0; i < method.length; ++i) {
                String name = method[i].getName();
                if (name.startsWith("get") && (!"getClass".equals(name)) && (!"getKey".equals(name))) {
                    name = name.substring(3);
                    String value = getter(obj, name);
                    if (!"null".equals(value)) {
                        name = name.toLowerCase();
                        map.put(name, value);
                    }
                }
            }
            return client.prepareUpdate(index, type, key).setDoc(map).setUpsert(map).setRouting(key);
        } catch (Exception e) {
            System.out.println("changed!!!!!");
            e.printStackTrace();
            return null;
        }
    }

    public void BulkIndexForUser(String index, String type, List<Object> lists) {
        TransportClient client = esClientMananger.getEsClient();
        try {

            BulkRequestBuilder bulkRequest = client.prepareBulk();
            for (int i = 0; i < lists.size(); i++) {
                Object obj = lists.get(i);
                if (obj == null) {
                    continue;
                }
                bulkRequest.add(changeToBuilderForUser(bulkRequest, client, index, type, obj));
            }
            BulkResponse bulkResponse = bulkRequest.get();
            if (bulkResponse.hasFailures()) {
                for (BulkItemResponse itemResponse : bulkResponse.getItems()) {

                    Failure failure = itemResponse.getFailure();
                    if (failure != null) {
                        System.err.println(" cause : " + failure.getCause());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(index + " ==>>  type : " + type + "  size: " + lists.size());
        }
    }

    public void createIndex(String index, String type, String id, Object bean) {
        TransportClient client = esClientMananger.getEsClient();
        // System.out.println(client != null);
        try {
            JSONObject jsonObj1 = (JSONObject) JSON.toJSON(bean);
            IndexResponse response = client.prepareIndex(index, type, id).setSource(jsonObj1).execute().actionGet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param INDEX_AMS  索引名;
     * @param index_type 索引类型;
     * @param qualifiers kv形式的键值对;
     * @return 更新失败或者成功.
     * @desc 更新索引字段信息.
     */
    public boolean update(String INDEX_AMS, String index_type, String index_id, Map<String, Object> qualifiers) {
        TransportClient transportClient = esClientMananger.getEsClient();
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.index(INDEX_AMS).type(index_type).id(index_id).doc(qualifiers).refresh(true);
        transportClient.update(updateRequest).actionGet();
        //flushRequest.立即刷新
        FlushRequest flushRequest = new FlushRequest(INDEX_AMS);
        flushRequest.force(true);
        transportClient.admin().indices().flush(flushRequest).actionGet();
        return true;
    }

    public void update1(String index, String type, String id, Map<String, String> map) {
        TransportClient client = esClientMananger.getEsClient();
        try {
            client.prepareUpdate(index, type, id).setDoc(map).get();
        } catch (Exception e) {
            System.err.println("update wrong " + index + " " + id);
            e.printStackTrace();
        }
    }

    /**
     * 批量更新
     *
     * @param index
     * @param type
     * @param lists
     */
    public void batchUpdate(String index, String type, List<Map<String, Object>> lists) {
        TransportClient client = esClientMananger.getEsClient();
        try {
            ArrayList<UpdateRequestBuilder> updateRequestBuilders = new ArrayList<>();
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            for (Map<String, Object> map : lists) {
                if (map == null) {
                    continue;
                }
                UpdateRequestBuilder request = changeToBuilder(bulkRequest, client, index, type, map);
                bulkRequest.add(request);
                updateRequestBuilders.add(request);
            }
            BulkResponse bulkResponse = bulkRequest.get();
            BulkItemResponse[] items = bulkResponse.getItems();
//            if (bulkResponse.hasFailures()) {
            ArrayList<Object> list = new ArrayList<>();
            for (int i = 0; i < items.length; i++) {
                BulkItemResponse itemResponse = items[i];
                if (itemResponse.isFailed()) {
                    Failure failure = itemResponse.getFailure();
                    Throwable cause = failure.getCause();
                    logger.error(i + " cause==>>>> " + cause);
//                        if (!cause.toString().contains("AlreadyExistsException")) {
                    UpdateRequestBuilder requestBuilder = updateRequestBuilders.get(i);
                    boolean add = arrayBlockingQueue.add(requestBuilder);
                    logger.info("arrayBlockingQueue.size():" + arrayBlockingQueue.size());
                    if (!add) {
                        arrayBlockingQueue.put(requestBuilder);
                    }
//                        }
                } else {
                    list.add(itemResponse.getId());
                }
            }
//            }
            logger.info("update success:" + list);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(index + " ==>>  type : " + type + "  size: " + lists.size());
        }
    }

    private IndexRequestBuilder changeToBuilderhasParent(BulkRequestBuilder bulkRequest, TransportClient client,
                                                         String index, String type, Object obj) {
        try {
            Class<?> demo = obj.getClass();
            Method method[] = demo.getMethods();
            String key = obj.getClass().getMethod("getKey").invoke(obj) + "";
            String mid = obj.getClass().getMethod("getMid").invoke(obj) + "";
            Map<String, String> map = new HashMap();
            for (int i = 0; i < method.length; ++i) {
                String name = method[i].getName();
                if (name.startsWith("get") && (!"getClass".equals(name)) && (!"getKey".equals(name))) {
                    name = name.substring(3);
                    String value = getter(obj, name);
                    name = name.toLowerCase();
                    map.put(name, value);
                }
            }
            System.out.println("index:" + index + ",type:" + type + ",key:" + key);
            return client.prepareIndex(index, type, key).setSource(map).setParent(mid);
        } catch (Exception e) {
            return null;
        }
    }

    public void BulkInsertHasParent(String index, String type, List<Object> lists) {
        TransportClient client = esClientMananger.getEsClient();
        try {
            BulkRequestBuilder bulkRequest = client.prepareBulk();

            for (int i = 0; i < lists.size(); i++) {
                Object obj = lists.get(i);
                bulkRequest.add(changeToBuilderhasParent(bulkRequest, client, index, type, obj));
            }
            BulkResponse bulkResponse = bulkRequest.get();
            if (bulkResponse.hasFailures()) {
                for (BulkItemResponse itemResponse : bulkResponse.getItems()) {

                    Failure failure = itemResponse.getFailure();
                    if (failure != null) {
                        System.err.println(" cause : " + failure.getCause());

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean exists(String index, String type, String mid) {
        TransportClient client = esClientMananger.getEsClient();
        SearchRequestBuilder searchRequest = null;
        SearchResponse searchResponse = null;
        try {
            searchRequest = client.prepareSearch(new String[]{index});
            searchRequest.setTypes(new String[]{type})
                    .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                    .setFetchSource(false)
                    .setQuery(QueryBuilders.idsQuery().ids(mid)) //采用filter查询，不计算结果。
            ;

            searchResponse = searchRequest.execute().actionGet();//执行请求

            // 获取命中
            SearchHits hits = searchResponse.getHits();
            return hits.getTotalHits() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void InsertHasParent(String index, String type, String id, Object bean, String parent) {
        TransportClient client = esClientMananger.getEsClient();
        try {
            JSONObject jsonObj1 = (JSONObject) JSON.toJSON(bean);
            client.prepareIndex(index, type, id).setSource(jsonObj1).setParent(parent).execute().actionGet();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void close() {
        esClientMananger.close();
    }

}
