package com.tyaer.elasticsearch.app;

import com.tyaer.database.mysql.MySQLHelperPool;
import com.tyaer.elasticsearch.conf.DTO;
import com.tyaer.elasticsearch.heatvalue.HeatValueComputer;
import com.tyaer.elasticsearch.manage.EsClientMananger;
import org.apache.commons.lang3.StringUtils;
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
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Twin on 2017/11/21.
 */
public class CbfxRuner implements Runnable {
    private static final Logger logger = Logger.getLogger(CbfxRuner.class);
    private static String url_jdbc;
    private static String user_name;
    private static String password;
    private static String t_domain;
    private static MySQLHelperPool mySQLHelperPool;
    private static EsClientMananger esClientMananger;
    private static String tableNameEvent = "ams_cbfx_event";

    static {
        Properties pps = new Properties();
        try {
            pps.load(CbfxRuner.class.getResourceAsStream("/elasticsearch.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        url_jdbc = pps.getProperty("jdbc.mysql.url");
        user_name = pps.getProperty("jdbc.mysql.username");
        password = pps.getProperty("jdbc.mysql.password");
        t_domain = pps.getProperty("mysql.table.t_domain");

        mySQLHelperPool = new MySQLHelperPool(user_name, password, url_jdbc);

        String es_hosts = DTO.configFileReader.getConfigValue("es.hosts");
        String esClusterName = DTO.configFileReader.getConfigValue("es.cluster.name");
        esClientMananger = new EsClientMananger(es_hosts, esClusterName);
    }

    String tableName;
    String event_id;
    int count_es;

    public CbfxRuner() {
    }

    public CbfxRuner(String tableName, String event_id) {
        this.tableName = tableName;
        this.event_id = event_id;
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    public static void main(String[] args) throws ParseException {

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            String sql = "INSERT IGNORE INTO " + tableNameEvent + "(org_id,event_id,event_name,keywords,flag) SELECT organizationid,id,`name`,keywords,1 FROM ams.vt_subject WHERE organizationid=10006 and type=3;";
            int i = mySQLHelperPool.executeUpdateSql(sql);
            logger.info("定时同步网信办专题：" + i);
        }, 0, 5, TimeUnit.MINUTES);

        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            List<Map<String, Object>> modeResult = mySQLHelperPool.findModeResult("select table_name,event_id,start_time,update_time from " + tableNameEvent + " where flag=1");
            logger.info("传播分析开始：" + modeResult.size() + "|" + modeResult);
            for (Map<String, Object> map : modeResult) {
                logger.info(map);
                String table_name = (String) map.get("table_name");
                int event_id = (int) map.get("event_id");
                if (table_name == null) {
                    table_name = createNewTable();
                    String sql = "update " + tableNameEvent + " set table_name='" + table_name + "' where event_id=" + event_id;
                    logger.info("更新table_name：" + sql);
                    mySQLHelperPool.updateByPreparedStatement(sql, null);
                }

                Date start_time = (Date) map.get("start_time");
                Date update_time = (Date) map.get("update_time");
                if (start_time == null || update_time == null) {
                    String sql = "update " + tableNameEvent + " set start_time=now(),update_time=now() where event_id=" + event_id;
                    logger.info("更新start_time：" + sql);
                    mySQLHelperPool.updateByPreparedStatement(sql, null);
//                executorService.execute(new CbfxRuner(event_id + ""));
                    new CbfxRuner(table_name, event_id + "").run();
                } else {
                    Calendar instance = Calendar.getInstance();
                    instance.add(Calendar.MINUTE, -30);
                    if (instance.getTime().after(update_time)) {
                        logger.info("需要更新传播分析路径：" + event_id);
//                    executorService.execute(new CbfxRuner(event_id + ""));
                        new CbfxRuner(table_name, event_id + "").run();
                    } else {
                        logger.info("跳过：" + event_id);
                    }
                }
            }

            logger.info("结束一轮处理...");
        }, 0, 1, TimeUnit.HOURS);

    }


