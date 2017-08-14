package com.tyaer.elasticsearch;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.tyaer.elasticsearch.bean.ESConstant;
import com.tyaer.elasticsearch.bean.Paginator;
import com.tyaer.elasticsearch.conditions.CountCondition;
import org.apache.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkItemResponse.Failure;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.avg.AvgBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author guohongdou.
 * @Date 16/8/18.
 * @Version 0.0.1.
 * @Desc <p>VMSES es 工具类</p>.
 * @Update 16/8/18.
 */
public class ESHelper {
    private static TransportClient transportClient;
    private static Logger logger = Logger.getLogger(ESHelper.class);
    private static ApplicationContext applicationContext;


    /**
     * @return 客户端对象;
     * @desc 初始化客户端参数列表.
     */
    public static TransportClient initClient() {

        InputStream is = null;
        Properties props;
        try {
            if (transportClient == null) {
                System.out.println("####### init client #####");
                //加载es配置文件
//                is = ESHelper.class.getClassLoader().getResourceAsStream("src/main/config/elasticsearch.properties");
//                props.load(is);
                props = PropertiesLoaderUtils.loadAllProperties("elasticsearch.properties");

                //es 客户端设置
                Settings settings = Settings.settingsBuilder().put("cluster.name", props.getProperty("es.cluster.name").trim())//设置集群名称
                        .put("tclient.transport.sniff", true).build();//自动嗅探整个集群的状态，把集群中其它机器的ip地址加到客户端中
                transportClient = TransportClient.builder().settings(settings).build();

                //得到es主机
                String es_hosts = props.getProperty("es.hosts").trim();
                String nodes[] = es_hosts.split(",");
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
                return transportClient;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return transportClient;
    }


    public static JSONObject BulkIndex(String index, String type, List<FTStatusWeibo> lists) {

        try {

            BulkRequestBuilder bulkRequest = transportClient.prepareBulk();
            for (int i = 0; i < lists.size(); i++) {
                Object obj = lists.get(i);
                if (obj == null) {
                    continue;
                }
                bulkRequest.add(changeToBuilder(bulkRequest, transportClient, index, type, obj));
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
            if (transportClient != null) {
                // client.close();
            }
        }
        return FastJsonHelper.deserialize(new AMSResponse(0, "").toString(), JSONObject.class);
    }

    private static UpdateRequestBuilder changeToBuilder(BulkRequestBuilder bulkRequest, TransportClient client, String index,
                                                        String type, Object obj) {
        try {
            Class<?> demo = obj.getClass();
            Field[] fields = demo.getDeclaredFields();
            Map<String, String> map = new HashMap<String, String>();
            String key = null;
            for (Field field : fields) {
                field.setAccessible(true);
                String name = field.getName().toLowerCase();
                String value = field.get(obj) + "";
                if (null != value && !"".equals(value.trim()) && !"null".equals(value)) {
                    map.put(name, value);
                }
                if (name.equals("mid")) {
                    key = field.get(obj) + "";
                }

            }

            //System.out.println(map);

            return client.prepareUpdate(index, type, key).setDoc(map).setUpsert(map);


        } catch (Exception e) {
            System.out.println("changed!!!!!");
            e.printStackTrace();
            return null;
        }
    }

    public static String countTimeCost() {
        initClient();

        SearchRequestBuilder searchRequest = null;
        SearchResponse searchResponse = null;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar c = Calendar.getInstance();
        //c.add(Calendar.DAY_OF_MONTH, -31);

        // c.add(Calendar.DAY_OF_MONTH, -14);
        String end = df.format(c.getTime());
        c.add(Calendar.DAY_OF_MONTH, -1);
        String start = df.format(c.getTime());

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        Scroll scoll = new Scroll(new TimeValue(600000));


        queryBuilder.must(QueryBuilders.rangeQuery("operatorTime").from(start).to(end).format("yyyy-MM-dd HH:mm:ss"))
                .must(QueryBuilders.termQuery("projectName", "data_platform_api"));


        AvgBuilder aggregation = AggregationBuilders
                .avg("avg")
                .field("operatorTimeLE");

        AggregationBuilder term = AggregationBuilders
                .terms("group")
                .field("operatorObject").subAggregation(aggregation);

        searchRequest = transportClient.prepareSearch(new String[]{ESConstant.INDEX_AMS.toString()});
        searchRequest.setTypes(new String[]{ESConstant.ARTICLE_TYPE.toString()})

                // .setFetchSource(new String[]{"mid","emotion","zan_count","comments_count","reposts_count","created_at"}, null)//指定返回字段.
                .setFetchSource(false)//指定返回字段.
                .setQuery(queryBuilder)
                .addAggregation(term)

                .setSize(0);

        logger.info(searchRequest);

        // .setQuery(QueryBuilders.termQuery("content", "黑社交"))
        searchResponse = searchRequest.get(); //执行请求.
        // 获取命中
        SearchHits hits = searchResponse.getHits();

        System.out.println(searchResponse);


        return searchResponse.toString();


    }

    public static short notZoreBitCount(long a) {
        short count = 0;
        for (int i = 0; i < 64; i++) {
            count += a & 1;
            a = a >> 1;
        }
        return count;
    }

    public static boolean isValidHanmingCode(String hanmingCode) {
        if (null != hanmingCode && hanmingCode.matches("[a-f0-9]{1,16}") && notZoreBitCount(Long.parseUnsignedLong(hanmingCode, 16)) > 10) {
            return true;
        }
        //logger.info("invalid hanmingCode:"+hanmingCode);
        return false;
    }

    public static void test() {
        new ESHelper().initClient();

        SearchRequestBuilder searchRequest = null;
        SearchResponse searchResponse = null;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -31);
        String end = df.format(c.getTime());
        c.add(Calendar.YEAR, -2);
        String start = df.format(c.getTime());

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        Scroll scoll = new Scroll(new TimeValue(600000));

        queryBuilder.must(QueryBuilders.rangeQuery("created_at").from(start).to(end).format("yyyy-MM-dd HH:mm:ss"));
        queryBuilder.must(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("orgs_tag")).mustNot(QueryBuilders.existsQuery("orgs_event_tag")).mustNot(QueryBuilders.existsQuery("orgs_alert_tag")).mustNot(QueryBuilders.existsQuery("push_types")));
//  	queryBuilder.must(QueryBuilders.boolQuery().should(QueryBuilders.termQuery("text_loc","上")));
//  	queryBuilder.must(QueryBuilders.boolQuery().should(QueryBuilders.termQuery("text_loc","海")));
        searchRequest = transportClient.prepareSearch(new String[]{ESConstant.INDEX_AMS.toString()});
        searchRequest.setTypes(new String[]{"t_status_weibo"}).
                setSearchType(SearchType.SCAN)
                // .setFetchSource(new String[]{"mid","emotion","zan_count","comments_count","reposts_count","created_at"}, null)//指定返回字段.
                .setFetchSource(new String[]{"mid"}, null)//指定返回字段.
                .setQuery(queryBuilder)
                .setSize(1000)

                .setScroll(scoll);
        System.out.println(searchRequest);

        // .setQuery(QueryBuilders.termQuery("content", "黑社交"))
        searchResponse = searchRequest.get(); //执行请求.
        // 获取命中
        SearchHits hits = searchResponse.getHits();
        List<String> mids = new ArrayList<String>();
        Set<String> sets = new HashSet<String>();
        Set<String> records = new HashSet<String>();
        int i = 0;
        while (true) {


            for (SearchHit hit : hits) {
                String mid = hit.getSource().get("mid").toString();
                // String text = hit.getSource().get("text").toString();
//    	      if(text.length()<60){
//    	    	  continue;
//    	      }
                i++;

//       	      String text_loc_province = hit.getSource().get("text_loc_province").toString();
//       	      String text_loc_city = hit.getSource().get("text_loc_city")==null?text_loc_province:hit.getSource().get("text_loc_city").toString();
//       	      String emotion = hit.getSource().get("emotion").toString();
//       	      String record=mid+"|"+text+"|"+text_loc_province+"|"+text_loc_city+"|"+emotion;
//       	      records.add(record);
                mids.add(mid);
                sets.add(mid);
            }
            String scrollId = searchResponse.getScrollId();
//    	    if(i>2000){
//  	    	  break;
//  	       }

            searchResponse = transportClient.prepareSearchScroll(scrollId).setScrollId(scrollId).setScroll(scoll).get();

            String scrollId2 = searchResponse.getScrollId();
            //System.out.println(scrollId.equals(scrollId2));
            hits = searchResponse.getHits();
            //Break condition: No hits are returned
            if (searchResponse.getHits().getHits().length == 0) {

                break;
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String mid : mids) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(mid);
        }
        try {
            System.out.println("mid size:" + mids.size() + "," + sets.size());
            String path = "/home/zhonghong/test/AMSES-1.0-SNAPSHOT/records.txt";
            path = "d:\\records.txt";
            FileUtils.write2File(path, sb.toString(), "utf-8", false);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        System.out.println(Long.toHexString(Long.MIN_VALUE + 1));

        System.out.println(notZoreBitCount(Long.parseUnsignedLong("b7daf13d81f32f02", 16)));

        long start = Calendar.getInstance().getTimeInMillis();
        new ESHelper().initClient();
        test();
        long end = Calendar.getInstance().getTimeInMillis();

        System.out.println(end - start);

    }

    /**
     * @param beans 实体beans，采用fastjson注解的形式，把实体对象转换成json串;
     * @return 返回以json形式的信息;
     * @desc 批量索引提高索引效率.
     */
    public JSONObject bulkIndex(List<? extends Object> beans) throws NoSuchFieldException, IllegalAccessException {
        String json_result = "";
        BulkResponse response = null; //es返回.

        // 通过实体对象获取索引元数据信息

        String id_name = "";
        boolean has_parent = false;

        try {
            // 解析实体bean，获取index元数据.
            JSONObject index_meta = ParseBeanHelper.parseBean(beans.get(0));

            id_name = index_meta.getString("id");

//            System.out.println("index:" + INDEX_AMS + " type:" + index_type + " idName:" + id_name);

            // 要求：索引需要首先创建索引及mapping.

            BulkRequestBuilder request = transportClient.prepareBulk(); //批量请求
            for (Iterator<? extends Object> iterator = beans.iterator(); iterator.hasNext(); ) {

                Object t = iterator.next(); //实体bean对象.
                String mid = null;
                String subjectId = null;
                String orgId = null;
                boolean filter = false;
                if (t instanceof FAlertSubject) {
                    mid = ((FAlertSubject) t).getM_id();
                    subjectId = ((FAlertSubject) t).getSub_id();
                    orgId = ((FAlertSubject) t).getOrg_id();
                    if (mid == null) {
                        mid = ((FAlertSubject) t).getParent_id();
                    }
                    filter = true;
                } else if (t instanceof FOrginSubject) {
                    mid = ((FOrginSubject) t).getM_id();
                    subjectId = ((FOrginSubject) t).getSub_id();
                    orgId = ((FOrginSubject) t).getOrg_id();
                    if (mid == null) {
                        mid = ((FOrginSubject) t).getParent_id();
                    }
                    filter = true;
                } else if (t instanceof FTEvent) {
                    mid = ((FTEvent) t).getM_id();
                    subjectId = ((FTEvent) t).getSub_id();
                    orgId = ((FTEvent) t).getOrg_id();
                    if (mid == null) {
                        mid = ((FTEvent) t).getParent_id();
                    }
                    filter = true;
                }
                if (filter && (subjectId != null && !subjectId.matches("\\d+"))) {
                    return FastJsonHelper.deserialize("{'error':'" + "subjectId is not valid,subjectId:" + subjectId + "'}", JSONObject.class);
                }
                // 通过bean toString() 方法获取索引相关信息.
                // 设置文档ID属性访问权限为true.
                Field field = t.getClass().getDeclaredField(id_name);
                field.setAccessible(true);
                //采用java反射机制获取对象属性以及属性值.
                String id = field.get(t).toString();
                if (null != mid && null != subjectId) {
                    id = subjectId + "|" + mid;
                } else if (null != mid && null != orgId) {
                    id = orgId + "-" + mid;
                }
                IndexRequestBuilder indexRequestBuilder = transportClient.prepareIndex(ESConstant.INDEX_AMS.toString(), "t_status_weibo",
                        id);

                // 判断并指定父类型ID.
                JSONObject object = ParseBeanHelper.parseBean(t);


                String source = FastJsonHelper.serialize(t);
                if (null != mid) {
                    try {
                        String weiboSource = getSource(mid);
                        if (null != weiboSource && weiboSource.indexOf(":") > 0) {
                            source = source.substring(0, source.length() - 1) + "," + weiboSource.substring(1, weiboSource.length() - 1) + "}";
                        }

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                request.add(indexRequestBuilder.setSource(source));
//                    println_(FastJsonHelper.serialize(t));
            }
            response = request.execute().actionGet();
            request.request().requests().clear();//释放请求.

            //处理错误信息.
                /*
                 规定：索引成功返回状态码200，失败返回500，无创建索引及mapping返回404错误.
                 */
            if (response != null && response.hasFailures()) {
                // 获取错误索引列表信息.
                String failure_indices = "";
                for (BulkItemResponse itemResponse : response.getItems()) {
                    failure_indices += itemResponse.getFailure().getIndex() + ",";
                }
                json_result = new AMSResponse(1, response.buildFailureMessage()).toString();
            } else {
                json_result = new AMSResponse(0, "").toString();

            }

//            if(alsoSavedToHbase&&"t_alert_subject".equals(index_type)){
//
//            	 for (Iterator<? extends Object> iterator = beans.iterator(); iterator.hasNext(); ) {
//            		 FAlertSubject alertSubject = (FAlertSubject) iterator.next();
//            		 hbaseAPI.addAlarm(alertSubject.getOrg_id(), alertSubject.getSub_id(), alertSubject.getM_id(), alertSubject.getPush_type());
//            	 }
//            }
            return FastJsonHelper.deserialize(json_result, JSONObject.class);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IndexNotFoundException e) {
            logger.error(e.getDetailedMessage());//记录日志
            println_("error:请联系es管理员创建索引以及mapping....");
//            throw new ElasticsearchException(e.getDetailedMessage());
        }
        json_result = new AMSResponse(1, "jsonParse exception,please check this bean [" + beans.get(0).getClass().toGenericString() + "]").toString();
        return FastJsonHelper.deserialize(json_result.toString(), JSONObject.class);

    }

    /**
     * @param beans 实体beans，采用fastjson注解的形式，把实体对象转换成json串;
     * @return json格式的更新信息;
     * @desc 实体bean批量更新.
     */
    @Deprecated
    public JSONObject bulkUpdate(List<? extends Object> beans) throws NoSuchFieldException, IllegalAccessException {
        BulkResponse response = null;
        String json_result = "";

        // 通过实体对象获取索引元数据信息
        String INDEX_AMS = "";
        String index_type = "";
        String id_name = "";
        try {
            Map<String, Object> index_meta = ParseBeanHelper.parseBean(beans.get(0));

            INDEX_AMS = index_meta.get("INDEX_AMS").toString();
            index_type = index_meta.get("index_type").toString();
            id_name = index_meta.get("id_name").toString();
            logger.info("index:" + INDEX_AMS + " type:" + index_type + " idName:" + id_name);

            if (isExistsType(INDEX_AMS, index_type)) {
                BulkRequestBuilder request = transportClient.prepareBulk();
                //根据文档ID批量更新.
                for (Iterator<? extends Object> iterator = beans.iterator(); iterator.hasNext(); ) {
                    Object t = iterator.next();
                    // 设置文档ID属性访问权限为true.
                    Field field = t.getClass().getDeclaredField(id_name);
                    field.setAccessible(true);
                    request.add(transportClient.prepareUpdate(INDEX_AMS, index_type, field.get(t).toString())
                            .setDoc(FastJsonHelper.serialize(t)));
                }
                response = request.execute().actionGet();
                request.request().requests().clear(); //释放请求.
            } else {
                System.out.println("请先创建索引以及mapping....");
            }

            //处理错误信息.
            /*
             规定：索引成功返回状态码200，失败返回500.
             */
            if (response.hasFailures()) {
                // 获取错误索引列表信息.
                String failure_indices = "";
                for (BulkItemResponse itemResponse : response.getItems()) {
                    failure_indices += itemResponse.getFailure().getIndex() + ",";
                }
                json_result = new AMSResponse(1, response.buildFailureMessage()).toString();
            } else {
                json_result = new AMSResponse(0, "").toString();
            }
            return FastJsonHelper.deserialize(json_result, JSONObject.class);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // 记录es日志.
            logger.error(e.getMessage());
        }
        json_result = new AMSResponse(1, "jsonParse exception,please check this bean [" + beans.get(0).getClass().toGenericString() + "]").toString();
        return FastJsonHelper.deserialize(json_result.toString(), JSONObject.class);
    }

    /**
     * @param INDEX_AMS  索引名;
     * @param index_type 索引类型;
     * @param ids        索引id列表;
     * @return 失败或者成功的信息;
     * @desc 根据id列表批量删除.
     */
    public JSONObject deleteByIDS(String INDEX_AMS, String index_type, String... ids) {
        initClient();
        BulkRequestBuilder bulkRequest = transportClient.prepareBulk();
        BulkResponse bulkResponse = null;
        String json_result = "";
        try {
            if (isExistsType(INDEX_AMS, index_type)) {
                //构建批量删除请求.
                for (String id : ids) {
                    DeleteRequestBuilder deleteBuilder = transportClient.prepareDelete(INDEX_AMS, index_type, id);
                    if ("t_orgin_subject".equals(index_type) || "t_alert_subject".equals(index_type)) {
                        String routing = id.substring(id.indexOf("|") + 1);
                        deleteBuilder.setRouting(routing);
                    }
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
                    json_result = new AMSResponse(1, bulkResponse.buildFailureMessage()).toString();
                } else {
                    json_result = new AMSResponse(0, "").toString();
                }

                // 释放请求.
                bulkRequest.request().requests().clear();//释放请求.
                bulkRequest.setTimeout(TimeValue.timeValueMinutes(20000)); //设置请求超时时间
            } else {
                json_result = new AMSResponse(2, "type [" + index_type + "] not found....").toString();
            }
            logger.info("delete result:" + json_result);
            return FastJsonHelper.deserialize(json_result, JSONObject.class);
        } catch (ElasticsearchException e) {
            e.printStackTrace();
            logger.error(e.getDetailedMessage());
        }
        json_result = new AMSResponse(1, "es异常,请联系es管理员。").toString();
        return FastJsonHelper.deserialize(json_result.toString(), JSONObject.class);
    }

    public List<String> getDeletedSubjects(String orgId, String type) throws Exception {
        initClient();
        SearchResponse searchResponse = null;
        SearchRequestBuilder searchRequest = null;

        logger.info("orgId:" + orgId + ",type:" + type);// 打印DSL语句.


        List<String> subjectIds = new ArrayList<String>();

        searchRequest = transportClient.prepareSearch(new String[]{ESConstant.INDEX_AMS.toString()});
        searchRequest.setTypes(new String[]{"t_delete_subject"})
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFetchSource(true)
                .setQuery(QueryBuilders.boolQuery().must(QueryBuilders.termQuery("orgId", orgId)).must(QueryBuilders.termQuery("type", type))) //采用filter查询，不计算结果。
        ;
//                if(null!=fields){
        searchRequest.setFetchSource(new String[]{"subjectId"}, null);
//                }

        logger.info("searchRequest:" + searchRequest);

        searchResponse = searchRequest.execute().actionGet();//执行请求

        // 获取命中
        SearchHits hits = searchResponse.getHits();


        logger.info("get source hits:" + hits.getHits().length + ",took:" + searchResponse.getTook());
        // 默认显示10条.
        for (int i = 0; i < hits.getHits().length; i++) {

            String subjectId = hits.getAt(i).getSource().get("subjectId").toString();
            subjectIds.add(subjectId);
        }
        return subjectIds;
    }

    public String getSource(String mid) throws Exception {
        SearchResponse searchResponse = null;
        SearchRequestBuilder searchRequest = null;

        logger.info("mid:" + mid);// 打印DSL语句.


        if (mid == null) {
            throw new Exception("mid can not be null,ids:" + mid);
        }


        searchRequest = transportClient.prepareSearch(new String[]{ESConstant.INDEX_AMS.toString()});
        searchRequest.setTypes(new String[]{"t_status_weibo"})
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFetchSource(true)
                .setQuery(QueryBuilders.idsQuery().ids(mid)) //采用filter查询，不计算结果。
        ;
//                 if(null!=fields){
//                 	searchRequest.setFetchSource(fields, null);
//                 }

        logger.info("searchRequest:" + searchRequest);

        searchResponse = searchRequest.execute().actionGet();//执行请求

        // 获取命中
        SearchHits hits = searchResponse.getHits();


        logger.info("get source hits:" + hits.getHits().length + ",took:" + searchResponse.getTook());
        // 默认显示10条.
        if (hits.getHits().length == 1) {

            String source = hits.getAt(0).getSourceAsString();
            return source;
        }

        return null;
    }

    public JSONObject getRecommendations(String orgId, int topN) {
        SearchResponse searchResponse = null;
        SearchRequestBuilder searchRequest = null;
        StringBuffer result = new StringBuffer();
//         Map<String,String> emotionMap = null;

//         if("t_status_weibo".equals(index_type)&&null!=orgId&&(null==fields||Arrays.asList(fields).contains("emotion"))){
//
//
//         	emotionMap=hbaseAPI.getEmotions(orgId, Arrays.asList(ids));
//         	System.out.println(emotionMap);
//         }
        logger.info("orgId:" + orgId + ",topN:" + topN);// 打印DSL语句.
        Date start = DateHelper.getCertainDaysBefore(3);
        Date end = Calendar.getInstance().getTime();

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();


        if (orgId.equals("10006")) {
            queryBuilder.must(QueryBuilders.termQuery("text_loc_city", "深圳"));
        } else if (!"2".equals(orgId)) {
            queryBuilder.must(QueryBuilders.termQuery("text_loc", "上海"));
        }
        queryBuilder.must(QueryBuilders.termQuery("recommends_tag", "0"));
        queryBuilder.mustNot(QueryBuilders.termQuery("recommends_delete_tag", orgId));
        QueryBuilder timeQuery = QueryBuilders.rangeQuery("created_at")
                .from(DateHelper.dateToString(start, "yyyy-MM-dd HH:mm:ss"))
                .to(DateHelper.dateToString(end, "yyyy-MM-dd HH:mm:ss")).format("yyyy-MM-dd HH:mm:ss");
        queryBuilder.must(timeQuery);
        try {


            searchRequest = transportClient.prepareSearch(ESConstant.INDEX_AMS.toString());
            searchRequest.setTypes(new String[]{ESConstant.ARTICLE_TYPE.toString(), ESConstant.WEIBO_TYPE.toString()})
                    .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                    .setFetchSource(false)
                    .setQuery(queryBuilder) //采用filter查询，不计算结果。
            ;

            searchRequest.setFrom(0).setSize(topN);


            // .setQuery(QueryBuilders.termQuery("content", "黑社交"))


            searchRequest.addSort("created_at", SortOrder.DESC);


            logger.info("searchRequest:" + searchRequest);

            searchResponse = searchRequest.execute().actionGet();//执行请求

            // 获取命中
            SearchHits hits = searchResponse.getHits();
            result.append(("{'took':'" + searchResponse.getTook() + "','hits':{'total':" + hits.getTotalHits() + ",'display':[").replaceAll("'", "\""));

            logger.info("hits:" + hits.getHits().length + ",took:" + searchResponse.getTook());
            // 默认显示10条.
            for (int i = 0; i < hits.getHits().length; i++) {
                // 构建返回字段
                /**result.append("{");
                 for (int j = 0; j < fields.length; j++) {
                 result.append("'" + fields[j] + "':" + "'" + hits.getAt(i).getSource().get(fields[j]) + "'");
                 //if (j < fields.length - 1) {
                 result.append(",");
                 //}
                 }
                 **/
                String source = hits.getAt(i).getSourceAsString();
//                     String id = hits.getAt(i).getId();
//                     if(null!=emotionMap){
//                     	String emotion = emotionMap.get(id);
//                     	if(null!=emotion){
//                     		source = source.replaceAll("\"emotion\":\"\\S\"", "\"emotion\":\""+emotion+"\"");
//                     	}
//                     }

                //source = source.substring(0,source.length()-1);
                //result.append(source);
                result.append("\"" + hits.getAt(i).getId() + "\"");
//
//                 	if(CheckFieldHelper.isWeiboMid(hits.getAt(i).getId())&&!source.contains("\"article_type\"")){
//                 		result.append(",\"article_type\":\"0\"");
//                 	}


                if (i < hits.totalHits() - 1) {
                    result.append(",");
                }
            }
            result.append("]}}");

            JSONObject json = FastJsonHelper.deserialize(result.toString(), JSONObject.class);
            return json;
        } catch (ElasticsearchException e) {
            logger.error(e.getDetailedMessage());
        }
        return FastJsonHelper.deserialize(new AMSResponse(1, "es异常,请联系es管理员。").toString(), JSONObject.class);
    }

    public JSONObject deleteRecommendation(String orgId, String... mids) {
        try {

            Set<String> idSet = new HashSet<String>();
            for (String mid : mids) {
                idSet.add(mid);
            }
            logger.info("orgId:" + orgId + ",mids:" + idSet);


            JSONObject json = this.idsSearch(orgId, ESConstant.INDEX_AMS.toString(), new String[]{"t_status_weibo", "t_article"}, mids, null, "recommends_delete_tag");

            JSONArray jsonArray = json.getJSONObject("hits").getJSONArray("display");
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObj = jsonArray.getJSONObject(i);
                String mid = jsonObj.getString("_id");
                String subs = null;
                String orgs = null;
                String[] orgArray = null;
                if (jsonObj.containsKey("recommends_delete_tag")) {
                    orgs = jsonObj.getString("recommends_delete_tag");
                    orgArray = orgs.split("\\,");

                }
                Set<String> orgList = new HashSet<String>();
                if (null != orgArray) {
                    for (String org : orgArray) {
                        orgList.add(org);
                    }
                }

                orgList.add(orgId);
                StringBuilder sb = new StringBuilder();
                if (null != orgList && orgList.size() > 0) {
                    for (String org : orgList) {
                        if (sb.length() > 0) {
                            sb.append(",");
                        }
                        sb.append(org);
                    }
                }
                orgs = sb.toString();


                if (null != orgs) {
                    Map<String, Object> data = new HashMap<String, Object>();


                    data.put("recommends_delete_tag", orgs);
                    logger.info("begin add   mid:" + mid + ",orgId:" + orgs + "subs:" + subs);
                    if (CheckFieldHelper.isWeiboMid(mid)) {
                        this.update(ESConstant.INDEX_AMS.toString(), "t_status_weibo", mid, data);
                    } else {
                        this.update(ESConstant.INDEX_AMS.toString(), "t_article", mid, data);
                    }
                    logger.info("finish add   mid:" + mid + ",orgId:" + orgs + "subs:" + subs);
                }
            }


            return JSON.parseObject(new AMSResponse(0, "").toString());
        } catch (Exception e) {
            logger.error("failed delete  recommendation mid:" + mids + ",orgId:" + orgId, e);
            return JSON.parseObject(new AMSResponse(1, "failed delete recommendation   mid:" + mids + ",orgId:" + orgId).toString());
        }
    }

    /**
     * @param INDEX_AMS  索引名;
     * @param index_type 索引类型;
     * @param ids        索引id列表;
     * @param fields     返回字段列表;
     * @return json格式的索引列表;
     * @desc 根据ids获取.
     */
    public JSONObject idsSearch(String orgId, String INDEX_AMS, String[] index_types, String[] ids, Paginator paginator, String... fields) throws Exception {
        SearchResponse searchResponse = null;
        SearchRequestBuilder searchRequest = null;
        StringBuffer result = new StringBuffer();
//        Map<String,String> emotionMap = null;

//        if("t_status_weibo".equals(index_type)&&null!=orgId&&(null==fields||Arrays.asList(fields).contains("emotion"))){
//
//
//        	emotionMap=hbaseAPI.getEmotions(orgId, Arrays.asList(ids));
//        	System.out.println(emotionMap);
//        }
        logger.info("orgId:" + orgId + ",ids:" + ids);// 打印DSL语句.

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
        try {


            searchRequest = transportClient.prepareSearch(new String[]{INDEX_AMS});
            searchRequest.setTypes(index_types)
                    .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                    .setFetchSource(fields, null)
                    .setQuery(queryBuilder) //采用filter查询，不计算结果。
            ;
            if (null != paginator) {
                searchRequest.setFrom(paginator.getFrom()).setSize(paginator.getSize());
            } else {
                searchRequest.setFrom(0).setSize(600);
            }


            // .setQuery(QueryBuilders.termQuery("content", "黑社交"))

            if (null != paginator && paginator.getSortOrder() != null) {
                String orderField = paginator.getField();
//					if("reports_count".equals(orderField)){
//						orderField = "reposts_count";
//					}
                searchRequest.addSort(orderField, paginator.getSortOrder());
            }

            logger.info("searchRequest:" + searchRequest);

            searchResponse = searchRequest.execute().actionGet();//执行请求

            // 获取命中
            SearchHits hits = searchResponse.getHits();
            result.append(("{'took':'" + searchResponse.getTook() + "','hits':{'total':" + hits.getTotalHits() + ",'display':[").replaceAll("'", "\""));

            logger.info("hits:" + hits.getHits().length + ",took:" + searchResponse.getTook());
            // 默认显示10条.
            for (int i = 0; i < hits.getHits().length; i++) {
                // 构建返回字段
                /**result.append("{");
                 for (int j = 0; j < fields.length; j++) {
                 result.append("'" + fields[j] + "':" + "'" + hits.getAt(i).getSource().get(fields[j]) + "'");
                 //if (j < fields.length - 1) {
                 result.append(",");
                 //}
                 }
                 **/
                String source = hits.getAt(i).getSourceAsString();
//                    String id = hits.getAt(i).getId();
//                    if(null!=emotionMap){
//                    	String emotion = emotionMap.get(id);
//                    	if(null!=emotion){
//                    		source = source.replaceAll("\"emotion\":\"\\S\"", "\"emotion\":\""+emotion+"\"");
//                    	}
//                    }

                source = source.substring(0, source.length() - 1);
                result.append(source);
                result.append(",\"_id\":\"" + hits.getAt(i).getId() + "\"");

                if (CheckFieldHelper.isWeiboMid(hits.getAt(i).getId()) && !source.contains("\"article_type\"")) {
                    result.append(",\"article_type\":\"0\"");
                }

                result.append("}");

                if (i < hits.totalHits() - 1) {
                    result.append(",");
                }
            }
            result.append("]}}");

            JSONObject json = FastJsonHelper.deserialize(result.toString(), JSONObject.class);
            return json;
        } catch (ElasticsearchException e) {
            logger.error(e.getDetailedMessage());
        }
        return FastJsonHelper.deserialize(new AMSResponse(1, "es异常,请联系es管理员。").toString(), JSONObject.class);
    }

    public JSONObject userSearch(String INDEX_AMS, String[] index_types, int userType, String keyword, Paginator paginator, String... fields) throws Exception {
        SearchResponse searchResponse = null;
        SearchRequestBuilder searchRequest = null;
        StringBuffer result = new StringBuffer();


        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        if (null != keyword && keyword.trim().length() > 0) {
            keyword = keyword.trim();
            queryBuilder.must(QueryBuilders.regexpQuery("account_name", ".*" + keyword + ".*"));
        }

        try {


            searchRequest = transportClient.prepareSearch(new String[]{INDEX_AMS});
            searchRequest.setTypes(index_types)
                    .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                    .setFetchSource(fields, null)
                    .setQuery(queryBuilder) //采用filter查询，不计算结果。
            ;
            if (null != paginator) {
                searchRequest.setFrom(paginator.getFrom()).setSize(paginator.getSize());
            } else {
                searchRequest.setFrom(0).setSize(20);
            }


            // .setQuery(QueryBuilders.termQuery("content", "黑社交"))

            if (null != paginator && paginator.getSortOrder() != null) {
                String orderField = paginator.getField();
//					if("reports_count".equals(orderField)){
//						orderField = "reposts_count";
//					}
                searchRequest.addSort(orderField, paginator.getSortOrder());
            }

            logger.info("searchRequest:" + searchRequest);

            searchResponse = searchRequest.execute().actionGet();//执行请求

            // 获取命中
            SearchHits hits = searchResponse.getHits();
            result.append(("{'took':'" + searchResponse.getTook() + "','hits':{'total':" + hits.getTotalHits() + ",'display':[").replaceAll("'", "\""));

            logger.info("hits:" + hits.getHits().length + ",took:" + searchResponse.getTook());
            // 默认显示10条.
            for (int i = 0; i < hits.getHits().length; i++) {
                // 构建返回字段
                /**result.append("{");
                 for (int j = 0; j < fields.length; j++) {
                 result.append("'" + fields[j] + "':" + "'" + hits.getAt(i).getSource().get(fields[j]) + "'");
                 //if (j < fields.length - 1) {
                 result.append(",");
                 //}
                 }
                 **/
                String source = hits.getAt(i).getSourceAsString();
//                    String id = hits.getAt(i).getId();
//                    if(null!=emotionMap){
//                    	String emotion = emotionMap.get(id);
//                    	if(null!=emotion){
//                    		source = source.replaceAll("\"emotion\":\"\\S\"", "\"emotion\":\""+emotion+"\"");
//                    	}
//                    }

                source = source.substring(0, source.length() - 1);
                result.append(source);
                result.append(",\"_id\":\"" + hits.getAt(i).getId() + "\"");


                result.append("}");

                if (i < hits.totalHits() - 1) {
                    result.append(",");
                }
            }
            result.append("]}}");

            JSONObject json = FastJsonHelper.deserialize(result.toString(), JSONObject.class);
            return json;
        } catch (ElasticsearchException e) {
            logger.error(e.getDetailedMessage());
        }
        return FastJsonHelper.deserialize(new AMSResponse(1, "es异常,请联系es管理员。").toString(), JSONObject.class);
    }

    public JSONObject idsSearch(String orgId, String[] subjectIds, String INDEX_AMS, String[] index_types, String[] ids, Paginator paginator, boolean hanmingCodeNotNull, String... fields) throws Exception {
        SearchResponse searchResponse = null;
        SearchRequestBuilder searchRequest = null;
        StringBuffer result = new StringBuffer();
//        Map<String,String> emotionMap = null;

//        if("t_status_weibo".equals(index_type)&&null!=orgId&&(null==fields||Arrays.asList(fields).contains("emotion"))){
//
//
//        	emotionMap=hbaseAPI.getEmotions(orgId, Arrays.asList(ids));
//        	System.out.println(emotionMap);
//        }
        logger.info("subjectIds:" + subjectIds + ",ids:" + ids);// 打印DSL语句.

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

        try {
            BoolQueryBuilder parentBuilder = QueryBuilders.boolQuery();   // 构建父条件
            parentBuilder.must(QueryBuilders.idsQuery().ids(idArray));

            BoolQueryBuilder subQuery = QueryBuilders.boolQuery();
            if (null != subjectIds) {
                for (String subjectId : subjectIds) {
                    subQuery.should(QueryBuilders.termQuery("subjects_tag", subjectId));
                }
                parentBuilder.must(subQuery);
            }
            if (null != orgId) {
                parentBuilder.must(QueryBuilders.termQuery("orgs_tag", orgId));
            }

            if (hanmingCodeNotNull) {
                parentBuilder.mustNot(QueryBuilders.termQuery("hanmingCode", ""));
            }

            searchRequest = transportClient.prepareSearch(new String[]{INDEX_AMS});
            searchRequest.setTypes(index_types)
                    .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                    .setFetchSource(fields, null)
                    .setQuery(parentBuilder) //采用filter查询，不计算结果。
            ;
            if (null != paginator) {
                searchRequest.setFrom(paginator.getFrom()).setSize(paginator.getSize());
            } else {
                searchRequest.setFrom(0).setSize(100);
            }


            // .setQuery(QueryBuilders.termQuery("content", "黑社交"))

            if (null != paginator && paginator.getSortOrder() != null) {
                String orderField = paginator.getField();
//					if("reports_count".equals(orderField)){
//						orderField = "reposts_count";
//					}
                searchRequest.addSort(orderField, paginator.getSortOrder());
            }

            logger.info("searchRequest:" + searchRequest);

            searchResponse = searchRequest.execute().actionGet();//执行请求

            // 获取命中
            SearchHits hits = searchResponse.getHits();
            result.append(("{'took':'" + searchResponse.getTook() + "','hits':{'total':" + hits.getTotalHits() + ",'display':[").replaceAll("'", "\""));

            logger.info("hits:" + hits.getHits().length + ",took:" + searchResponse.getTook());
            // 默认显示10条.
            for (int i = 0; i < hits.getHits().length; i++) {
                // 构建返回字段
                /**result.append("{");
                 for (int j = 0; j < fields.length; j++) {
                 result.append("'" + fields[j] + "':" + "'" + hits.getAt(i).getSource().get(fields[j]) + "'");
                 //if (j < fields.length - 1) {
                 result.append(",");
                 //}
                 }
                 **/
                String source = hits.getAt(i).getSourceAsString();
//                    String id = hits.getAt(i).getId();
//                    if(null!=emotionMap){
//                    	String emotion = emotionMap.get(id);
//                    	if(null!=emotion){
//                    		source = source.replaceAll("\"emotion\":\"\\S\"", "\"emotion\":\""+emotion+"\"");
//                    	}
//                    }

                source = source.substring(0, source.length() - 1);
                result.append(source);
                result.append(",\"_id\":\"" + hits.getAt(i).getId() + "\"");
                if (CheckFieldHelper.isWeiboMid(hits.getAt(i).getId()) && !source.contains("\"article_type\"")) {
                    result.append(",\"article_type\":\"0\"");
                }
                result.append("}");

                if (i < hits.totalHits() - 1) {
                    result.append(",");
                }
            }
            result.append("]}}");

            JSONObject json = FastJsonHelper.deserialize(result.toString(), JSONObject.class);
            return json;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return FastJsonHelper.deserialize(new AMSResponse(1, "es异常,请联系es管理员。").toString(), JSONObject.class);
    }

    /**
     * @param INDEX_AMS  索引名;
     * @param index_type 索引类型;
     * @param condition  查询条件;
     * @param fields     查询返回字段列表;
     * @return json格式的索引列表;
     * @desc 自定义条件搜索文档.
     */
    public JSONObject defineSearch(String INDEX_AMS, String index_type, QueryBuilder condition, Paginator paginator, String... fields) throws NullPointerException {
        SearchResponse searchResponse = null;
        SearchRequestBuilder searchRequest = null;
        StringBuffer result = new StringBuffer();
        try {
            logger.info("index:" + INDEX_AMS + " type:" + index_type);

            if (isExistsType(INDEX_AMS, index_type)) {
                searchRequest = transportClient.prepareSearch(new String[]{INDEX_AMS});
                searchRequest.setTypes(new String[]{index_type})
                        //.setTimeout("1000") // 设置超时时间 1s
                        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                        .setFetchSource(fields, null)
                        .setPostFilter(condition) // 不计算得分，采用filter查询.
                        .setFrom(paginator.getFrom()).setSize(paginator.getSize()); // 设置分页.

                // 排序.
                if (paginator.getSortOrder() != null)
                    searchRequest.addSort(paginator.getField(), paginator.getSortOrder());

                println_(new Date() + " defineSearch() called\n" +
                        "--- DSL查询 Explain ----\n"
                        + searchRequest.toString() + "\n" +
                        "-----------------------");// 打印DSL语句.

                searchResponse = searchRequest.execute().actionGet(); //执行请求.
                // 获取命中
                SearchHits hits = searchResponse.getHits();
                result.append(("{'took':'" + searchResponse.getTook() + "','hits':{'total':" + hits.getTotalHits() + ",'display':[").replaceAll("'", "\""));
                // 默认显示10条.
                // 判断。。。。
                int count = hits.getHits().length >= paginator.getSize() ? paginator.getSize() : (int) hits.getHits().length;
                logger.info("hits:" + hits.getHits().length + ",took:" + searchResponse.getTook());
                for (int i = 0; i < count; i++) {

                    String source = hits.getAt(i).getSourceAsString();

                    source = source.replace("}", "");

                    result.append(source);
                    result.append(",\"_id\":\"" + hits.getAt(i).getId() + "\"");
                    result.append("}");
                    if (i < count - 1) {
                        result.append(",");
                    }
                }
                result.append("]}}");
            } else {
                // 索引不存在.
                result.append(new AMSResponse(1, "index [" + INDEX_AMS + "] not found"));
            }
            return FastJsonHelper.deserialize(result.toString(), JSONObject.class);
        } catch (ElasticsearchException e) {
            logger.error(e.getDetailedMessage());
        }
        return FastJsonHelper.deserialize(new AMSResponse(1, "es异常,请联系es管理员。").toString(), JSONObject.class);
    }

    public JSONObject count(String orgId, List<String> subjectIds, int type, Date start, Date end, Integer pushType, Integer emotion) {
        initClient();
        SearchResponse searchResponse = null;
        SearchRequestBuilder searchRequest = null;
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        StringBuffer result = new StringBuffer();
        try {
            String orgTag = null;
            if (type == 2) {
                orgTag = "orgs_tag";
            } else if (type == 0) {
                orgTag = "orgs_alert_tag";
            } else if (type == 3) {
                orgTag = "orgs_event_tag";
            } else {
                return JSON.parseObject(new AMSResponse(1, "type=" + type + " not support").toString());
            }

            String subTag = null;
            if (type == 2) {
                subTag = "subjects_tag";
            } else if (type == 0) {
                subTag = "alerts_tag";
            } else if (type == 3) {
                subTag = "events_tag";
            } else {
                return JSON.parseObject(new AMSResponse(1, "type=" + type + " not support").toString());
            }
            if (null != orgId) {
                queryBuilder.must(QueryBuilders.termQuery(orgTag, orgId));
            }
            if (null != subjectIds && subjectIds.size() > 0) {
                BoolQueryBuilder subQuery = QueryBuilders.boolQuery();
                for (String subjectId : subjectIds) {
                    subQuery.should(QueryBuilders.termQuery(subTag, subjectId));
                }
                queryBuilder.must(subQuery);
            }
            if (null != start) {
                QueryBuilder timeQuery = QueryBuilders.rangeQuery((type == 0 && pushType == 1) ? "manual_update_time" : "created_at")
                        .from(DateHelper.dateToString(start, "yyyy-MM-dd HH:mm:ss"))
                        .to(DateHelper.dateToString(end, "yyyy-MM-dd HH:mm:ss")).format("yyyy-MM-dd HH:mm:ss");

                queryBuilder.must(timeQuery);
            }
            if (null != emotion) {
                String emotionValue = null;
                if (1 == emotion) {
                    emotionValue = "正面";
                } else if (-1 == emotion) {
                    emotionValue = "负面";
                } else if (0 == emotion) {
                    emotionValue = "中性";
                }
                if (null != emotionValue) {
                    queryBuilder.must(QueryBuilders.termQuery("emotion", emotionValue));
                }
            }

            if (null != pushType) {
                if (1 == pushType) {
                    queryBuilder.must(QueryBuilders.termQuery("push_types", orgId));
                } else if (0 == pushType) {
                    queryBuilder.mustNot(QueryBuilders.termQuery("push_types", orgId));
                }
            }


            searchRequest = transportClient.prepareSearch(new String[]{ESConstant.INDEX_AMS.toString()});
            searchRequest.setTypes(new String[]{"t_status_weibo"})
                    //.setTimeout("1000") // 设置超时时间 1s
                    //.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                    .setFetchSource(false)
                    .setQuery(queryBuilder)
                    .setFrom(0).setSize(0); // 设置分页.


            println_(new Date() + " count() called\n" +
                    "--- DSL查询 Explain ----\n"
                    + searchRequest.toString() + "\n" +
                    "-----------------------");// 打印DSL语句.

            searchResponse = searchRequest.execute().actionGet(); //执行请求.
            SearchHits hits = searchResponse.getHits();
            // 获取命中
            result.append("{'took':'" + searchResponse.getTook() + "','hits':{'total':" + hits.getTotalHits() + ",'display':[]}}");

            return FastJsonHelper.deserialize(result.toString(), JSONObject.class);
        } catch (ElasticsearchException e) {
            logger.error(e.getDetailedMessage(), e);
        }
        return FastJsonHelper.deserialize(new AMSResponse(1, "es异常,请联系es管理员。").toString(), JSONObject.class);
    }

    public JSONObject countAll(CountCondition condition, String orgId, List<String> subjectIds, int type, Date start, Date end, Integer pushType, Integer emotion, String keyword) {
        initClient();
        SearchResponse searchResponse = null;
        SearchRequestBuilder searchRequest = null;
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        StringBuffer result = new StringBuffer();
        try {
            String orgTag = null;


            if (type == 2) {
                orgTag = "orgs_tag";
            } else if (type == 0) {
                orgTag = "push_types";
            } else if (type == 3) {
                orgTag = "orgs_event_tag";
            } else {
                return JSON.parseObject(new AMSResponse(1, "type=" + type + " not support").toString());
            }

            String subTag = null;
            if (type == 2) {
                subTag = "subjects_tag";
            } else if (type == 0) {
                subTag = "alerts_tag";
            } else if (type == 3) {
                subTag = "events_tag";
            } else {
                return JSON.parseObject(new AMSResponse(1, "type=" + type + " not support").toString());
            }
            if (null != orgId) {
                queryBuilder.must(QueryBuilders.termQuery(orgTag, orgId));

                if (null != condition.getCategories()) {
                    BoolQueryBuilder categoryBuilder = QueryBuilders.boolQuery();
                    for (Integer category : condition.getCategories()) {
                        categoryBuilder.should(QueryBuilders.termQuery("categories", category)); // 相当于or查询。
                    }
                    queryBuilder.must(categoryBuilder);
                }
                if (null != condition.getRegions()) {
                    BoolQueryBuilder regionBuilder = QueryBuilders.boolQuery();
                    for (Integer region : condition.getRegions()) {
                        regionBuilder.should(QueryBuilders.termQuery("regions", region)); // 相当于or查询。
                    }
                    queryBuilder.must(regionBuilder);
                }
                if (null != condition.getDepartments()) {
                    BoolQueryBuilder departmentBuilder = QueryBuilders.boolQuery();
                    for (Integer department : condition.getDepartments()) {
                        departmentBuilder.should(QueryBuilders.termQuery("departments", department)); // 相当于or查询。
                    }
                    queryBuilder.must(departmentBuilder);
                }
            }
            if (null != subjectIds && subjectIds.size() > 0) {
                BoolQueryBuilder subQuery = QueryBuilders.boolQuery();
                for (String subjectId : subjectIds) {
                    subQuery.should(QueryBuilders.termQuery(subTag, subjectId));
                }
                queryBuilder.must(subQuery);
            }
            if (null != condition && null != condition.getSiteIds() && condition.getSiteIds().size() > 0) {

                // 指定一个或多个网站
                BoolQueryBuilder siteBuilder = QueryBuilders.boolQuery();
                for (Integer siteId : condition.getSiteIds()) {
                    if (null != siteId) {
                        siteBuilder.should(QueryBuilders.termQuery("crawler_site_id", siteId));
                        siteBuilder.should(QueryBuilders.termQuery("site_id", siteId));
                    }

                    // 相当于or查询。
                }
                queryBuilder.must(siteBuilder);
            }

            if (null != condition.getTextLoc()) {
                queryBuilder.must(QueryBuilders.matchPhraseQuery("text_loc", condition.getTextLoc()).slop(1));
            }
            if (null != condition.getIsRubbish()) {
                if (condition.getIsRubbish()) {
                    queryBuilder.must(QueryBuilders.termQuery("rubbish", "1"));
                } else {
                    queryBuilder.mustNot(QueryBuilders.existsQuery("rubbish"));
                }
            }

            if (null != emotion) {
                String emotionValue = null;
                if (1 == emotion) {
                    emotionValue = "正";
                } else if (-1 == emotion) {
                    emotionValue = "负";
                } else if (0 == emotion) {
                    emotionValue = "中";
                }
                if (null != emotionValue) {
                    queryBuilder.must(QueryBuilders.termQuery("emotion", emotionValue));
                }
            }

            if (null != condition.getPid()) {
                String pid = condition.getPid().substring(condition.getPid().indexOf("_") + 1);
                queryBuilder.must(QueryBuilders.termQuery("mid_p", pid));
            }
            if (null != keyword && !keyword.trim().isEmpty()) {
                queryBuilder.must(CommonSearcher.builderQuery(keyword, condition.getSearchScopes()));
            }
            if (null != start) {
                QueryBuilder timeQuery = QueryBuilders.rangeQuery((type == 0 && pushType == 1) ? "manual_update_time" : "created_at")
                        .from(DateHelper.dateToString(start, "yyyy-MM-dd HH:mm:ss"))
                        .to(DateHelper.dateToString(end, "yyyy-MM-dd HH:mm:ss")).format("yyyy-MM-dd HH:mm:ss");

                queryBuilder.must(timeQuery);
            }
            if (null != emotion) {
                String emotionValue = null;
                if (1 == emotion) {
                    emotionValue = "正";
                } else if (-1 == emotion) {
                    emotionValue = "负";
                } else if (0 == emotion) {
                    emotionValue = "中";
                }
                if (null != emotionValue) {
                    queryBuilder.must(QueryBuilders.termQuery("emotion", emotionValue));
                }
            }

            if (null != pushType && null != orgId) {
                if (1 == pushType) {
                    queryBuilder.must(QueryBuilders.termQuery("push_types", orgId));
                } else if (0 == pushType) {
                    queryBuilder.mustNot(QueryBuilders.termQuery("push_types", orgId));
                }
            }
            if (null != condition.getRecommend_person()) {
                queryBuilder.must(QueryBuilders.termQuery("recommend_person", condition.getRecommend_person()));
            }
            List<String> searchTypes = new ArrayList<String>();

            List<Integer> articleTypes = condition.getArticleTypes();
            BoolQueryBuilder articleTypeQuery = QueryBuilders.boolQuery();
            boolean articleTypeFilter = false;
            if (null != articleTypes) {
                for (Integer articleType : articleTypes) {
                    if (null != articleType) {
                        if (articleType == 0) {
                            if (!searchTypes.contains(ESConstant.WEIBO_TYPE.toString())) {
                                searchTypes.add(ESConstant.WEIBO_TYPE.toString());
                            }

                        } else {
                            articleTypeFilter = true;
                            searchTypes.add(ESConstant.ARTICLE_TYPE.toString());
                            articleTypeQuery.should(QueryBuilders.termQuery("article_type", articleType));
                        }
                    }
                }
            } else {
                searchTypes.add(ESConstant.WEIBO_TYPE.toString());
                searchTypes.add(ESConstant.ARTICLE_TYPE.toString());
            }

            if (articleTypeFilter) {
                queryBuilder.must(articleTypeQuery);
            }

            String[] types = new String[searchTypes.size()];
            for (int i = 0; i < types.length; i++) {
                types[i] = searchTypes.get(i);
            }

            SearchHits hits = null;
            List<Integer> countFields = condition.getCountFields();
            long totalComments = 0;
            long totalReposts = 0;
            long totalZans = 0;
            long totalReads = 0;
            long totals = 0;
            if (countFields == null) {
                countFields = new ArrayList<Integer>();
            }
            if (countFields.contains(2)) {
                searchRequest = transportClient.prepareSearch(new String[]{ESConstant.INDEX_AMS.toString()});
                searchRequest.setTypes(types)
                        //.setTimeout("1000") // 设置超时时间 1s
                        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                        .setFetchSource(new String[]{"comments_count"}, null)
                        .setQuery(queryBuilder)
                        .setFrom(0).setSize(100); // 设置分页.
                searchRequest.addSort("comments_count", SortOrder.DESC);

                println_(new Date() + " count() called\n" +
                        "--- DSL查询 Explain ----\n"
                        + searchRequest.toString() + "\n" +
                        "-----------------------");// 打印DSL语句.

                searchResponse = searchRequest.execute().actionGet(); //执行请求.
                hits = searchResponse.getHits();
                totals = hits.getTotalHits();
                for (int i = 0; i < hits.getHits().length; i++) {
                    Object o = hits.getAt(i).getSource().get("comments_count");
                    int comments = 0;
                    try {
                        comments = Integer.parseInt(o.toString());
                    } catch (Exception e) {

                    }
                    totalComments += comments;
                }

            } else if (countFields.contains(3)) {
                searchRequest = transportClient.prepareSearch(new String[]{ESConstant.INDEX_AMS.toString()});
                searchRequest.setTypes(types)
                        //.setTimeout("1000") // 设置超时时间 1s
                        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                        .setFetchSource(new String[]{"reports_count"}, null)
                        .setQuery(queryBuilder)
                        .setFrom(0).setSize(100); // 设置分页.
                searchRequest.addSort("reports_count", SortOrder.DESC);

                searchResponse = searchRequest.execute().actionGet(); //执行请求.
                hits = searchResponse.getHits();
                totals = hits.getTotalHits();
                for (int i = 0; i < hits.getHits().length; i++) {
                    Object o = hits.getAt(i).getSource().get("reports_count");
                    int reposts = 0;
                    try {
                        reposts = Integer.parseInt(o.toString());
                    } catch (Exception e) {

                    }
                    totalReposts += reposts;
                }

            } else if (countFields.contains(4)) {
                searchRequest = transportClient.prepareSearch(new String[]{ESConstant.INDEX_AMS.toString()});
                searchRequest.setTypes(types)
                        //.setTimeout("1000") // 设置超时时间 1s
                        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                        .setFetchSource(new String[]{"zans_count"}, null)
                        .setQuery(queryBuilder)
                        .setFrom(0).setSize(100); // 设置分页.
                searchRequest.addSort("zans_count", SortOrder.DESC);

                searchResponse = searchRequest.execute().actionGet(); //执行请求.
                hits = searchResponse.getHits();
                totals = hits.getTotalHits();
                for (int i = 0; i < hits.getHits().length; i++) {
                    Object o = hits.getAt(i).getSource().get("zans_count");
                    int zans = 0;
                    try {
                        zans = Integer.parseInt(o.toString());
                    } catch (Exception e) {

                    }
                    totalZans += zans;
                }
            } else if (countFields.contains(5)) {
                searchRequest = transportClient.prepareSearch(new String[]{ESConstant.INDEX_AMS.toString()});
                searchRequest.setTypes(types)
                        //.setTimeout("1000") // 设置超时时间 1s
                        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                        .setFetchSource(new String[]{"read_count"}, null)
                        .setQuery(queryBuilder)
                        .setFrom(0).setSize(100); // 设置分页.
                searchRequest.addSort("read_count", SortOrder.DESC);

                searchResponse = searchRequest.execute().actionGet(); //执行请求.
                hits = searchResponse.getHits();
                totals = hits.getTotalHits();
                for (int i = 0; i < hits.getHits().length; i++) {
                    Object o = hits.getAt(i).getSource().get("read_count");
                    int read = 0;
                    try {
                        read = Integer.parseInt(o.toString());
                    } catch (Exception e) {

                    }
                    totalReads += read;
                }
            } else {
                searchRequest = transportClient.prepareSearch(new String[]{ESConstant.INDEX_AMS.toString()});
                searchRequest.setTypes(types)
                        //.setTimeout("1000") // 设置超时时间 1s
                        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                        .setFetchSource(new String[]{"read_count"}, null)
                        .setQuery(queryBuilder)
                        .setFrom(0).setSize(100); // 设置分页.

                println_(new Date() + " count() called\n" +
                        "--- DSL查询 Explain ----\n"
                        + searchRequest.toString() + "\n" +
                        "-----------------------");// 打印DSL语句.

                searchResponse = searchRequest.execute().actionGet(); //执行请求.
                hits = searchResponse.getHits();

                totals = hits.getTotalHits();
            }


            // 获取命中
            result.append("{'took':'" + searchResponse.getTook() + "','hits':{'total':" + totals + ",'repostsCount':" + totalReposts + ",'zansCount':" + totalZans + ",'commentsCount':" + totalComments + ",'readsCount':" + totalReads + ",'display':[]}}");

            return FastJsonHelper.deserialize(result.toString(), JSONObject.class);
        } catch (ElasticsearchException e) {
            logger.error(e.getDetailedMessage(), e);
        }
        return FastJsonHelper.deserialize(new AMSResponse(1, "es异常,请联系es管理员。").toString(), JSONObject.class);
    }

    public JSONObject countAllGroupByArticleType(CountCondition condition, String orgId, List<String> subjectIds, int type, Date start, Date end, Integer pushType, Integer emotion, String keyword) {
        initClient();
        List<Integer> articleTypes = condition.getArticleTypes();
        StringBuffer result = new StringBuffer();
        StringBuilder countBuilder = new StringBuilder();
        Map<Integer, Long> countMap = new HashMap<Integer, Long>();
        long startTime = Calendar.getInstance().getTimeInMillis();
        for (Integer articleType : articleTypes) {
            SearchResponse searchResponse = null;
            SearchRequestBuilder searchRequest = null;
            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

            try {
                String orgTag = null;
                if (type == 2) {
                    orgTag = "orgs_tag";
                } else if (type == 0) {
                    orgTag = "orgs_tag";
                } else if (type == 3) {
                    orgTag = "orgs_event_tag";
                }

                String subTag = null;
                if (type == 2) {
                    subTag = "subjects_tag";
                } else if (type == 0) {
                    subTag = "push_types";
                } else if (type == 3) {
                    subTag = "events_tag";
                }
                if (null != orgId && null != orgTag) {
                    queryBuilder.must(QueryBuilders.termQuery(orgTag, orgId));
                }
                if (null != subjectIds && subjectIds.size() > 0 && null != subTag) {
                    BoolQueryBuilder subQuery = QueryBuilders.boolQuery();
                    for (String subjectId : subjectIds) {
                        subQuery.should(QueryBuilders.termQuery(subTag, subjectId));
                    }
                    queryBuilder.must(subQuery);
                }

                if (null != keyword && !keyword.trim().isEmpty()) {
                    queryBuilder.must(CommonSearcher.builderQuery(keyword, condition.getSearchScopes()));
                }
                if (null != start) {
                    QueryBuilder timeQuery = QueryBuilders.rangeQuery((type == 0 && pushType == 1) ? "manual_update_time" : "created_at")
                            .from(DateHelper.dateToString(start, "yyyy-MM-dd HH:mm:ss"))
                            .to(DateHelper.dateToString(end, "yyyy-MM-dd HH:mm:ss")).format("yyyy-MM-dd HH:mm:ss");

                    queryBuilder.must(timeQuery);
                }
                if (null != emotion) {
                    String emotionValue = null;
                    if (1 == emotion) {
                        emotionValue = "正面";
                    } else if (-1 == emotion) {
                        emotionValue = "负面";
                    } else if (0 == emotion) {
                        emotionValue = "中性";
                    }
                    if (null != emotionValue) {
                        queryBuilder.must(QueryBuilders.termQuery("emotion", emotionValue));
                    }
                }

                if (null != pushType && null != orgId) {
                    if (1 == pushType) {
                        queryBuilder.must(QueryBuilders.termQuery("push_types", orgId));
                    } else if (0 == pushType) {
                        queryBuilder.mustNot(QueryBuilders.termQuery("push_types", orgId));
                    }
                }
                ;
                if (null != condition && null != condition.getSiteIds() && condition.getSiteIds().size() > 0) {

                    // 指定一个或多个网站
                    BoolQueryBuilder siteBuilder = QueryBuilders.boolQuery();
                    for (Integer siteId : condition.getSiteIds()) {
                        if (null != siteId) {
                            siteBuilder.should(QueryBuilders.termQuery("crawler_site_id", siteId));
                            siteBuilder.should(QueryBuilders.termQuery("site_id", siteId));
                        }

                        // 相当于or查询。
                    }
                    queryBuilder.must(siteBuilder);
                }


                if (null != condition.getMids()) {
                    queryBuilder.must(QueryBuilders.idsQuery().ids(condition.getMids()));
                }
                AggregationBuilder aggregation = AggregationBuilders
                        .terms("agg")
                        .field("article_type")
                        .order(Terms.Order.term(false));

                searchRequest = transportClient.prepareSearch(new String[]{ESConstant.INDEX_AMS.toString()});
                searchRequest.setTypes(new String[]{ESConstant.ARTICLE_TYPE.toString()})
                        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                        .setFetchSource(false)
                        .setQuery(queryBuilder)

                        .addAggregation(aggregation); // 设置分页.


                searchResponse = searchRequest.execute().actionGet(); //执行请求.
                System.out.println("agg response:" + searchResponse);


                if (null != articleType) {
                    if (articleType == 0) {


                        long totals = 0;

                        searchRequest = transportClient.prepareSearch(new String[]{ESConstant.INDEX_AMS.toString()});
                        searchRequest.setTypes(new String[]{ESConstant.WEIBO_TYPE.toString()})
                                //.setTimeout("1000") // 设置超时时间 1s
                                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                                .setFetchSource(false)
                                .setQuery(queryBuilder)
                                .setFrom(0).setSize(100); // 设置分页.


                        println_(new Date() + " count() called\n" +
                                "--- DSL查询 Explain ----\n"
                                + searchRequest.toString() + "\n" +
                                "-----------------------");// 打印DSL语句.

                        searchResponse = searchRequest.execute().actionGet(); //执行请求.
                        SearchHits hits = searchResponse.getHits();
                        totals = hits.getTotalHits();
                        countMap.put(articleType, totals);

                    } else {


                        queryBuilder.must(QueryBuilders.termQuery("article_type", articleType));
                        long totals = 0;

                        searchRequest = transportClient.prepareSearch(new String[]{ESConstant.INDEX_AMS.toString()});
                        searchRequest.setTypes(new String[]{ESConstant.ARTICLE_TYPE.toString()})
                                //.setTimeout("1000") // 设置超时时间 1s
                                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                                .setFetchSource(false)
                                .setQuery(queryBuilder)
                                .setFrom(0).setSize(100); // 设置分页.


                        println_(new Date() + " count() called\n" +
                                "--- DSL查询 Explain ----\n"
                                + searchRequest.toString() + "\n" +
                                "-----------------------");// 打印DSL语句.

                        searchResponse = searchRequest.execute().actionGet(); //执行请求.
                        SearchHits hits = searchResponse.getHits();
                        totals = hits.getTotalHits();
                        countMap.put(articleType, totals);
                    }
                }


            } catch (ElasticsearchException e) {
                logger.error(e.getDetailedMessage(), e);
                return FastJsonHelper.deserialize(new AMSResponse(1, "es异常,请联系es管理员。").toString(), JSONObject.class);
            }
        }


        for (Integer articleType : countMap.keySet()) {
            if (countBuilder.length() > 0) {
                countBuilder.append(",");
            }
            countBuilder.append("'" + articleType + "':" + countMap.get(articleType));
        }
        long endTime = Calendar.getInstance().getTimeInMillis();
        // 获取命中
        result.append("{'took':'" + (endTime - startTime) + "','hits':{" + countBuilder.toString() + ",'display':[]}}");

        return FastJsonHelper.deserialize(result.toString(), JSONObject.class);

    }

    public JSONObject countAllGroupByArticleType2(CountCondition condition, String orgId, List<String> subjectIds, int type, Date start, Date end, Integer pushType, Integer emotion, String keyword) {
        initClient();
        List<Integer> articleTypes = condition.getArticleTypes();
        StringBuffer result = new StringBuffer();
        StringBuilder countBuilder = new StringBuilder();
        Map<Integer, Long> countMap = new HashMap<Integer, Long>();
        long startTime = Calendar.getInstance().getTimeInMillis();

        SearchResponse searchResponse = null;
        SearchRequestBuilder searchRequest = null;
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

        try {
            String orgTag = null;
            if (type == 2) {
                orgTag = "orgs_tag";
            } else if (type == 0) {
                orgTag = "orgs_tag";
            } else if (type == 3) {
                orgTag = "orgs_event_tag";
            }

            String subTag = null;
            if (type == 2) {
                subTag = "subjects_tag";
            } else if (type == 0) {
                subTag = "push_types";
            } else if (type == 3) {
                subTag = "events_tag";
            }
            if (null != orgId && null != orgTag) {
                //queryBuilder.must(QueryBuilders.termQuery(orgTag, orgId));
//         	    	queryBuilder.must(QueryBuilders.boolQuery()
//            				.should(QueryBuilders.termQuery(orgTag, orgId))
//            				.should(QueryBuilders.termQuery("push_types", orgId)));//指定组织.

                BoolQueryBuilder a = QueryBuilders.boolQuery()
                        .should(QueryBuilders.termQuery(orgTag, orgId));

                if (type == 2) {
                    a.should(QueryBuilders.termQuery("push_types", orgId));//指定组织.
                }
                queryBuilder.must(a);
            }
            if (null != subjectIds && subjectIds.size() > 0 && null != subTag) {
                BoolQueryBuilder subQuery = QueryBuilders.boolQuery();
                for (String subjectId : subjectIds) {
                    subQuery.should(QueryBuilders.termQuery(subTag, subjectId));
                }
                if (null != orgId && type == 2) {
                    subQuery.should(QueryBuilders.termQuery("push_types", orgId));
                }
                queryBuilder.must(subQuery);
            }

            if (null != keyword && !keyword.trim().isEmpty()) {
                queryBuilder.must(CommonSearcher.builderQuery(keyword, condition.getSearchScopes()));
            }
            if (null != start) {
                QueryBuilder timeQuery = QueryBuilders.rangeQuery((type == 0 && pushType == 1) ? "manual_update_time" : "created_at")
                        .from(DateHelper.dateToString(start, "yyyy-MM-dd HH:mm:ss"))
                        .to(DateHelper.dateToString(end, "yyyy-MM-dd HH:mm:ss")).format("yyyy-MM-dd HH:mm:ss");

                queryBuilder.must(timeQuery);
            }


            Date crawlerStart = condition.getCrawlerStart();
            Date crawlerEnd = condition.getCrawlerEnd();

            if (null != crawlerStart && null != crawlerEnd) {
                QueryBuilder crawlerQuery = QueryBuilders.rangeQuery("crawler_time")
                        .from(DateHelper.dateToString(crawlerStart, "yyyy-MM-dd HH:mm:ss"))
                        .to(DateHelper.dateToString(crawlerEnd, "yyyy-MM-dd HH:mm:ss")).format("yyyy-MM-dd HH:mm:ss");
                //if(null==childBuilder||null!=condition.getStart()) {
                queryBuilder.must(crawlerQuery);
            }

            if (null != condition.getIsRubbish()) {
                if (condition.getIsRubbish()) {
                    queryBuilder.must(QueryBuilders.termQuery("rubbish", "1"));
                } else {
                    queryBuilder.mustNot(QueryBuilders.existsQuery("rubbish"));
                }
            }

            if (null != emotion) {
                String emotionValue = null;
                if (1 == emotion) {
                    emotionValue = "正";
                } else if (-1 == emotion) {
                    emotionValue = "负";
                } else if (0 == emotion) {
                    emotionValue = "中";
                }
                if (null != emotionValue) {
                    queryBuilder.must(QueryBuilders.termQuery("emotion", emotionValue));
                }
            }


            if (null != pushType && null != orgId) {
                if (1 == pushType) {
                    queryBuilder.must(QueryBuilders.termQuery("push_types", orgId));
                } else if (0 == pushType) {
                    queryBuilder.mustNot(QueryBuilders.termQuery("push_types", orgId));
                }
            }
            ;
            if (null != condition && null != condition.getSiteIds() && condition.getSiteIds().size() > 0) {

                // 指定一个或多个网站
                BoolQueryBuilder siteBuilder = QueryBuilders.boolQuery();
                for (Integer siteId : condition.getSiteIds()) {
                    if (null != siteId) {
                        siteBuilder.should(QueryBuilders.termQuery("crawler_site_id", siteId));
                        siteBuilder.should(QueryBuilders.termQuery("site_id", siteId));
                    }

                    // 相当于or查询。
                }
                queryBuilder.must(siteBuilder);
            }

            if (null != condition.getAuthors() && condition.getAuthors().size() > 0) {
                BoolQueryBuilder autherQuery = QueryBuilders.boolQuery();
                // 指定一个或多个专题
                for (String author : condition.getAuthors()) {
                    autherQuery.should(QueryBuilders.termQuery("name", author)); // 相当于or查询。
                }
                queryBuilder = queryBuilder.must(autherQuery);
            }

            if (null != condition.getTextLoc() && !"所有地区".equals(condition.getTextLoc().trim())) {
                queryBuilder.must(QueryBuilders.boolQuery().should(QueryBuilders.matchPhraseQuery("text", condition.getTextLoc()).slop(1)).should(QueryBuilders.matchPhraseQuery("text_loc", condition.getTextLoc()).slop(1)));

            }

            if (null != condition.getRecommend_person()) {
                queryBuilder.must(QueryBuilders.termQuery("recommend_person", condition.getRecommend_person()));
            }

            if (null != condition.getPid()) {
                String pid = condition.getPid().substring(condition.getPid().indexOf("_") + 1);
                queryBuilder.must(QueryBuilders.termQuery("mid_p", pid));
            }
            if (null != condition.getMids()) {
                queryBuilder.must(QueryBuilders.idsQuery().ids(condition.getMids()));
            }
            AggregationBuilder aggregation = AggregationBuilders
                    .terms("agg")
                    .field("article_type")
                    .order(Terms.Order.term(false));

            searchRequest = transportClient.prepareSearch(new String[]{ESConstant.INDEX_AMS.toString()});
            searchRequest.setTypes(new String[]{ESConstant.ARTICLE_TYPE.toString()})
                    //.setSearchType(SearchType.COUNT)
                    .setFetchSource(false)
                    .setQuery(queryBuilder)

                    .addAggregation(aggregation)
                    .setSize(0); // 设置分页.


            logger.info("countAllGroupByArticleType2: " + searchRequest);

            searchResponse = searchRequest.execute().actionGet(); //执行请求.
            //System.out.println("agg response:"+searchResponse);

            JSONObject json = FastJsonHelper.deserialize(searchResponse.toString(), JSONObject.class);
            JSONArray jsonArray = json.getJSONObject("aggregations").getJSONObject("agg").getJSONArray("buckets");
            for (int i = 0; i < jsonArray.size(); i++) {
                String docType = jsonArray.getJSONObject(i).getString("key");
                Long count = jsonArray.getJSONObject(i).getLong("doc_count");
                if (null == condition.getArticleTypes() || condition.getArticleTypes().contains(Integer.parseInt(docType))) {
                    countMap.put(Integer.parseInt(docType), count);
                }

            }
            if (null == condition.getArticleTypes() || condition.getArticleTypes().contains(0)) {
                searchRequest = transportClient.prepareSearch(new String[]{ESConstant.INDEX_AMS.toString()});
                searchRequest.setTypes(new String[]{ESConstant.WEIBO_TYPE.toString()})
                        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                        .setFetchSource(false)
                        .setQuery(queryBuilder);
                searchResponse = searchRequest.execute().actionGet(); //执行请求.
                long totalHits = searchResponse.getHits().getTotalHits();
                countMap.put(0, totalHits);
            }


        } catch (ElasticsearchException e) {
            logger.error(e.getDetailedMessage(), e);
            return FastJsonHelper.deserialize(new AMSResponse(1, "es异常,请联系es管理员。").toString(), JSONObject.class);
        }


        for (Integer articleType : countMap.keySet()) {
            if (countBuilder.length() > 0) {
                countBuilder.append(",");
            }
            countBuilder.append("'" + articleType + "':" + countMap.get(articleType));
        }
        long endTime = Calendar.getInstance().getTimeInMillis();
        // 获取命中
        result.append("{'took':'" + (endTime - startTime) + "','hits':{" + countBuilder.toString() + ",'display':[]}}");

        return FastJsonHelper.deserialize(result.toString(), JSONObject.class);

    }

    public JSONObject countAllGroupByDownloadType(CountCondition condition, String orgId, List<String> subjectIds, int type, Date start, Date end, Integer pushType, Integer emotion, String keyword) {
        initClient();
        List<Integer> articleTypes = condition.getArticleTypes();
        StringBuffer result = new StringBuffer();
        StringBuilder countBuilder = new StringBuilder();
        Map<Integer, Long> countMap = new HashMap<Integer, Long>();
        long startTime = Calendar.getInstance().getTimeInMillis();

        SearchResponse searchResponse = null;
        SearchRequestBuilder searchRequest = null;
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

        try {
            String orgTag = null;
            if (type == 2) {
                orgTag = "orgs_tag";
            } else if (type == 0) {
                orgTag = "orgs_tag";
            } else if (type == 3) {
                orgTag = "orgs_event_tag";
            }

            String subTag = null;
            if (type == 2) {
                subTag = "subjects_tag";
            } else if (type == 0) {
                subTag = "push_types";
            } else if (type == 3) {
                subTag = "events_tag";
            }
            if (null != orgId && null != orgTag) {
                queryBuilder.must(QueryBuilders.termQuery(orgTag, orgId));
            }
            if (null != subjectIds && subjectIds.size() > 0 && null != subTag) {
                BoolQueryBuilder subQuery = QueryBuilders.boolQuery();
                for (String subjectId : subjectIds) {
                    subQuery.should(QueryBuilders.termQuery(subTag, subjectId));
                }
                queryBuilder.must(subQuery);
            }

            if (null != keyword && !keyword.trim().isEmpty()) {
                queryBuilder.must(CommonSearcher.builderQuery(keyword, condition.getSearchScopes()));
            }
            if (null != start) {
                QueryBuilder timeQuery = QueryBuilders.rangeQuery((type == 0 && pushType == 1) ? "manual_update_time" : "created_at")
                        .from(DateHelper.dateToString(start, "yyyy-MM-dd HH:mm:ss"))
                        .to(DateHelper.dateToString(end, "yyyy-MM-dd HH:mm:ss")).format("yyyy-MM-dd HH:mm:ss");

                queryBuilder.must(timeQuery);
            }

            Date crawlerStart = condition.getCrawlerStart();
            Date crawlerEnd = condition.getCrawlerEnd();

            if (null != crawlerStart && null != crawlerEnd) {
                QueryBuilder crawlerQuery = QueryBuilders.rangeQuery("crawler_time")
                        .from(DateHelper.dateToString(crawlerStart, "yyyy-MM-dd HH:mm:ss"))
                        .to(DateHelper.dateToString(crawlerEnd, "yyyy-MM-dd HH:mm:ss")).format("yyyy-MM-dd HH:mm:ss");
                //if(null==childBuilder||null!=condition.getStart()) {
                queryBuilder.must(crawlerQuery);
            }


            if (null != condition.getIsRubbish()) {
                if (condition.getIsRubbish()) {
                    queryBuilder.must(QueryBuilders.termQuery("rubbish", "1"));
                } else {
                    queryBuilder.mustNot(QueryBuilders.existsQuery("rubbish"));
                }
            }

            if (null != emotion) {
                String emotionValue = null;
                if (1 == emotion) {
                    emotionValue = "正";
                } else if (-1 == emotion) {
                    emotionValue = "负";
                } else if (0 == emotion) {
                    emotionValue = "中";
                }
                if (null != emotionValue) {
                    queryBuilder.must(QueryBuilders.termQuery("emotion", emotionValue));
                }
            }


            if (null != pushType && null != orgId) {
                if (1 == pushType) {
                    queryBuilder.must(QueryBuilders.termQuery("push_types", orgId));
                } else if (0 == pushType) {
                    queryBuilder.mustNot(QueryBuilders.termQuery("push_types", orgId));
                }
            }
            ;
            if (null != condition && null != condition.getSiteIds() && condition.getSiteIds().size() > 0) {

                // 指定一个或多个网站
                BoolQueryBuilder siteBuilder = QueryBuilders.boolQuery();
                for (Integer siteId : condition.getSiteIds()) {
                    if (null != siteId) {
                        siteBuilder.should(QueryBuilders.termQuery("crawler_site_id", siteId));
                        siteBuilder.should(QueryBuilders.termQuery("site_id", siteId));
                    }

                    // 相当于or查询。
                }
                queryBuilder.must(siteBuilder);
            }

            if (null != condition.getTextLoc() && !"所有地区".equals(condition.getTextLoc().trim())) {
                queryBuilder.must(QueryBuilders.boolQuery().should(QueryBuilders.matchPhraseQuery("text", condition.getTextLoc()).slop(1)).should(QueryBuilders.matchPhraseQuery("text_loc", condition.getTextLoc()).slop(1)));
            }
            if (null != condition.getMids()) {
                queryBuilder.must(QueryBuilders.idsQuery().ids(condition.getMids()));
            }
            if (null != condition.getRecommend_person()) {
                queryBuilder.must(QueryBuilders.termQuery("recommend_person", condition.getRecommend_person()));
            }

            List<String> searchTypes = new ArrayList<String>();


            BoolQueryBuilder articleTypeQuery = QueryBuilders.boolQuery();
            boolean articleTypeFilter = false;
            if (null != articleTypes) {
                for (Integer articleType : articleTypes) {
                    if (null != articleType) {
                        if (articleType == 0) {
                            if (!searchTypes.contains(ESConstant.WEIBO_TYPE.toString())) {
                                searchTypes.add(ESConstant.WEIBO_TYPE.toString());
                            }
                            articleTypeQuery.should(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("article_type")));
                        } else {
                            articleTypeFilter = true;
                            searchTypes.add(ESConstant.ARTICLE_TYPE.toString());
                            articleTypeQuery.should(QueryBuilders.termQuery("article_type", articleType));
                        }
                    }
                }
            } else {
                searchTypes.add(ESConstant.WEIBO_TYPE.toString());
                searchTypes.add(ESConstant.ARTICLE_TYPE.toString());
            }

            if (articleTypeFilter) {
                queryBuilder = queryBuilder.must(articleTypeQuery);
            }

            String[] types = new String[searchTypes.size()];
            for (int i = 0; i < types.length; i++) {
                types[i] = searchTypes.get(i);
            }


            AggregationBuilder aggregation = AggregationBuilders
                    .terms("agg")
                    .field("download_type")
                    .size(50);

            searchRequest = transportClient.prepareSearch(new String[]{ESConstant.INDEX_AMS.toString()});
            searchRequest.setTypes(types)
                    //.setTimeout("1000") // 设置超时时间 1s
                    //.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                    .setFetchSource(false)
                    .setQuery(queryBuilder)
                    .setFrom(0).setSize(0)
                    .addAggregation(aggregation); // 设置分页.

            System.out.println("searchRequest:" + searchRequest);

            searchResponse = searchRequest.execute().actionGet(); //执行请求.


        } catch (ElasticsearchException e) {
            logger.error(e.getDetailedMessage(), e);
            return FastJsonHelper.deserialize(new AMSResponse(1, "es异常,请联系es管理员。").toString(), JSONObject.class);
        }


        for (Integer articleType : countMap.keySet()) {
            if (countBuilder.length() > 0) {
                countBuilder.append(",");
            }
            countBuilder.append("'" + articleType + "':" + countMap.get(articleType));
        }
        long endTime = Calendar.getInstance().getTimeInMillis();
        // 获取命中
        String buckets = FastJsonHelper.deserialize(searchResponse.toString(), JSONObject.class).getJSONObject("aggregations").getJSONObject("agg").getJSONArray("buckets").toJSONString();
        result.append("{'took':'" + (endTime - startTime) + "','hits':{" + countBuilder.toString() + ",'display':" + buckets + "}}");

        return FastJsonHelper.deserialize(result.toString(), JSONObject.class);

    }

    public JSONObject countTopNWebsite(CountCondition condition, String orgId, List<String> subjectIds, int type, Date start, Date end, Integer pushType, Integer emotion, String keyword, int topN) {
        initClient();
        List<Integer> articleTypes = condition.getArticleTypes();
        StringBuffer result = new StringBuffer();
        StringBuilder countBuilder = new StringBuilder();
        Map<Integer, Long> countMap = new HashMap<Integer, Long>();
        long startTime = Calendar.getInstance().getTimeInMillis();

        SearchResponse searchResponse = null;
        SearchRequestBuilder searchRequest = null;
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

        try {
            String orgTag = null;
            if (type == 2) {
                orgTag = "orgs_tag";
            } else if (type == 0) {
                orgTag = "orgs_tag";
            } else if (type == 3) {
                orgTag = "orgs_event_tag";
            }

            String subTag = null;
            if (type == 2) {
                subTag = "subjects_tag";
            } else if (type == 0) {
                subTag = "push_types";
            } else if (type == 3) {
                subTag = "events_tag";
            }
            if (null != orgId && null != orgTag) {
                queryBuilder.must(QueryBuilders.termQuery(orgTag, orgId));
            }
            if (null != subjectIds && subjectIds.size() > 0 && null != subTag) {
                BoolQueryBuilder subQuery = QueryBuilders.boolQuery();
                for (String subjectId : subjectIds) {
                    subQuery.should(QueryBuilders.termQuery(subTag, subjectId));
                }
                queryBuilder.must(subQuery);
            }

            if (null != keyword && !keyword.trim().isEmpty()) {
                queryBuilder.must(CommonSearcher.builderQuery(keyword, condition.getSearchScopes()));
            }
            if (null != start) {
                QueryBuilder timeQuery = QueryBuilders.rangeQuery((type == 0 && pushType == 1) ? "manual_update_time" : "created_at")
                        .from(DateHelper.dateToString(start, "yyyy-MM-dd HH:mm:ss"))
                        .to(DateHelper.dateToString(end, "yyyy-MM-dd HH:mm:ss")).format("yyyy-MM-dd HH:mm:ss");

                queryBuilder.must(timeQuery);
            }
            if (null != condition.getIsRubbish()) {
                if (condition.getIsRubbish()) {
                    queryBuilder.must(QueryBuilders.termQuery("rubbish", "1"));
                } else {
                    queryBuilder.mustNot(QueryBuilders.existsQuery("rubbish"));
                }
            }


            if (null != emotion) {
                String emotionValue = null;
                if (1 == emotion) {
                    emotionValue = "正";
                } else if (-1 == emotion) {
                    emotionValue = "负";
                } else if (0 == emotion) {
                    emotionValue = "中";
                }
                if (null != emotionValue) {
                    queryBuilder.must(QueryBuilders.termQuery("emotion", emotionValue));
                }
            }


            if (null != pushType && null != orgId) {
                if (1 == pushType) {
                    queryBuilder.must(QueryBuilders.termQuery("push_types", orgId));
                } else if (0 == pushType) {
                    queryBuilder.mustNot(QueryBuilders.termQuery("push_types", orgId));
                }
            }
            ;
            if (null != condition && null != condition.getSiteIds() && condition.getSiteIds().size() > 0) {

                // 指定一个或多个网站
                BoolQueryBuilder siteBuilder = QueryBuilders.boolQuery();
                for (Integer siteId : condition.getSiteIds()) {
                    if (null != siteId) {
                        siteBuilder.should(QueryBuilders.termQuery("crawler_site_id", siteId));
                        //siteBuilder.should(QueryBuilders.termQuery("site_id", siteId));
                    }

                    // 相当于or查询。
                }
                queryBuilder.must(siteBuilder);
            }
            if (null != condition.getAuthors() && condition.getAuthors().size() > 0) {
                BoolQueryBuilder autherQuery = QueryBuilders.boolQuery();
                // 指定一个或多个专题
                for (String author : condition.getAuthors()) {
                    autherQuery.should(QueryBuilders.termQuery("name", author)); // 相当于or查询。
                }
                queryBuilder = queryBuilder.must(autherQuery);
            }
            if (null != condition.getTextLoc() && !"所有地区".equals(condition.getTextLoc().trim())) {
                queryBuilder.must(QueryBuilders.boolQuery().should(QueryBuilders.matchPhraseQuery("text", condition.getTextLoc()).slop(1)).should(QueryBuilders.matchPhraseQuery("text_loc", condition.getTextLoc()).slop(1)));
            }

            if (null != condition.getMids()) {
                queryBuilder.must(QueryBuilders.idsQuery().ids(condition.getMids()));
            }

            if (null != condition.getRecommend_person()) {
                queryBuilder.must(QueryBuilders.termQuery("recommend_person", condition.getRecommend_person()));
            }
            List<String> searchTypes = new ArrayList<String>();


            BoolQueryBuilder articleTypeQuery = QueryBuilders.boolQuery();
            boolean articleTypeFilter = false;
            if (null != articleTypes) {
                for (Integer articleType : articleTypes) {
                    if (null != articleType) {
                        if (articleType == 0) {
                            if (!searchTypes.contains(ESConstant.WEIBO_TYPE.toString())) {
                                searchTypes.add(ESConstant.WEIBO_TYPE.toString());
                            }
                            articleTypeQuery.should(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("article_type")));
                        } else {
                            articleTypeFilter = true;
                            searchTypes.add(ESConstant.ARTICLE_TYPE.toString());
                            articleTypeQuery.should(QueryBuilders.termQuery("article_type", articleType));
                        }
                    }
                }
            } else {
                searchTypes.add(ESConstant.WEIBO_TYPE.toString());
                searchTypes.add(ESConstant.ARTICLE_TYPE.toString());
            }

            if (articleTypeFilter) {
                queryBuilder = queryBuilder.must(articleTypeQuery);
            }

            String[] types = new String[searchTypes.size()];
            for (int i = 0; i < types.length; i++) {
                types[i] = searchTypes.get(i);
            }


            AggregationBuilder aggregation = AggregationBuilders
                    .terms("agg")
                    .field("crawler_site_id")
                    .subAggregation(
                            AggregationBuilders.terms("agg2").field("emotion"));


            searchRequest = transportClient.prepareSearch(new String[]{ESConstant.INDEX_AMS.toString()});
            searchRequest.setTypes(types)
                    //.setTimeout("1000") // 设置超时时间 1s
                    //  .setSearchType(SearchType.COUNT)
                    .setFetchSource(false)
                    .setQuery(queryBuilder)
                    .setFrom(0).setSize(topN)
                    .addAggregation(aggregation); // 设置分页.

            System.out.println("topN request:" + searchRequest);

            searchResponse = searchRequest.execute().actionGet(); //执行请求.


        } catch (ElasticsearchException e) {
            logger.error(e.getDetailedMessage(), e);
            return FastJsonHelper.deserialize(new AMSResponse(1, "es异常,请联系es管理员。").toString(), JSONObject.class);
        }


        for (Integer articleType : countMap.keySet()) {
            if (countBuilder.length() > 0) {
                countBuilder.append(",");
            }
            countBuilder.append("'" + articleType + "':" + countMap.get(articleType));
        }
        long endTime = Calendar.getInstance().getTimeInMillis();
        // 获取命中
        String buckets = FastJsonHelper.deserialize(searchResponse.toString(), JSONObject.class).getJSONObject("aggregations").getJSONObject("agg").getJSONArray("buckets").toJSONString();
        result.append("{'took':'" + (endTime - startTime) + "','hits':{" + countBuilder.toString() + ",'display':" + buckets + "}}");

        return FastJsonHelper.deserialize(result.toString(), JSONObject.class);

    }

    public JSONObject countGroupBySiteIds(CountCondition condition, String orgId, List<String> subjectIds, int type, Date start, Date end, Integer pushType, Integer emotion, String keyword) {
        return this.countGroupByTag(condition, orgId, subjectIds, type, start, end, pushType, emotion, keyword, "crawler_site_id");


    }

    public JSONObject countGroupByAlarmSubjectId(CountCondition condition, String orgId, List<String> subjectIds, int type, Date start, Date end, Integer pushType, Integer emotion, String keyword) {
        return this.countGroupByTag(condition, orgId, subjectIds, type, start, end, pushType, emotion, keyword, "alerts_tag");


    }

    public JSONObject countGroupByTag(CountCondition condition, String orgId, List<String> subjectIds, int type, Date start, Date end, Integer pushType, Integer emotion, String keyword, String groupByTag) {
        initClient();
        List<Integer> articleTypes = condition.getArticleTypes();
        StringBuffer result = new StringBuffer();
        StringBuilder countBuilder = new StringBuilder();
        Map<Integer, Long> countMap = new HashMap<Integer, Long>();
        long startTime = Calendar.getInstance().getTimeInMillis();

        SearchResponse searchResponse = null;
        SearchRequestBuilder searchRequest = null;
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

        try {
            String orgTag = null;
            if (type == 2) {
                orgTag = "orgs_tag";
            } else if (type == 0) {
                orgTag = "orgs_tag";
            } else if (type == 3) {
                orgTag = "orgs_event_tag";
            }

            String subTag = null;
            if (type == 2) {
                subTag = "subjects_tag";
            } else if (type == 0) {
                subTag = "push_types";
            } else if (type == 3) {
                subTag = "events_tag";
            }
            if (null != orgId && null != orgTag) {
                queryBuilder.must(QueryBuilders.termQuery(orgTag, orgId));
            }
            if (null != subjectIds && subjectIds.size() > 0 && null != subTag) {
                BoolQueryBuilder subQuery = QueryBuilders.boolQuery();
                for (String subjectId : subjectIds) {
                    subQuery.should(QueryBuilders.termQuery(subTag, subjectId));
                }
                queryBuilder.must(subQuery);
            }

            if (null != keyword && !keyword.trim().isEmpty()) {
                queryBuilder.must(CommonSearcher.builderQuery(keyword, condition.getSearchScopes()));
            }
            if (null != start) {
                QueryBuilder timeQuery = QueryBuilders.rangeQuery((type == 0 && pushType == 1) ? "manual_update_time" : "created_at")
                        .from(DateHelper.dateToString(start, "yyyy-MM-dd HH:mm:ss"))
                        .to(DateHelper.dateToString(end, "yyyy-MM-dd HH:mm:ss")).format("yyyy-MM-dd HH:mm:ss");

                queryBuilder.must(timeQuery);
            }

            Date crawlerStart = condition.getCrawlerStart();
            Date crawlerEnd = condition.getCrawlerEnd();

            if (null != crawlerStart && null != crawlerEnd) {
                QueryBuilder crawlerQuery = QueryBuilders.rangeQuery("crawler_time")
                        .from(DateHelper.dateToString(crawlerStart, "yyyy-MM-dd HH:mm:ss"))
                        .to(DateHelper.dateToString(crawlerEnd, "yyyy-MM-dd HH:mm:ss")).format("yyyy-MM-dd HH:mm:ss");
                //if(null==childBuilder||null!=condition.getStart()) {
                queryBuilder.must(crawlerQuery);
            }


            if (null != condition.getIsRubbish()) {
                if (condition.getIsRubbish()) {
                    queryBuilder.must(QueryBuilders.termQuery("rubbish", "1"));
                } else {
                    queryBuilder.mustNot(QueryBuilders.existsQuery("rubbish"));
                }
            }

            if (null != emotion) {
                String emotionValue = null;
                if (1 == emotion) {
                    emotionValue = "正";
                } else if (-1 == emotion) {
                    emotionValue = "负";
                } else if (0 == emotion) {
                    emotionValue = "中";
                }
                if (null != emotionValue) {
                    queryBuilder.must(QueryBuilders.termQuery("emotion", emotionValue));
                }
            }


            if (null != pushType && null != orgId) {
                if (1 == pushType) {
                    queryBuilder.must(QueryBuilders.termQuery("push_types", orgId));
                } else if (0 == pushType) {
                    queryBuilder.mustNot(QueryBuilders.termQuery("push_types", orgId));
                }
            }
            ;
            if (null != condition && null != condition.getSiteIds() && condition.getSiteIds().size() > 0) {

                // 指定一个或多个网站
                BoolQueryBuilder siteBuilder = QueryBuilders.boolQuery();
                for (Integer siteId : condition.getSiteIds()) {
                    if (null != siteId) {
                        siteBuilder.should(QueryBuilders.termQuery("crawler_site_id", siteId));
                    }

                    // 相当于or查询。
                }
                queryBuilder.must(siteBuilder);
            }
            if (null != condition.getAuthors() && condition.getAuthors().size() > 0) {
                BoolQueryBuilder autherQuery = QueryBuilders.boolQuery();
                // 指定一个或多个专题
                for (String author : condition.getAuthors()) {
                    autherQuery.should(QueryBuilders.termQuery("name", author)); // 相当于or查询。
                }
                queryBuilder = queryBuilder.must(autherQuery);
            }
            if (null != condition.getTextLoc() && !"所有地区".equals(condition.getTextLoc().trim())) {
                queryBuilder.must(QueryBuilders.boolQuery().should(QueryBuilders.matchPhraseQuery("text", condition.getTextLoc()).slop(1)).should(QueryBuilders.matchPhraseQuery("text_loc", condition.getTextLoc()).slop(1)));
            }
            if (null != condition.getMids()) {
                queryBuilder.must(QueryBuilders.idsQuery().ids(condition.getMids()));
            }
            if (null != condition.getRecommend_person()) {
                queryBuilder.must(QueryBuilders.termQuery("recommend_person", condition.getRecommend_person()));
            }

            List<String> searchTypes = new ArrayList<String>();


            BoolQueryBuilder articleTypeQuery = QueryBuilders.boolQuery();
            boolean articleTypeFilter = false;
            if (null != articleTypes) {
                for (Integer articleType : articleTypes) {
                    if (null != articleType) {
                        if (articleType == 0) {
                            if (!searchTypes.contains(ESConstant.WEIBO_TYPE.toString())) {
                                searchTypes.add(ESConstant.WEIBO_TYPE.toString());
                            }
                            articleTypeQuery.should(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("article_type")));
                        } else {
                            articleTypeFilter = true;
                            searchTypes.add(ESConstant.ARTICLE_TYPE.toString());
                            articleTypeQuery.should(QueryBuilders.termQuery("article_type", articleType));
                        }
                    }
                }
            } else {
                searchTypes.add(ESConstant.WEIBO_TYPE.toString());
                searchTypes.add(ESConstant.ARTICLE_TYPE.toString());
            }

            if (articleTypeFilter) {
                queryBuilder = queryBuilder.must(articleTypeQuery);
            }

            String[] types = new String[searchTypes.size()];
            for (int i = 0; i < types.length; i++) {
                types[i] = searchTypes.get(i);
            }


            AggregationBuilder aggregation = AggregationBuilders
                    .terms("agg")
                    .field(groupByTag)
                    .size(10000)

                    .order(Terms.Order.term(false));

            searchRequest = transportClient.prepareSearch(new String[]{ESConstant.INDEX_AMS.toString()});
            searchRequest.setTypes(types)
                    //.setTimeout("1000") // 设置超时时间 1s
                    //.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                    .setFetchSource(false)
                    .setQuery(queryBuilder)
                    .setFrom(0).setSize(50)
                    .addAggregation(aggregation); // 设置分页.


            searchResponse = searchRequest.execute().actionGet(); //执行请求.
            //System.out.println("response:"+searchResponse);


        } catch (ElasticsearchException e) {
            logger.error(e.getDetailedMessage(), e);
            return FastJsonHelper.deserialize(new AMSResponse(1, "es异常,请联系es管理员。").toString(), JSONObject.class);
        }


        for (Integer articleType : countMap.keySet()) {
            if (countBuilder.length() > 0) {
                countBuilder.append(",");
            }
            countBuilder.append("'" + articleType + "':" + countMap.get(articleType));
        }
        long endTime = Calendar.getInstance().getTimeInMillis();
        // 获取命中
        String buckets = FastJsonHelper.deserialize(searchResponse.toString(), JSONObject.class).getJSONObject("aggregations").getJSONObject("agg").getJSONArray("buckets").toJSONString();
        result.append("{'took':'" + (endTime - startTime) + "','hits':{" + countBuilder.toString() + ",'display':" + buckets + "}}");

        return FastJsonHelper.deserialize(result.toString(), JSONObject.class);

    }

    public JSONObject countGroupByCity(CountCondition condition, String orgId, List<String> subjectIds, int type, Date start, Date end, Integer pushType, Integer emotion, String keyword) {
        initClient();
        List<Integer> articleTypes = condition.getArticleTypes();
        StringBuffer result = new StringBuffer();
        StringBuilder countBuilder = new StringBuilder();
        Map<Integer, Long> countMap = new HashMap<Integer, Long>();
        long startTime = Calendar.getInstance().getTimeInMillis();

        SearchResponse searchResponse = null;
        SearchRequestBuilder searchRequest = null;
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

        try {
            String orgTag = null;
            if (type == 2) {
                orgTag = "orgs_tag";
            } else if (type == 0) {
                orgTag = "orgs_tag";
            } else if (type == 3) {
                orgTag = "orgs_event_tag";
            }

            String subTag = null;
            if (type == 2) {
                subTag = "subjects_tag";
            } else if (type == 0) {
                subTag = "push_types";
            } else if (type == 3) {
                subTag = "events_tag";
            }
            if (null != orgId && null != orgTag) {
                queryBuilder.must(QueryBuilders.termQuery(orgTag, orgId));
            }
            if (null != subjectIds && subjectIds.size() > 0 && null != subTag) {
                BoolQueryBuilder subQuery = QueryBuilders.boolQuery();
                for (String subjectId : subjectIds) {
                    subQuery.should(QueryBuilders.termQuery(subTag, subjectId));
                }
                queryBuilder.must(subQuery);
            }

            if (null != keyword && !keyword.trim().isEmpty()) {
                queryBuilder.must(CommonSearcher.builderQuery(keyword, condition.getSearchScopes()));
            }
            if (pushType == null) {
                pushType = 1;
            }
            if (null != condition.getStart()) {
                QueryBuilder timeQuery = QueryBuilders.rangeQuery("created_at")
                        .from(DateHelper.dateToString(start, "yyyy-MM-dd HH:mm:ss"))
                        .to(DateHelper.dateToString(end, "yyyy-MM-dd HH:mm:ss")).format("yyyy-MM-dd HH:mm:ss");

                queryBuilder.must(timeQuery);
            }
            if (null != condition.getRecommendStart() && (type == 0 && pushType == 1)) {
                QueryBuilder timeQuery = QueryBuilders.rangeQuery("manual_update_time")
                        .from(DateHelper.dateToString(condition.getRecommendStart(), "yyyy-MM-dd HH:mm:ss"))
                        .to(DateHelper.dateToString(condition.getRecommendEnd(), "yyyy-MM-dd HH:mm:ss")).format("yyyy-MM-dd HH:mm:ss");

                queryBuilder.must(timeQuery);
            }
            Date crawlerStart = condition.getCrawlerStart();
            Date crawlerEnd = condition.getCrawlerEnd();

            if (null != crawlerStart && null != crawlerEnd) {
                QueryBuilder crawlerQuery = QueryBuilders.rangeQuery("crawler_time")
                        .from(DateHelper.dateToString(crawlerStart, "yyyy-MM-dd HH:mm:ss"))
                        .to(DateHelper.dateToString(crawlerEnd, "yyyy-MM-dd HH:mm:ss")).format("yyyy-MM-dd HH:mm:ss");
                //if(null==childBuilder||null!=condition.getStart()) {
                queryBuilder.must(crawlerQuery);
            }


            if (null != condition.getIsRubbish()) {
                if (condition.getIsRubbish()) {
                    queryBuilder.must(QueryBuilders.termQuery("rubbish", "1"));
                } else {
                    queryBuilder.mustNot(QueryBuilders.existsQuery("rubbish"));
                }
            }

            if (null != emotion) {
                String emotionValue = null;
                if (1 == emotion) {
                    emotionValue = "正";
                } else if (-1 == emotion) {
                    emotionValue = "负";
                } else if (0 == emotion) {
                    emotionValue = "中";
                }
                if (null != emotionValue) {
                    queryBuilder.must(QueryBuilders.termQuery("emotion", emotionValue));
                }
            }


            if (null != pushType && null != orgId) {
                if (1 == pushType) {
                    queryBuilder.must(QueryBuilders.termQuery("push_types", orgId));
                } else if (0 == pushType) {
                    queryBuilder.mustNot(QueryBuilders.termQuery("push_types", orgId));
                }
            }
            ;
            if (null != condition && null != condition.getSiteIds() && condition.getSiteIds().size() > 0) {

                // 指定一个或多个网站
                BoolQueryBuilder siteBuilder = QueryBuilders.boolQuery();
                for (Integer siteId : condition.getSiteIds()) {
                    if (null != siteId) {
                        siteBuilder.should(QueryBuilders.termQuery("crawler_site_id", siteId));
                        siteBuilder.should(QueryBuilders.termQuery("site_id", siteId));
                    }

                    // 相当于or查询。
                }
                queryBuilder.must(siteBuilder);
            }

            if (null != condition.getTextLoc() && !"所有地区".equals(condition.getTextLoc().trim())) {
                queryBuilder.must(QueryBuilders.boolQuery().should(QueryBuilders.matchPhraseQuery("text", condition.getTextLoc()).slop(1)).should(QueryBuilders.matchPhraseQuery("text_loc", condition.getTextLoc()).slop(1)));
            }
            if (null != condition.getMids()) {
                queryBuilder.must(QueryBuilders.idsQuery().ids(condition.getMids()));
            }


            List<String> searchTypes = new ArrayList<String>();


            BoolQueryBuilder articleTypeQuery = QueryBuilders.boolQuery();
            boolean articleTypeFilter = false;
            if (null != articleTypes) {
                for (Integer articleType : articleTypes) {
                    if (null != articleType) {
                        if (articleType == 0) {
                            if (!searchTypes.contains(ESConstant.WEIBO_TYPE.toString())) {
                                searchTypes.add(ESConstant.WEIBO_TYPE.toString());
                            }
                            articleTypeQuery.should(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("article_type")));
                        } else {
                            articleTypeFilter = true;
                            searchTypes.add(ESConstant.ARTICLE_TYPE.toString());
                            articleTypeQuery.should(QueryBuilders.termQuery("article_type", articleType));
                        }
                    }
                }
            } else {
                searchTypes.add(ESConstant.WEIBO_TYPE.toString());
                searchTypes.add(ESConstant.ARTICLE_TYPE.toString());
            }

            if (articleTypeFilter) {
                queryBuilder = queryBuilder.must(articleTypeQuery);
            }

            String[] types = new String[searchTypes.size()];
            for (int i = 0; i < types.length; i++) {
                types[i] = searchTypes.get(i);
            }

            MatchQueryBuilder match = new MatchQueryBuilder("content", "a");
            match.analyzer("ik");

            //    queryBuilder.must(match);


            AggregationBuilder aggregation = AggregationBuilders


                    .terms("agg")
                    .field("text_loc_city")
                    //.collectMode(SubAggCollectionMode.DEPTH_FIRST)

                    //.order(Terms.Order.term(false))
                    ;

            searchRequest = transportClient.prepareSearch(new String[]{ESConstant.INDEX_AMS.toString()});
            searchRequest.setTypes(types)
                    //.setTimeout("1000") // 设置超时时间 1s
                    .setSearchType(SearchType.COUNT)
                    .setFetchSource(false)
                    .setQuery(queryBuilder)
                    .setFrom(0).setSize(100)
                    .addAggregation(aggregation); // 设置分页.

            System.out.println(searchRequest);

            searchResponse = searchRequest.execute().actionGet(); //执行请求.
            //System.out.println("response:"+searchResponse);


        } catch (ElasticsearchException e) {
            logger.error(e.getDetailedMessage(), e);
            return FastJsonHelper.deserialize(new AMSResponse(1, "es异常,请联系es管理员。").toString(), JSONObject.class);
        }


        for (Integer articleType : countMap.keySet()) {

            if (countBuilder.length() > 0) {

                countBuilder.append(",");
            }
            countBuilder.append("'" + articleType + "':" + countMap.get(articleType));
        }
        long endTime = Calendar.getInstance().getTimeInMillis();
        // 获取命中
        String buckets = FastJsonHelper.deserialize(searchResponse.toString(), JSONObject.class).getJSONObject("aggregations").getJSONObject("agg").getJSONArray("buckets").toJSONString();
        JSONArray jsons = FastJsonHelper.deserialize(searchResponse.toString(), JSONObject.class).getJSONObject("aggregations").getJSONObject("agg").getJSONArray("buckets");
        System.out.println("city json:" + jsons);
        StringBuilder c = new StringBuilder("[");
        for (int k = 0; k < jsons.size(); k++) {
            String key = jsons.getJSONObject(k).get("key").toString();
            String realKey = null;
            if ("宁".equals(key)) {
                realKey = "长宁区";
            } else if ("闵".equals(key)) {
                realKey = "闵行区";
            } else if ("静".equals(key) || "安".equals(key)) {
                realKey = "静安区";
            } else if ("宝".equals(key)) {
                realKey = "宝山区";
            } else if ("黄".equals(key)) {
                realKey = "黄浦区";
            } else if ("汇".equals(key)) {
                realKey = "徐汇区";
            } else if ("陀".equals(key)) {
                realKey = "普陀区";
            } else if ("口".equals(key)) {
                realKey = "虹口区";
            } else if ("定".equals(key)) {
                realKey = "嘉定区";
            } else if ("杨".equals(key)) {
                realKey = "杨浦区";
            } else if ("新".equals(key) || "浦东".equals(key)) {
                realKey = "浦东新区";
            } else if ("松".equals(key)) {
                realKey = "松江区";
            }
            if (null != realKey) {
                if (c.length() > 1) {
                    c.append(",");
                }
                c.append("{\"key\":\"" + realKey + "\",\"doc_count\":" + jsons.getJSONObject(k).get("doc_count") + "}");
            }


        }
        c.append("]");


        result.append("{'took':'" + (endTime - startTime) + "','hits':{" + countBuilder.toString() + ",'display':" + c + "}}");

        return FastJsonHelper.deserialize(result.toString(), JSONObject.class);

    }

