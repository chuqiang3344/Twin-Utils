package com.tyaer.elasticsearch;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElasticSearchHelper<T> {


    // ---------------------- 索引相关 -----------------------

    private static String getter(Object obj, String att) {
        try {
            Method method = obj.getClass().getMethod("get" + att);
            String value = method.invoke(obj) + "";
            return value;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static UpdateRequestBuilder changeToBuilder(BulkRequestBuilder bulkRequest, TransportClient client, String index,
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

    public static void BulkIndex(String index, String type, List<Object> lists) {
        TransportClient client = EsClientMananger.getEsClient();
        try {

            BulkRequestBuilder bulkRequest = client.prepareBulk();
            for (int i = 0; i < lists.size(); i++) {
                Object obj = lists.get(i);
                if (obj == null) {
                    continue;
                }
                bulkRequest.add(changeToBuilder(bulkRequest, client, index, type, obj));
            }
            BulkResponse bulkResponse = bulkRequest.get();
            if (bulkResponse.hasFailures()) {
                for (BulkItemResponse itemResponse : bulkResponse.getItems()) {
                    Failure failure = itemResponse.getFailure();
                    if (failure != null) {
                        System.err.println("cause==>>>> " + failure.getCause());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(index + " ==>>  type : " + type + "  size: " + lists.size());
        } finally {
            if (client != null) {
                // client.close();
            }
        }
    }

    public static void main(String[] args) {
        ElasticSearchHelper<Object> helper = new ElasticSearchHelper<>();
        System.out.println(helper.getIndicies());
    }

    public static void delete(String index_name, String index_type, String... ids) {
        TransportClient transportClient = EsClientMananger.getEsClient();
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
     * @return 索引列表;
     * @desc 获取所有索引.
     */
    public String[] getIndicies() {
        TransportClient transportClient = EsClientMananger.getEsClient();
        String indices[] = null;
        try {
            ClusterStateResponse clusterStateResponse = transportClient.admin().cluster().prepareState()
                    .execute().actionGet();
            indices = clusterStateResponse.getState().getMetaData().getConcreteAllIndices();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return indices;
    }

    /**
     * @param indexName 索引名;
     * @return true or false;
     * @desc 判断某个索引是否存在
     */
    public boolean isExistsIndex(String indexName) {
        TransportClient transportClient = EsClientMananger.getEsClient();
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
        TransportClient transportClient = EsClientMananger.getEsClient();
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
        TransportClient client = EsClientMananger.getEsClient();
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
        } finally {
            if (client != null) {
                // client.close();
            }
        }
    }

    public void createIndex(String index, String type, String id, Object bean) {
        TransportClient client = EsClientMananger.getEsClient();
        // System.out.println(client != null);
        try {
            JSONObject jsonObj1 = (JSONObject) JSON.toJSON(bean);
            IndexResponse response = client.prepareIndex(index, type, id).setSource(jsonObj1).execute().actionGet();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (client != null) {
                // client.close();
            }
        }
    }

    public void update1(String index, String type, String id, Map<String, String> map) {
        TransportClient client = EsClientMananger.getEsClient();
        try {
            client.prepareUpdate(index, type, id).setDoc(map).get();
        } catch (Exception e) {
            System.err.println("update wrong " + index + " " + id);
            e.printStackTrace();
        } finally {
            if (client != null) {
                // client.close();
            }
        }
    }
    /**
     * @param INDEX_AMS 索引名;
     * @param index_type 索引类型;
     * @param qualifiers kv形式的键值对;
     * @return 更新失败或者成功.
     * @desc 更新索引字段信息.
     */
    public boolean delete(String INDEX_AMS, String index_type, String index_id,String field) {
        TransportClient transportClient = EsClientMananger.getEsClient();
        transportClient.prepareUpdate(INDEX_AMS, index_type, index_id).setScript(new Script("ctx._source.remove(\""+field+"\")", ScriptService.ScriptType.INLINE, null, null)).get();
        return true;
    }

    /**
     * @param INDEX_AMS 索引名;
     * @param index_type 索引类型;
     * @param qualifiers kv形式的键值对;
     * @return 更新失败或者成功.
     * @desc 更新索引字段信息.
     */
    public boolean update(String INDEX_AMS, String index_type, String index_id, Map<String, Object> qualifiers) {
        TransportClient transportClient = EsClientMananger.getEsClient();
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.index(INDEX_AMS).type(index_type).id(index_id).doc(qualifiers).refresh(true);
        FlushRequest flushRequest = new FlushRequest(INDEX_AMS);
        //flushRequest.
        transportClient.update(updateRequest).actionGet();
        flushRequest.force(true);
        transportClient.admin().indices().flush(flushRequest).actionGet();
        return true;

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
        TransportClient client = EsClientMananger.getEsClient();
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
        } finally {
            if (client != null) {
                // client.close();
            }
        }

    }

    public boolean exists(String index, String type, String mid) {
        TransportClient client = EsClientMananger.getEsClient();
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
        TransportClient client = EsClientMananger.getEsClient();
        try {
            JSONObject jsonObj1 = (JSONObject) JSON.toJSON(bean);
            client.prepareIndex(index, type, id).setSource(jsonObj1).setParent(parent).execute().actionGet();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (client != null) {
                // client.close();
            }
        }
    }


}