    public static String createNewTable() {

        List<Map<String, Object>> modeResult = mySQLHelperPool.findModeResult("SELECT table_name,COUNT(*) as count FROM " + tableNameEvent + " WHERE table_name IS not NULL GROUP BY table_name;");
        for (Map<String, Object> map : modeResult) {
            String table_name = (String) map.get("table_name");
            long count = (long) map.get("count");
            if (count < 5) {
                return table_name;
            }
        }

        String hz = Calendar.getInstance().getTimeInMillis() + "";
        String tableName = "ams_cbfx" + "_" + hz;
        logger.info("分表，创建新表：" + tableName);
        mySQLHelperPool.executeSql("CREATE TABLE `" + tableName + "` (\n" +
                "  `id` int(11) NOT NULL AUTO_INCREMENT,\n" +
                "  `mid` varchar(56) CHARACTER SET utf8 NOT NULL COMMENT 'ams上的mid',\n" +
                "  `mid_i` varchar(56) DEFAULT NULL COMMENT '原始mid',\n" +
                "  `mid_p` varchar(56) CHARACTER SET utf8 DEFAULT NULL COMMENT '上一级用户',\n" +
                "  `mid_f` varchar(56) CHARACTER SET utf8 DEFAULT NULL COMMENT '首发mid',\n" +
                "  `created_at` datetime DEFAULT NULL COMMENT '创建时间',\n" +
                "  `grade_all` int(11) DEFAULT NULL COMMENT '热度值',\n" +
                "  `isOriginal` int(11) DEFAULT NULL COMMENT '是否为原创',\n" +
                "  `article_type` int(11) DEFAULT NULL COMMENT '文章类型：微博为0',\n" +
                "  `download_type` int(11) DEFAULT NULL COMMENT '下载类型',\n" +
                "  `reports_count` int(11) DEFAULT NULL COMMENT '转发数',\n" +
                "  `reposts_depth` int(11) DEFAULT NULL COMMENT '转发层数',\n" +
                "  `name` varchar(100) DEFAULT NULL COMMENT '昵称',\n" +
                "  `sourcemid` varchar(50) CHARACTER SET utf8 DEFAULT NULL COMMENT 'sourcemid',\n" +
                "  `fmid` varchar(56) DEFAULT NULL COMMENT '父mid',\n" +
                "  `path_count` int(11) DEFAULT NULL COMMENT '存在路径的记录数',\n" +
                "  `event_id` int(11) DEFAULT NULL COMMENT '事件id',\n" +
                "  PRIMARY KEY (`id`),\n" +
                "  UNIQUE KEY `mid` (`mid`,`event_id`) USING BTREE,\n" +
                "  KEY `event_id` (`event_id`) USING BTREE,\n" +
                "  KEY `mid_i` (`mid_i`),\n" +
                "  KEY `nn` (`event_id`,`name`) USING BTREE,\n" +
                "  KEY `mid_p` (`event_id`,`mid_p`) USING BTREE,\n" +
                "  KEY `index_mid_f` (`event_id`,`mid_f`) USING BTREE,\n" +
                "  KEY `sourcemid` (`sourcemid`) USING BTREE,\n" +
                "  KEY `fmid` (`fmid`) USING BTREE,\n" +
                "  KEY `created_at` (`created_at`) USING BTREE\n" +
                ") ENGINE=InnoDB AUTO_INCREMENT=3952314 DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;\n" +
                "\n");
        return tableName;
    }