    /**
     * @param INDEX_AMS        索引名;
     * @param child_type       子类型;
     * @param parent_type      父类型;
     * @param child_condition  子条件;
     * @param parent_condition 父条件;
     * @param paginator        分页器;
     * @param fields           查询返回字段列表;
     * @return json格式的索引列表;
     * @desc es关联查询.
     */
    public JSONObject nestedSearch(String orgId, String INDEX_AMS,
                                   String[] searchTypes,
                                   QueryBuilder child_condition, QueryBuilder parent_condition,
                                   Paginator paginator, String... fields) {
        SearchResponse searchResponse = null;
        SearchRequestBuilder searchRequest = null;
        StringBuffer result = new StringBuffer();
        try {


            searchRequest = transportClient.prepareSearch(new String[]{INDEX_AMS});

            BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();


            if (null != parent_condition) {
                queryBuilder.must(parent_condition);
            }

            if (null != fields && fields.length > 0) {
                List<String> list = new ArrayList<String>();
                for (String field : fields) {
//                		if("reports_count".equals(field)){//以前提供接口单词错误
//                			list.add("reposts_count");
//                		}else{
                    list.add(field);
//                		}

                }
                if (!list.contains("mid")) {
                    list.add("mid");
                }
                fields = new String[list.size()];
                for (int i = 0; i < fields.length; i++) {
                    fields[i] = list.get(i);
                }
            }

            if (null != searchTypes && searchTypes.length == 1) {

                searchRequest.setTypes(searchTypes);
            }
            searchRequest.
                    setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                    .setFetchSource(fields, null)//指定返回字段.
                    .setQuery(
                            queryBuilder                                         //父条件
                    ).setRequestCache(true)
                    .setFrom(paginator.getFrom()).setSize(paginator.getSize());                           //设置分页

            // 排序.
            if (paginator.getSortOrder() != null) {
                String orderField = paginator.getField();
//                	if("reports_count".equals(orderField)){
//                		orderField = "reposts_count";
//                	}
                searchRequest.addSort(orderField, paginator.getSortOrder());
            }


            logger.info(searchRequest.toString());// 打印DSL语句.

            searchResponse = searchRequest.execute().actionGet(); //执行请求.


            // 获取命中
            SearchHits hits = searchResponse.getHits();


            result.append(("{'took':'" + searchResponse.getTook() + "','hits':{'total':" + hits.getTotalHits() + ",'display':[").replaceAll("'", "\""));
            // 不指定from size默认显示10条.
            // 判断。。。。
            int count = hits.getHits().length < paginator.getSize() ? (int) hits.getHits().length : paginator.getSize();
            //               Map<String,String> emotionMap = null;

//                if("t_status_weibo".equals(parent_type)&&null!=orgId&&(null==fields||Arrays.asList(fields).contains("emotion"))){
//
//                	 List<String> ids = new ArrayList<String>();
//                	   for (int i = 0; i < count; i++) {
//                		   ids.add(hits.getAt(i).getId());
//                	   }
//                	emotionMap=hbaseAPI.getEmotions(orgId, ids);
//                	System.out.println(emotionMap);
//                }
            logger.info("hits:" + hits.getHits().length + ",took:" + searchResponse.getTook());
            for (int i = 0; i < count; i++) {


                String source = hits.getAt(i).getSourceAsString();


                source = source.substring(0, source.length() - 1);
                result.append(source);
                result.append(",\"_id\":\"" + hits.getAt(i).getId() + "\"");

                if (CheckFieldHelper.isWeiboMid(hits.getAt(i).getId()) && !source.contains("\"article_type\"")) {
                    result.append(",\"article_type\":\"0\"");
                }
                result.append(orgId == null ? "" : ",\"org_id\":\"" + orgId + "\"");

                result.append("}");
//                    result.append(hits.getAt(i).getSourceAsString());

                if (i < count - 1) {
                    result.append(",");
                }
            }
            result.append("]}}");


            return FastJsonHelper.deserialize(result.toString(), JSONObject.class);
        } catch (ElasticsearchException e) {
            e.printStackTrace();
            logger.error(e.getDetailedMessage());
        }
        return FastJsonHelper.deserialize(new AMSResponse(1, "es异常,请联系es管理员。").toString(), JSONObject.class);
    }

