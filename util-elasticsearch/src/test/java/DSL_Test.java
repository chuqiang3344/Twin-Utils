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
public class DSL_Test {
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
    }


    @Test
    public void t1(){

        SearchRequestBuilder searchRequest = null;
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
        queryBuilder.must(QueryBuilders.rangeQuery("created_at").from(start).to(end).format("yyyy-MM-dd HH:mm:ss"));
        queryBuilder.must(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("push_types")));
        queryBuilder.mustNot(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("push_types")));
//        String[] tempIds = {"585", "100012", "100013", "100014", "100015", "100016", "100070", "100071"};
//        BoolQueryBuilder subjectQuery = QueryBuilders.boolQuery();
//        for (String tempId : tempIds) {
//            subjectQuery.should(QueryBuilders.termQuery("subjects_tag", tempId));
//        }
//        queryBuilder.must(subjectQuery);
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
    }

}