    private void query2weibo(String event_id) {
        String sql2 = "select mid_i from " + tableName + " where article_type=0 and event_id=" + event_id + " and isOriginal=1 and reports_count>0";
        logger.info("2query2weibo向下找数据:" + sql2);
        List<Map<String, Object>> modeResult1 = mySQLHelperPool.findModeResult(sql2);
        HashSet<String> hashSet = new HashSet<>();
        for (Map<String, Object> map : modeResult1) {
            String mid_i = (String) map.get("mid_i");
            hashSet.add(mid_i);
        }
        if (hashSet.isEmpty()) {
            return;
        }

        ArrayList<String> list = new ArrayList<>();
        int i = 0;
        for (String key : hashSet) {
            i++;
            list.add(key);
            if (list.size() == 1000 || i == hashSet.size()) {
                BoolQueryBuilder queryBuilder2 = QueryBuilders.boolQuery()
                        .should(QueryBuilders.termsQuery("mid_f", hashSet))
                        .should(QueryBuilders.termsQuery("mid_p", hashSet));
                queryData(event_id, queryBuilder2);
                list.clear();
            }
        }
    }

    private void query2article(String event_id) {
        String sql2 = "select mid from " + tableName + " where article_type!=0 and event_id=" + event_id + " and isOriginal=1";
        logger.info("2query2article向下找数据:" + sql2);
        List<Map<String, Object>> modeResult1 = mySQLHelperPool.findModeResult(sql2);
        HashSet<String> hashSet = new HashSet<>();
        for (Map<String, Object> map : modeResult1) {
            String mid = (String) map.get("mid");
            hashSet.add(mid);
        }
        if (hashSet.isEmpty()) {
            return;
        }

        ArrayList<String> list = new ArrayList<>();
        int i = 0;
        for (String key : hashSet) {
            i++;
            list.add(key);
            if (list.size() == 1000 || i == hashSet.size()) {
                BoolQueryBuilder queryBuilder2 = QueryBuilders.boolQuery()
                        .should(QueryBuilders.termsQuery("mid_p", hashSet));
                queryData(event_id, queryBuilder2);
                list.clear();
            }
        }
    }

    private void query3weibo(String event_id) {
        /*3向上找数据*/
        String sql3 = "SELECT a.mid_f,a.article_type,b.mid FROM (SELECT * FROM " + tableName + " WHERE mid_f is NOT NULL and article_type!=0 and event_id=" + event_id + " ) a LEFT JOIN  " + tableName + " b ON a.mid_f=b.mid_i";
        List<Map<String, Object>> modeResult3 = mySQLHelperPool.findModeResult(sql3);
        logger.info("3query3weibo向上找数据:" + sql3);
        HashSet<String> hashSet = new HashSet<>();
        for (Map<String, Object> map : modeResult3) {
            String mid = (String) map.get("mid");
            if (StringUtils.isBlank(mid)) {
                String mid_f = (String) map.get("mid_f");
                if (StringUtils.isNotBlank(mid_f)) {
                    hashSet.add("0_" + mid_f);
                }
            }
        }
        if (hashSet.isEmpty()) {
            return;
        }
        BoolQueryBuilder queryBuilder3 = QueryBuilders.boolQuery()
//                .should(QueryBuilders.termsQuery("mid", list2));
                .should(QueryBuilders.termsQuery("sourcemid", hashSet));
        queryData(event_id, queryBuilder3);
    }

    private void query3article(String event_id) {
        /*3向上找数据*/
        String sql3 = "SELECT a.mid_p,b.mid FROM (SELECT * FROM " + tableName + " WHERE mid_p is NOT NULL and article_type!=0 and event_id=" + event_id + " ) a LEFT JOIN  " + tableName + " b ON a.mid_p=b.mid";
        List<Map<String, Object>> modeResult3 = mySQLHelperPool.findModeResult(sql3);
        logger.info("3query3article向上找数据:" + sql3);
        HashSet<String> hashSet = new HashSet<>();
        for (Map<String, Object> map : modeResult3) {
            String mid = (String) map.get("mid");
            if (StringUtils.isBlank(mid)) {
                String mid_p = (String) map.get("mid_p");
                if (StringUtils.isNotBlank(mid_p)) {
                    hashSet.add(mid_p);
                }
            }
        }
        if (hashSet.isEmpty()) {
            return;
        }
        BoolQueryBuilder queryBuilder3 = QueryBuilders.boolQuery()
//                .should(QueryBuilders.termsQuery("mid", list2));
                .should(QueryBuilders.termsQuery("mid", hashSet));
        queryData(event_id, queryBuilder3);
    }