    public JSONObject nestedParentSearch(String orgId, String INDEX_AMS,
                                         String[] searchTypes,
                                         QueryBuilder child_condition, QueryBuilder parent_condition,
                                         Paginator paginator, List<String> subIdSortList, String... fields) {
        SearchResponse searchResponse = null;
        SearchRequestBuilder searchRequest = null;
        StringBuffer result = new StringBuffer();
        try {
// 判断索引类型是否存在。
            if (true) {
                searchRequest = transportClient.prepareSearch(new String[]{INDEX_AMS});


                BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
                if (null != parent_condition) {
                    queryBuilder.must(parent_condition);
                }

                if (null != child_condition) {
                    queryBuilder.must(child_condition);
                }

//queryBuilder.must(QueryBuilders.existsQuery("text"));

                if (null != fields && fields.length > 0) {
                    List<String> list = new ArrayList<String>();
                    for (String field : fields) {

//		if("reports_count".equals(field)){//以前提供接口单词错误
//			list.add("reposts_count");
//		}else{
                        list.add(field);
                        //}
                    }


                    if (!list.contains("subjects_tag")) {
                        list.add("subjects_tag");
                    }
                    if (!list.contains("orgs_tag")) {
                        list.add("orgs_tag");
                    }

                    if (!list.contains("push_types")) {
                        list.add("push_types");
                    }
                    if (!list.contains("mid")) {
                        list.add("mid");
                    }
                    fields = new String[list.size()];
                    for (int i = 0; i < fields.length; i++) {
                        fields[i] = list.get(i);
                    }
                }


                if (null != searchTypes && searchTypes.length == 1) {
                    searchRequest.setTypes(searchTypes);
                }
                searchRequest
//.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                        .setFetchSource(fields, null)//指定返回字段.
                        .setQuery(
                                queryBuilder                                         //父条件
                        )

                        .setFrom(paginator.getFrom()).setSize(paginator.getSize());                           //设置分页


//if("t_alert_subject".equals(child_type)){
//	searchRequest.addSort("push_types", SortOrder.DESC);
//}
                if (subIdSortList != null && subIdSortList.size() > 0) {
                    for (String subId : subIdSortList) {
                        FieldSortBuilder sortBuilder = new FieldSortBuilder("top_time_" + subId).order(paginator.getSortOrder().DESC).ignoreUnmapped(true);
                        searchRequest.addSort(sortBuilder);
                    }
                }
// 排序.
                if (paginator.getSortOrder() != null) {
                    String orderField = paginator.getField();
//	if("reports_count".equals(orderField)){
//		orderField = "reposts_count";
//	}
                    searchRequest.addSort(orderField, paginator.getSortOrder());

                }


                logger.info(searchRequest.toString());// 打印DSL语句.

                searchResponse = searchRequest.execute().actionGet(); //执行请求.


// 获取命中
                SearchHits hits = searchResponse.getHits();


                result.append(("{'took':'" + searchResponse.getTook() + "','hits':{'total':" + hits.getTotalHits() + ",'display':[").replaceAll("'", "\""));
// 不指定from size默认显示10条.
// 判断。。。。
                int count = hits.getHits().length < paginator.getSize() ? (int) hits.getHits().length : paginator.getSize();

                logger.info("hits:" + hits.getHits().length + ",took:" + searchResponse.getTook());
                for (int i = 0; i < count; i++) {


                    String source = hits.getAt(i).getSourceAsString();


                    source = source.substring(0, source.length() - 1);
                    result.append(source);
                    result.append(",\"_id\":\"" + hits.getAt(i).getId() + "\"");

                    if (CheckFieldHelper.isWeiboMid(hits.getAt(i).getId()) && !source.contains("\"article_type\"")) {
                        result.append(",\"article_type\":\"0\"");
                    }
                    result.append("}");
//result.append(hits.getAt(i).getSourceAsString());

                    if (i < count - 1) {
                        result.append(",");
                    }
                }
                result.append("]}}");
            } else {
// 索引不存在.
                result.append(new AMSResponse(1, "index [" + INDEX_AMS + "] not found").toString());
            }

            return FastJsonHelper.deserialize(result.toString().replace("reposts_count", "reports_count"), JSONObject.class);
        } catch (ElasticsearchException e) {
            e.printStackTrace();
            logger.error(e.getDetailedMessage(), e);
        }
        return FastJsonHelper.deserialize(new AMSResponse(1, "es异常,请联系es管理员。").toString(), JSONObject.class);
    }

