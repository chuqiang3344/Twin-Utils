import com.tyaer.elasticsearch.manage.ElasticSearchHelper;
import com.tyaer.elasticsearch.manage.EsClientMananger;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.avg.AvgBuilder;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Twin on 2017/9/23.
 */
public class Scan_Test {
    private static final Logger logger = Logger.getLogger(ElasticSearchHelper.class);


    //    static String es_hosts = "192.168.2.116:9300,192.168.2.115:9300,192.168.2.116:9400";
//    static String es_hosts = "192.168.3.111:9300";
    static String es_hosts = "192.168.2.116:9300,192.168.2.115:9300,192.168.2.116:9400";
    static String esClusterName = "izhonghong";
    static String i_ams_total_data = "i_ams_total_data";
    static String t_status_weibo = "t_status_weibo";
    private static EsClientMananger esClientMananger = new EsClientMananger(es_hosts, esClusterName);
    static TransportClient transportClient = esClientMananger.getEsClient();


    public static void main(String[] args) {

        SearchRequestBuilder searchRequest = null;
        SearchResponse searchResponse = null;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar c = Calendar.getInstance();
        //c.add(Calendar.DAY_OF_MONTH, -31);

        c.add(Calendar.DAY_OF_MONTH, -8);
        String end = df.format(c.getTime());
        c.add(Calendar.YEAR, -5);
        String start = df.format(c.getTime());

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        Scroll scoll = new Scroll(new TimeValue(600000));


        queryBuilder.must(QueryBuilders.rangeQuery("created_at").from(start).to(end).format("yyyy-MM-dd HH:mm:ss"));
        queryBuilder.must(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("push_types")));
        queryBuilder.must(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("recommends_tag")));
        queryBuilder.must(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("alerts_tag")));
        queryBuilder.must(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("events_tag")));
        String[] tempIds = {"585", "100012", "100013", "100014", "100015", "100016", "100070", "100071"};
        BoolQueryBuilder subjectQuery = QueryBuilders.boolQuery();
        for (String tempId : tempIds) {
            subjectQuery.should(QueryBuilders.termQuery("subjects_tag", tempId));
        }
        queryBuilder.must(subjectQuery);
//	   	queryBuilder.must(QueryBuilders.boolQuery().should(QueryBuilders.termQuery("text_loc","海")));
        searchRequest = transportClient.prepareSearch(new String[]{i_ams_total_data});
        searchRequest.setTypes(new String[]{t_status_weibo}).
                setSearchType(SearchType.SCAN)
                // .setFetchSource(new String[]{"mid","emotion","zan_count","comments_count","reposts_count","created_at"}, null)//指定返回字段.
                .setFetchSource(new String[]{"subjects_tag", "orgs_tag"}, null)//指定返回字段.
                .setQuery(queryBuilder)
                .setSize(1000)
                .setScroll(scoll);
        System.out.println(searchRequest);// 打印DSL语句.


        // .setQuery(QueryBuilders.termQuery("content", "黑社交"))
        searchResponse = searchRequest.get(); //执行请求.
        // 获取命中
        SearchHits hits = searchResponse.getHits();
        List<String> mids = new ArrayList<String>();
        Set<String> sets = new HashSet<String>();

        int i = 0;

        while (true) {
            for (SearchHit hit : hits) {
                String mid = hit.getId();
                System.out.println(mid);
                String subjects_tag = hit.getSource().get("subjects_tag").toString();
                String[] subjects = subjects_tag.split("\\,");
                List<String> subjectList = new ArrayList<String>();
                for (String tempId : tempIds) {
                    subjectList.add(tempId);
                }
                boolean match = true;
                for (String id : subjects) {
                    if (!id.trim().isEmpty() && !subjectList.contains(id)) {
                        match = false;
                    }
                }
                if (match) {
                    mids.add(mid);
                }


                i++;

            }
            if (i % 100000 == 0) {
                logger.info("loading weibo to delete,num=" + i);
            }
            if (i > 8000000) {
                break;
            }
            String scrollId = searchResponse.getScrollId();

            searchResponse = transportClient.prepareSearchScroll(scrollId).setScrollId(scrollId).setScroll(scoll).get();

            String scrollId2 = searchResponse.getScrollId();
            //logger.info(scrollId.equals(scrollId2));
            hits = searchResponse.getHits();
            //Break condition: No hits are returned
            if (searchResponse.getHits().getHits().length == 0) {

                break;
            }
        }
    }

    @Test
    public void count() {

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

        searchRequest = transportClient.prepareSearch(new String[]{i_ams_total_data});
        searchRequest.setTypes(new String[]{t_status_weibo})

                // .setFetchSource(new String[]{"mid","emotion","zan_count","comments_count","reposts_count","created_at"}, null)//指定返回字段.
                .setFetchSource(false)//指定返回字段.
                .setQuery(queryBuilder)
                .addAggregation(term)

                .setSize(0);

        logger.info(searchRequest);

        // .setQuery(QueryBuilders.termQuery("content", "黑社交"))
        searchResponse = searchRequest.get(); //执行请求.

//        System.out.println(searchResponse);
        // 获取命中
        SearchHits hits = searchResponse.getHits();
        System.out.println(hits);
    }
}