    public void analysis4fmid(String event_id) {
        try {
            logger.info("数据插入完毕等待更新fmid");
//            String sql = "update " + tableName + " a," + tableName + "  b set a.fmid=b.mid_i where   b.event_id= a.event_id and  a.mid_p=b.name and a.mid_f=b.mid_f and a.fmid is null";
//            String sql = "update " + tableName + " a," + tableName + "  b set a.fmid=b.mid_i where   b.event_id= a.event_id and  a.mid_p=b.name and a.mid_f=b.mid_f and a.mid_p IS NOT NULL and a.mid_f IS NULL";
//            String sql = "update " + tableName + " a," + tableName + "  b set a.fmid=b.mid_i where   b.event_id= a.event_id and  a.mid_p=b.name and a.mid_f=b.mid_f and not b.mid_i is null and not a.mid_p is null  and not  a.mid_f is null";
            String sql = "update " + tableName + " a," + tableName + "  b set a.fmid=b.mid_i where a.event_id=" + event_id + " AND b.event_id=" + event_id + " AND a.mid_p=b.name and a.mid_f=b.mid_f and a.mid_p IS NOT NULL and a.mid_f IS NOT NULL";
            logger.info(sql);
            int i = mySQLHelperPool.executeUpdateSql(sql);
            logger.info("fmid更新数：" + i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void analysis5pathCount(String event_id) {
    /*计算首发是否有传播路径*/
        String sql4 = "select mid,mid_i,mid_f,isOriginal,fmid from " + tableName + " where event_id=" + event_id;
        List<Map<String, Object>> modeResult = mySQLHelperPool.findModeResult(sql4);
        logger.info("计算首发是否有传播路径：" + sql4);
//        logger.info("计算首发是否有传播路径：" + modeResult.size());
        for (Map<String, Object> objectMap : modeResult) {
            int path_count = 0;
            String isOriginal = objectMap.get("isOriginal") + "";
            String fmid = objectMap.get("fmid") + "";
            if ("1".equals(isOriginal)) {
                String mid = objectMap.get("mid") + "";
                String mid_i = objectMap.get("mid_i") + "";
                for (Map<String, Object> map : modeResult) {
                    String fmid2 = map.get("fmid") + "";
                    String mid_f = map.get("mid_f") + "";
                    if (mid_i.equals(fmid2) || mid_i.equals(mid_f)) {
                        path_count = 1;
                        break;
                    }
                }
                if (path_count > 0) {
                    logger.info("发现传播路径:" + mid);
                    mySQLHelperPool.updateByPreparedStatement("update " + tableName + " set path_count=" + path_count + " where mid='" + mid + "'", null);
                }
            }
        }
    }

    private void analysis5pathCount2(String event_id) {
    /*计算首发是否有传播路径*/
        String sql4 = "select mid,mid_i,mid_f,isOriginal from " + tableName + " where event_id=" + event_id;
        List<Map<String, Object>> modeResult = mySQLHelperPool.findModeResult(sql4);
        logger.info("计算首发是否有传播路径：" + sql4);
        logger.info("计算首发是否有传播路径：" + modeResult.size());
        for (Map<String, Object> objectMap : modeResult) {
            String isOriginal = objectMap.get("isOriginal") + "";
            String mid = objectMap.get("mid") + "";
            String mid_i = objectMap.get("mid_i") + "";
            if ("1".equals(isOriginal)) {
                boolean flag = false;
                for (Map<String, Object> map : modeResult) {
                    String mid_f = map.get("mid_f") + "";
                    if (mid_i.equals(mid_f)) {
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    logger.info("有路径:" + mid);
                    mySQLHelperPool.updateByPreparedStatement("update " + tableName + " set path_count=1 where mid='" + mid + "'", null);
                }
            }
        }
    }

    public void analysis6sql() {
        logger.info("analysis6sql sql更新操作");

        /*处理异常数据*/
        mySQLHelperPool.executeUpdateSql("UPDATE " + tableName + " SET mid_p=NULL,fmid=NULL,isOriginal=1 where mid_i=fmid");

        /*找路径*/
        logger.info("找路径:" + mySQLHelperPool.executeUpdateSql("update " + tableName + " a," + tableName + " b set a.path_count=1  where b.fmid=a.mid_i and a.fmid is null  and a.mid_f is null and a.event_id=b.event_id;"));

        /*标记mid_f*/
        logger.info("标记mid_f:" + mySQLHelperPool.executeUpdateSql("UPDATE " + tableName + " SET mid_f=fmid WHERE article_type!=0 AND fmid is not null AND mid_f IS NULL;"));

        /*找首发*/
        logger.info("找首发:" + mySQLHelperPool.executeUpdateSql("UPDATE " + tableName + " a," + tableName + " b SET a.isOriginal=1  where b.fmid=a.mid_i AND a.mid_p IS NULL AND a.mid_f IS NULL AND a.fmid IS NULL AND a.isOriginal=0 and a.event_id=b.event_id;"));

        /*新闻热度值*/
        ArrayList<ArrayList<Object>> arrayLists = new ArrayList<>();
        List<Map<String, Object>> modeResult = mySQLHelperPool.findModeResult("select a.id,a.mid_i,a.event_id,COUNT(*) as count from " + tableName + " a," + tableName + " b  where a.isOriginal=1 AND a.article_type<>0 AND b.mid_f=a.mid_i GROUP BY a.mid_i,a.event_id;");
        logger.info("mysql计算热度值:" + modeResult.size());
        for (Map<String, Object> objectMap : modeResult) {
            int id = (int) objectMap.get("id");
            String count = objectMap.get("count")+"";
            int grade_all = HeatValueComputer.computeValue(Integer.valueOf(count));
            ArrayList<Object> objects = new ArrayList<>();
            objects.add(grade_all);
            objects.add(id);
            arrayLists.add(objects);
        }
        mySQLHelperPool.batchUpdateByPreparedStatement("update " + tableName + " set grade_all=? where id=?", arrayLists);

    }

    public void analysis7name() {
        logger.info("analysis7name 修改传统媒体数据的name");
        List<Map<String, Object>> domains = mySQLHelperPool.findModeResult("select domain_id,domain_name from " + t_domain);
        HashMap<String, String> domainMap = new HashMap<>();
        for (Map<String, Object> domain : domains) {
            String domain_id = domain.get("domain_id") + "";
            String domain_name = domain.get("domain_name") + "";
            domainMap.put(domain_id, domain_name);
        }
        ArrayList<ArrayList<Object>> hashMaps = new ArrayList<>();
        List<Map<String, Object>> modeResult = mySQLHelperPool.findModeResult("select * from " + tableName + " where article_type<>0 and name is null;");
        for (Map<String, Object> objectMap : modeResult) {
            String mid = (String) objectMap.get("mid");
            String domainId = mid.substring(4, mid.indexOf("_"));
            String name = domainMap.get(domainId);
            ArrayList<Object> list = new ArrayList<>();
            list.add(name);
            list.add(mid);
            hashMaps.add(list);
        }
        String sql = "update " + tableName + " set name=? where mid=?";
        mySQLHelperPool.batchUpdateByPreparedStatement(sql, hashMaps);
    }

    private int queryData(String objectID, BoolQueryBuilder queryBuilder0) {
        TransportClient transportClient = esClientMananger.getEsClient();
        String index = DTO.configFileReader.getConfigValue("es.index");
        SearchRequestBuilder searchRequest = transportClient.prepareSearch(index);

        /**
         * 查询设置
         */
        String[] types = new String[]{};
//        types = new String[]{"t_article"};
        if (types != null && types.length > 0) {
            searchRequest.setTypes(types);
        }
        int pageSize = 200;
        Scroll scoll = new Scroll(new TimeValue(600000));//翻页器,保持游标查询窗口一分钟。
        searchRequest.addSort("indextime", SortOrder.DESC);//排序规则
        searchRequest.setSearchType(SearchType.QUERY_AND_FETCH)
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
        int sum = 0;
        try {
            SearchResponse searchResponse = searchRequest.execute().get();
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
                List<Map<String, Object>> hashMaps = new ArrayList<>();
                for (SearchHit searchHit : searchHits) {
//                    logger.info(searchHit.getSourceAsString());
                    Map<String, Object> source = searchHit.getSource();
                    String id = searchHit.getId();
                    source.put("_id", id);
                    /**
                     * 数据操作
                     */
//                    dataHandle(source);//todo
                    String mid = source.get("mid") + "";
                    String article_type = (String) source.get("article_type");
                    String download_type = (String) source.get("download_type");
                    if (StringUtils.isBlank(article_type)) {
                        article_type = "0";
                        source.put("article_type", article_type);
                    }
                    source.put("event_id", objectID);

                    //下载类型同化+算法
                    String mid_p = (String) source.get("mid_p");
                    String mid_f = (String) source.get("mid_f");

                    if (article_type.equals("0")) {
                        int isOriginal = 0;
                        if (mid_f != null && mid_f.contains("_source")) {
                            isOriginal = 1;
                            mid_f = null;
                        }
                        source.put("mid_i", mid.substring(mid.indexOf("_") + 1, mid.length()));
                        if (!"3".equals(download_type)) {
                            mid_f = mid_p;
                            source.put("fmid", mid_p);
                            mid_p = null;
                        }
                        if ((StringUtils.isBlank(mid_p)) && StringUtils.isBlank(mid_f) || (mid_f != null && mid_f.contains("_source"))) {
                            isOriginal = 1;
                        }
                        source.put("isOriginal", isOriginal);
                    } else {
                        source.put("mid_i", mid);
                        int isOriginal = 0;
                        if (StringUtils.isNotBlank(mid_f)) {
                            if (mid_f.contains("_source")) {
                                isOriginal = 1;
                            }
                            mid_f = null;
                        }
                        source.put("isOriginal", isOriginal);
                        if (StringUtils.isNotBlank(mid_p)) {
                            source.put("fmid", mid_p);
                        }
                        source.put("name", null);
                    }
                    source.put("mid_f", mid_f);
                    source.put("mid_p", mid_p);

                    //数据清洗
                    String name = (String) source.get("name");
                    if (name != null && name.length() > 40) {
                        logger.warn("name too long:" + name);
                        name = name.substring(0, 40);
                        source.put("name", name);
                    }
                    //热度值
                    String reports_count1 = (String) source.get("reports_count");
                    if (reports_count1 != null) {
                        try {
                            int reports_count = Integer.valueOf(reports_count1);
                            if (reports_count > 0) {
//                                HeatValueBean heatValueBean = new HeatValueBean(null, 0, reports_count, 0, 0, new Timestamp(Calendar.getInstance().getTimeInMillis()), null, null);
                                int grade_all = HeatValueComputer.computeValue(reports_count);
                                source.put("grade_all", grade_all);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
//                    logger.info(source);
                    hashMaps.add(source);
                }
                mySQLHelperPool.batchInsertByPreparedStatement(tableName, hashMaps);

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
        return sum;
    }

    public void computeValue() {
        ArrayList<ArrayList<Object>> hashMaps = new ArrayList<>();
        List<Map<String, Object>> modeResult = mySQLHelperPool.findModeResult("select * from ams_cbfx where grade_all=0 or grade_all is null and event_id=" + event_id);
        for (Map<String, Object> objectMap : modeResult) {
//            String mid = (String) objectMap.get("mid");
//            String mid = (String) objectMap.get("mid");
//            String mid = (String) objectMap.get("mid");
//            String mid = (String) objectMap.get("mid");
//            String domainId = mid.substring(4, mid.indexOf("_"));
//            new HeatValueBean(id,comments_count,reposts_count,zan_count,read_count,create_time,emotion,null);
        }
        String sql = "update " + tableName + " set name=? where mid=?";
        mySQLHelperPool.batchUpdateByPreparedStatement(sql, hashMaps);
    }

    @Override
    public void run() {
        /**
         * DSL查询语句组装
         */
        BoolQueryBuilder queryBuilder0 = QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("events_tag", event_id))
//                .must(QueryBuilders.termQuery("mid", "df922017070909280_4127531671809763"))
                ;
        count_es = queryData(event_id, queryBuilder0);

        /*2向下找数据*/
        query2weibo(event_id);
        query2article(event_id);

        /*3向上找数据*/
        query3weibo(event_id);
        query3article(event_id);

        /*数据插入完毕等待更新fmid*/
        analysis4fmid(event_id);

        /*计算首发是否有传播路径*/
        analysis5pathCount(event_id);

        /*使用sql语句优化数据*/
        analysis6sql();

        /*将传统网站的name赋予站点名称*/
        analysis7name();


        logger.info("传播路径分析完毕！" + event_id);
        String sql = "update " + tableNameEvent + " set update_time=now(),count_es=?,count_mysql=? where event_id=" + event_id;
        logger.info("更新：" + sql);
        ArrayList<Object> objects = new ArrayList<>();
        List<Map<String, Object>> modeResult = mySQLHelperPool.findModeResult("select id from " + tableName + " where event_id=" + event_id);
        objects.add(count_es);
        objects.add(modeResult.size());
        logger.info(modeResult.size());
        mySQLHelperPool.updateByPreparedStatement(sql, objects);

    }

}

/*
 * "weibo_url":"http://www.weibo.com/1683820891/Fbz503pbV",
 * "events_tag":"100683", "reports_count":"0",
 * "mid":"a58c2017070911390_4127564580811823", "created_at":
 * "2017-07-09 11:39:05.0", "sourcemid":"0_4127564580811823", "source":
 * "<a href="http://app.weibo.com/t/feed/U0uYq" rel="nofollow">OPPO R9 Plus</a>"
 * , "uid":"137d0_1683820891", "reposts_depth":"0", "grade_all":"0",
 * "text_loc_country":"中国", "text":
 * "哈哈哈哈哈哈哈哈哈哈哈哈哈//@幕斯:hhhh//@M大王叫我来巡山: 学习了………比好好卖生意好多了……//@阿福闭嘴:学习了//@江南大野花: 江湖又现商业奇才//@回忆专用小马甲:城市套路深，这波服[作揖]//@假装在纽约:哈哈哈哈独辟蹊径！"
 * , "orgs_event_tag":"10030", "text_loc":"广西", "text_loc_city":"贵港市",
 * "zans_count":"0", "crawler_time":"2017-07-10 21:36:55.191",
 * "at_who":"幕斯:hhhh//@M大王叫我来巡山:,阿福闭嘴:学习了//@江南大野花:", "crawler_site_id":"0",
 * "text_loc_province":"广西", "emotion":"中性", "comments_count":"0",
 * "download_type":"10", "site_id":"0", "name":"调皮捣蛋小暮暮",
 * "created_date":"20170709", "updatetime":"2017-07-10 21:36:55.194"
 * 
 * 
update "+tableName+" a,"+tableName+"  b set a.fmid=b.mid where a.mid_p=b.name and a.mid_f=b.mid_f and a.fmid is null

 */