    // ------------------------------- other ------------------------------------

    public JSONObject searchSimilarities(List<HanmingCondition> conditions, Paginator paginator) {

        FTStatusWeibo weibo = new FTStatusWeibo();
        // 获取索引元数据信息;


        SearchResponse searchResponse = null;
        SearchRequestBuilder searchRequest = null;
        StringBuilder result = new StringBuilder();

        try {

            long start = Calendar.getInstance().getTimeInMillis();
            result.append("{\"result\":[");
            int k = 0;
            for (int i = 0; i < conditions.size(); i++) {
                HanmingCondition condition = conditions.get(i);
                if (result.toString().contains("_id")) {
                    result.append(",");
                }


                String mid = condition.getMid();

                result.append("{'mid':'").append(mid).append("','similarities':");
                String hanmingCode = condition.getHanmingCode();
                StringBuilder midsBuilder = new StringBuilder("[");
                if (null != hanmingCode && isValidHanmingCode(hanmingCode)) {

                    searchRequest = transportClient.prepareSearch(new String[]{ESConstant.INDEX_AMS.toString()});
                    searchRequest.setTypes(new String[]{"t_status_weibo", "t_article"}).
                            setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                            .setFetchSource(false)//指定返回字段.
                            .setQuery(QueryBuilders.termQuery("hanmingCode", hanmingCode));
                    if (null != paginator) {
                        searchRequest.setFrom(paginator.getFrom()).setSize(paginator.getSize());
                    }


                    // .setQuery(QueryBuilders.termQuery("content", "黑社交"))

                    if (null != paginator && paginator.getSortOrder() != null) {
                        String orderField = paginator.getField();
                        if ("reports_count".equals(orderField)) {
                            orderField = "reposts_count";
                        }
                        searchRequest.addSort(orderField, paginator.getSortOrder());
                    }

                    k++;
                    logger.info(searchRequest);
                    searchResponse = searchRequest.execute().actionGet(); //执行请求.
                    logger.info("search by hanmingCode:" + hanmingCode + ",cost " + searchResponse.getTook() + " ms");
                    // 获取命中
                    SearchHits hits = searchResponse.getHits();
                    long totalHits = hits.getHits().length;

                    for (int j = 0; j < totalHits; j++) {
                        String id = hits.getAt(j).getId();
                        if (mid.equals(id)) {
                            continue;
                        }
                        if (midsBuilder.length() > 1) {
                            midsBuilder.append(",");
                        }

                        midsBuilder.append("'" + id + "'");
                    }

                }
                midsBuilder.append("]}");
                result.append(midsBuilder);

            }
            long end = Calendar.getInstance().getTimeInMillis();
            result.append("],\"took\":\"" + (end - start) + "ms\"}");
            logger.info("searchSimilarities " + k + " hanmingCode, cost:" + (end - start) + "ms");
            return FastJsonHelper.deserialize(result.toString().replaceAll("'", "\""), JSONObject.class);
        } catch (Exception e) {
            e.printStackTrace();
            return FastJsonHelper.deserialize(new AMSResponse(1, "es异常,请联系es管理员。").toString(), JSONObject.class);
        }


    }

