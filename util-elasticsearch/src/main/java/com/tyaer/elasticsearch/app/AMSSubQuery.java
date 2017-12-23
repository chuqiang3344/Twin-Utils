package com.tyaer.elasticsearch.app;

import com.tyaer.elasticsearch.conf.DTO;
import com.tyaer.elasticsearch.manage.EsClientMananger;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by Twin on 2017/11/20.
 */
public class AMSSubQuery {
    private static final Logger logger = Logger.getLogger(AMSSubQuery.class);

    public static void main(String[] args) {
        String es_hosts = DTO.configFileReader.getConfigValue("es.hosts");
        String esClusterName = DTO.configFileReader.getConfigValue("es.cluster.name");
        EsClientMananger esClientMananger = new EsClientMananger(es_hosts, esClusterName);
        TransportClient transportClient = esClientMananger.getEsClient();
        String index = DTO.configFileReader.getConfigValue("es.index");
        SearchRequestBuilder searchRequest = transportClient.prepareSearch(index);

        /**
         * DSL查询语句组装
         */
        BoolQueryBuilder queryBuilder0 = getQueryBuilder0();//todo
        //

        /***/

        /**
         * 查询设置
         */
        String[] types = new String[]{};
//        types = new String[]{"t_article"};
        if (types != null && types.length > 0) {
            searchRequest.setTypes(types);
        }
        int pageSize = 10;
        Scroll scoll = new Scroll(new TimeValue(600000));//翻页器,保持游标查询窗口一分钟。
        searchRequest.addSort("created_at", SortOrder.DESC);//排序规则
        searchRequest.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                // .setFetchSource(new String[]{"mid","emotion","zan_count","comments_count","reposts_count","created_at"}, null)//指定返回字段.
//                .setFetchSource(new String[]{"subjects_tag", "orgs_tag"}, null)//指定返回字段.
                .setQuery(queryBuilder0)
                .setSize(pageSize)
                .setScroll(scoll);

        //打印DSL语句到控制台
        if (types != null && types.length > 0) {
            StringBuilder types_str = new StringBuilder();
            for (String type : types) {
                if (types_str.length() > 0) {
                    types_str.append(",");
                }
                types_str.append(type);
            }
            logger.info("\nGET /" + index + "/" + types_str + "/_search" + "\n" + searchRequest.toString());// 打印DSL语句.
        } else {
            logger.info("\nGET /" + index + "/_search" + "\n" + searchRequest.toString());// 打印DSL语句.
        }


        /**
         * 是否需要翻页
         */
        boolean isPaging = true;
        /**
         * 翻页页数，等于0则无限制
         */
        int pageMax = 0;
        try {
            SearchResponse searchResponse = searchRequest.execute().get();

            int sum = 0;
            int pageNum = 1;
            while (pageNum == 1 || (isPaging && (pageMax == 0 || pageNum < pageMax))) {

                String scrollId = searchResponse.getScrollId();
                logger.info("###翻页查询，页数：" + pageNum + "，开始ID：" + scrollId);
                pageNum++;

                SearchHits hits = searchResponse.getHits();
                long totalHits = hits.getTotalHits();
                SearchHit[] searchHits = hits.getHits();
                logger.info("当前查询条件下的记录数：" + totalHits + "，单次返回条数：" + searchHits.length);
                sum += searchHits.length;
                for (SearchHit searchHit : searchHits) {
//                    System.out.println(searchHit.getSourceAsString());
                    Map<String, Object> source = searchHit.getSource();
                    String id = searchHit.getId();
                    source.put("_id", id);
                    System.out.println(source);
                    /**
                     * 数据操作
                     */
                    dataHandle(source);//todo
                }

                /**
                 * scroll翻页操作
                 */
                searchResponse = transportClient.prepareSearchScroll(scrollId).setScrollId(scrollId).setScroll(scoll).get();

//                String scrollId2 = searchResponse.getScrollId();//全部是一样的值
//                logger.info(scrollId.equals(scrollId2));

                //Break condition: No hits are returned
                if (searchResponse.getHits().getHits().length == 0) {
                    logger.info("全部查询结果已返回，总计：" + sum + "|结果验证：" + (sum == totalHits));
                    break;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static void dataHandle(Map<String, Object> source) {

    }

    private static BoolQueryBuilder getQueryBuilder0() {
        BoolQueryBuilder queryBuilder0 = QueryBuilders.boolQuery();
        //属性
        queryBuilder0.must(QueryBuilders.existsQuery("events_tag"));
        queryBuilder0.must(QueryBuilders.matchQuery("events_tag", "100683"));
        //时间
        RangeQueryBuilder queryBuilder_created_at = QueryBuilders.rangeQuery("created_at");
        String format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        queryBuilder_created_at.format(format);
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.DAY_OF_MONTH, -150);
        queryBuilder_created_at.from(simpleDateFormat.format(instance.getTime()));
        queryBuilder_created_at.to("2100-08-12 15:43:45");
        queryBuilder0.must(queryBuilder_created_at);
        //文章类型
//        queryBuilder0.must(QueryBuilders.termQuery("article_type", "1"));
        queryBuilder0.mustNot(QueryBuilders.existsQuery("article_type"));//筛选出微博
        return queryBuilder0;
    }
}