    public JSONObject searchSimilarities(String... ids) {

        FTStatusWeibo weibo = new FTStatusWeibo();
        // 获取索引元数据信息;
        JSONObject index_meta = ParseBeanHelper.parseBean(weibo);
        String INDEX_AMS = index_meta.getString("index");
        String index_type = index_meta.getString("type");
        SearchResponse searchResponse = null;
        SearchRequestBuilder searchRequest = null;
        StringBuilder result = new StringBuilder();

        try {
            JSONObject hanmingCodes = this.idsSearch(null, INDEX_AMS, new String[]{ESConstant.ARTICLE_TYPE.toString(), ESConstant.WEIBO_TYPE.toString()}, ids, null, "hanmingCode");
            JSONArray jsonArray = hanmingCodes.getJSONObject("hits").getJSONArray("display");
            long start = Calendar.getInstance().getTimeInMillis();
            result.append("{\"result\":[");
            int k = 0;
            for (int i = 0; i < jsonArray.size(); i++) {
                if (result.toString().contains("_id")) {
                    result.append(",");
                }

                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String mid = jsonObject.getString("_id");

                result.append("{'mid':'").append(mid).append("','similarities':");
                Object hanmingCodeObj = jsonObject.get("hanmingCode");
                StringBuilder midsBuilder = new StringBuilder("[");
                if (null != hanmingCodeObj && isValidHanmingCode(hanmingCodeObj.toString())) {
                    String hanmingCode = hanmingCodeObj.toString();
                    searchRequest = transportClient.prepareSearch(new String[]{INDEX_AMS});
                    searchRequest.setTypes(new String[]{index_type}).
                            setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                            .setFetchSource(false)//指定返回字段.
                            .setQuery(QueryBuilders.termQuery("hanmingCode", hanmingCode));
                    // .setQuery(QueryBuilders.termQuery("content", "黑社交"))

                    k++;
                    logger.info(searchRequest);
                    searchResponse = searchRequest.execute().actionGet(); //执行请求.
                    logger.info("search by hanmingCode:" + hanmingCode + ",cost " + searchResponse.getTook() + " ms");
                    // 获取命中
                    SearchHits hits = searchResponse.getHits();
                    long totalHits = hits.getHits().length;

                    for (int j = 0; j < totalHits; j++) {
                        String id = hits.getAt(j).getId();
                        if (mid.equals(id)) {
                            continue;
                        }
                        if (midsBuilder.length() > 1) {
                            midsBuilder.append(",");
                        }

                        midsBuilder.append("'" + id + "'");
                    }

                }
                midsBuilder.append("]}");
                result.append(midsBuilder);

            }
            long end = Calendar.getInstance().getTimeInMillis();
            result.append("],\"took\":\"" + (end - start) + "ms\"}");
            logger.info("searchSimilarities " + k + " hanmingCode, cost:" + (end - start) + "ms");
            return FastJsonHelper.deserialize(result.toString().replaceAll("'", "\""), JSONObject.class);
        } catch (Exception e) {
            e.printStackTrace();
            return FastJsonHelper.deserialize(new AMSResponse(1, "es异常,请联系es管理员。").toString(), JSONObject.class);
        }


    }

    /**
     * @param message 要打印的信息;
     * @desc 打印信息.
     */
    public void println_(Object message) {
        System.out.println(message);
    }

    public void deleteWeibos(List<String> ids) {
        String[] mids = new String[ids.size()];
        for (int i = 0; i < mids.length; i++) {
            mids[i] = ids.get(i);
        }
        JSONObject j = deleteByIDS(ESConstant.INDEX_AMS.toString(), "t_status_weibo", mids);
        System.out.println(j);
    }
}