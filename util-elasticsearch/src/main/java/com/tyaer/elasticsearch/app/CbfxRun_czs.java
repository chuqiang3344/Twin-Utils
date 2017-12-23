package com.tyaer.elasticsearch.app;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tyaer.database.mysql.MySQLHelperPool;
import com.tyaer.elasticsearch.manage.ESHttpHelper;

import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Twin on 2017/11/21.
 * czs修改：深圳网信办——传播分析
 */
public class CbfxRun_czs {
    public static String url_jdbc;
    public static String user_name;
    public static String password;
    public static String url;
    private static Connection conn = null;
    private static Statement stateExec = null;

    static {
        Properties pps = new Properties();
        try {
            pps.load(CbfxRun_czs.class.getResourceAsStream("/elasticsearch.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        url_jdbc = pps.getProperty("jdbc.mysql.url");
        user_name = pps.getProperty("jdbc.mysql.username");
        password = pps.getProperty("jdbc.mysql.password");
        url = pps.getProperty("es1.hosts");
        linkMysql();
    }

    public static void linkMysql() {
        String driver = "com.mysql.jdbc.Driver";
//        String driver = "com.mysql.cj.jdbc.Driver";
        String url = url_jdbc;
        // String user = "dev";
        // String password = "dev";
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, user_name, password);
            if (!conn.isClosed()) {
                System.out.println("Succeeded connecting to the Database!");
            }
            stateExec = conn.createStatement();
            // stateExec.executeUpdate(strSql);
        } catch (ClassNotFoundException e) {
            System.out.println("Sorry,can`t find the Driver!");
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 设置日期格式
        SimpleDateFormat dfS = new SimpleDateFormat("yyyyMMdd");
        String[][] tasks;
        String[] formArr = {"1", "2", "3", "4"};
        String objectID = args[0];
        String startTime = args[1];
        String tableName = "createsz_demo";

        String maxDate = "2017-04-12 15:43:45";
        // objectID="100683";
        int task_count = 0;
        int n = 0;

        // String url = "http://192.168.2.116:9200/i_ams_total_data/_search";
        // System.out.println(n);
        // 循环备用
        {
            formArr[0] = "{\n" + "    \"from\":0,\n" + "    \"size\":8000,\n" + "    \"query\":{\n"
                    + "        \"bool\":{\n" + "            \"must\":[\n" + "                {\n"
                    + "                    \"and\":[\n" + "                        {\n"
                    + "                            \"exists\":{\n"
                    + "                                \"field\":\"events_tag\"\n" + "                            }\n"
                    + "                        },\n" + "                        {\n"
                    + "                            \"match\":{\n" + "                                \"events_tag\":{\n"
                    + "                                    \"query\":\"" + objectID + "\",\n"
                    + "                                    \"type\":\"boolean\"\n"
                    + "                                }\n" + "                            }\n"
                    + "                        },\n" + "                        {\n"
                    + "                            \"range\":{\n"
                    + "                                \"reports_count\":{\n"
                    + "                                    \"gte\":0\n" + "                                }\n"
                    + "                            }\n" + "                        },\n" + "                        {\n"
                    + "                            \"range\":{\n" + "                                \"updatetime\":{\n"
                    + "                                    \"from\":\"" + startTime + " 00:00:00" + "\",\n"
                    + "                                    \"to\":\"2100-08-12 15:43:45\",\n"
                    + "                                    \"format\":\"yyyy-MM-dd HH:mm:ss\",\n"
                    + "                                    \"include_lower\":true,\n"
                    + "                                    \"include_upper\":true\n"
                    + "                                }\n" + "                            }\n"
                    + "                        }\n" + "                    ]\n" + "                }\n"
                    + "            ],\n" + "            \"must_not\":{\n" + "                \"exists\":{\n"
                    + "                    \"field\":[\n" + "                        \"article_type\",\n" +
                    // " \"mid_p\",\n"+
                    "                        \"weibo_test\"\n" + "                    ]\n" + "                }\n"
                    + "            }\n" + "        }\n" + "    },\n" + "    \"sort\":[\n" + "        {\n"
                    + "            \"reports_count\":{\n" + "                \"order\":\"desc\"\n" + "            }\n"
                    + "        }\n" + "    ],\n" + "    \"_source\":{\n" + "        \"includes\":[\n"
                    + "            \"article_type\",\n" + " \"site_id\", \"grade_all\",\n" + "            \"download_type\",\n" + "            \"sourcemid\",\n"
                    + "            \"created_at\",\n" + "            \"reports_count\",\n" + "            \"mid_f\",\n"
                    + "            \"mid_p\",\n" + "  \"mid\",\"reposts_depth\",\n" + "\"name\",\"events_tag\"\n"
                    + "        ]\n" + "    }\n" + "}\n";

            formArr[1] = "{\n" + "    \"from\":0,\n" + "    \"size\":8000,\n" + "    \"query\":{\n"
                    + "        \"bool\":{\n" + "            \"must\":[\n" + "                {\n"
                    + "                    \"and\":[\n" + "                        {\n"
                    + "                            \"exists\":{\n"
                    + "                                \"field\":[\"events_tag\",\"article_type\"]\n"
                    + "                            }\n"
                    + "                        },{\"terms\" : { \"article_type\" : [\"1\", \"11\"]}},\n"
                    + "                        {\n" + "                            \"match\":{\n"
                    + "                                \"events_tag\":{\n"
                    + "                                    \"query\":\"" + objectID + "\",\n"
                    + "                                    \"type\":\"boolean\"\n"
                    + "                                }\n" + "                            }\n"
                    + "                        },\n" + "                        {\n"
                    + "                            \"range\":{\n"
                    + "                                \"reports_count\":{\n"
                    + "                                    \"gte\":0\n" + "                                }\n"
                    + "                            }\n" + "                        },\n" + "                        {\n"
                    + "                            \"range\":{\n" + "                                \"updatetime\":{\n"
                    + "                                    \"from\":\"" + startTime + " 00:00:00" + "\",\n"
                    + "                                    \"to\":\"2100-08-12 15:43:45\",\n"
                    + "                                    \"format\":\"yyyy-MM-dd HH:mm:ss\",\n"
                    + "                                    \"include_lower\":true,\n"
                    + "                                    \"include_upper\":true\n"
                    + "                                }\n" + "                            }\n"
                    + "                        }\n" + "                    ]\n" + "                }\n"
                    + "            ],\n" + "            \"must_not\":{\n" + "                \"exists\":{\n"
                    + "                    \"field\":[\n" +
                    // " \"article_type\",\n"+
                    // " \"mid_p\",\n"+
                    "                        \"weibo_test\"\n" + "                    ]\n" + "                }\n"
                    + "            }\n" + "        }\n" + "    },\n" + "    \"sort\":[\n" + "        {\n"
                    + "            \"reports_count\":{\n" + "                \"order\":\"desc\"\n" + "            }\n"
                    + "        }\n" + "    ],\n" + "    \"_source\":{\n" + "        \"includes\":[\n"
                    + "            \"article_type\",\n" + "            \"grade_all\",\n" + "            \"download_type\",\n" + "     \"site_id\", \"sourcemid\",\n"
                    + "            \"created_at\",\n" + " \"mid\", \"reports_count\",\n" + "            \"mid_f\",\n"
                    + "            \"mid_p\",\n" + "            \"reposts_depth\",\n" + "\"name\",\"events_tag\"\n"
                    + "        ]\n" + "    }\n" + "}\n";

            formArr[2] = "{\n" + "  \"size\":0,\n" + "    \"query\" : {\n" + "    \"bool\" : {\n"
                    + "      \"must\" : [ {\n" + "        \"exists\" : {\n" + "          \"field\" : [\"events_tag\"]\n"
                    + "        }\n" + "      }, {\n" + "        \"match\" : {\n" + "          \"events_tag\" : {\n"
                    + "            \"query\" : \"" + objectID + "\",\n" + "            \"type\" : \"boolean\"\n"
                    + "          }\n" + "        }\n" + "      }, {\n" + "        \"range\" : {\n"
                    + "          \"created_date\" : {\n" + "            \"gte\" : \""
                    + dfS.format(df.parse(startTime + " 00:00:00")) + " \",\n" + "            \"lte\" : \"21000101 \"\n" +
                    // " \"format\" : \"yyyy-MM-dd HH:mm:ss\","+
                    // " \"include_lower\" : true,"+
                    // " \"include_upper\" : true"+
                    "          }\n" + "        }\n" + "      }],\n" + "      \"must_not\" : {\n"
                    + "        \"exists\" : {\n" + "          \"field\" : [\"mid_p\",\"mid_f\",\"article_type\"]\n"
                    + "        }\n" + "      }\n" + "    }\n" + "  },\n" + "    \"aggs\": {\n"
                    + "        \"top_tags\": {\n" + "            \"terms\": {\n"
                    + "                \"field\": \"created_date\",\n" + "                \"size\": 300\n"
                    + "            },\n" + "            \"aggs\": {\n" + "                \"top_grade_hits\": {\n"
                    + "                    \"top_hits\": {\n" + "                        \"sort\": [\n"
                    + "                            {\n" + "                                \"grade_all\": {\n"
                    + "                                    \"order\": \"desc\"\n"
                    + "                                }\n" + "                            }\n"
                    + "                        ],\"_source\": \n{"
                    + "\"includes\": [\"article_type\", \"mid\", \"grade_all\",\"name\", \"site_id\",\"sourcemid\",\"created_at\",\"reports_count\",\"mid_f\",\"mid_p\",\"reposts_depth\",\"download_type\" ]},\n"
                    + "                        \"size\" : 60\n" + "                    }\n" + "                }\n"
                    + "            }\n" + "        }\n" + "    }\n" + "}";

            formArr[3] = "{\n" + "  \"size\":0,\n" + "    \"query\" : {\n" + "    \"bool\" : {\n"
                    + "      \"must\" : [ {\n" + "        \"exists\" : {\n"
                    + "          \"field\" : [\"events_tag\",\"article_type\"]\n" + "        }\n" + "      }, {\n"
                    + "        \"match\" : {\n" + "          \"events_tag\" : {\n" + "            \"query\" : \""
                    + objectID + "\",\n" + "            \"type\" : \"boolean\"\n" + "          }\n" + "        }\n"
                    + "      }, {\n" + "        \"range\" : {\n" + "          \"created_date\" : {\n"
                    + "            \"gte\" : \"" + dfS.format(df.parse(startTime + " 00:00:00")) + " \",\n"
                    + "            \"lte\" : \"21000101 \"\n" +
                    // " \"format\" : \"yyyy-MM-dd HH:mm:ss\","+
                    // " \"include_lower\" : true,"+
                    // " \"include_upper\" : true"+
                    "          }\n" + "        }\n" + "      },{\"terms\" : { \"article_type\" : [\"1\", \"11\"]}} ],\n"
                    + "      \"must_not\" : {\n" + "        \"exists\" : {\n"
                    + "          \"field\" : [\"mid_p\",\"mid_f\"]\n" + "        }\n" + "      }\n" + "    }\n"
                    + "  },\n" + "    \"aggs\": {\n" + "        \"top_tags\": {\n" + "            \"terms\": {\n"
                    + "                \"field\": \"created_date\",\n" + "                \"size\": 300\n"
                    + "            },\n" + "            \"aggs\": {\n" + "                \"top_grade_hits\": {\n"
                    + "                    \"top_hits\": {\n" + "                        \"sort\": [\n"
                    + "                            {\n" + "                                \"grade_all\": {\n"
                    + "                                    \"order\": \"desc\"\n"
                    + "                                }\n" + "                            }\n"
                    + "                        ],\"_source\": \n{"
                    + "\"includes\": [\"article_type\", \"grade_all\",\"mid\",\"name\",\"site_id\", \"sourcemid\",\"created_at\",\"reports_count\",\"mid_f\",\"mid_p\",\"reposts_depth\",\"download_type\" ]},\n"
                    + "                        \"size\" : 60\n" + "                    }\n" + "                }\n"
                    + "            }\n" + "        }\n" + "    }\n" + "}";

            ESHttpHelper esHttpHelper = new ESHttpHelper();
            for (int l = 0; l < 2; l++) {
                // System.out.println(formArr[l]);
                String post = esHttpHelper.query(url, formArr[l]);
                JSONObject jsonObject = JSON.parseObject(post);
                JSONArray hits = jsonObject.getJSONObject("hits").getJSONArray("hits");
                System.out.println("ES1条数：" + hits.size() + "  第 " + l + " 组");
                for (int i = 0; i < hits.size(); i++) {
                    JSONObject record = hits.getJSONObject(i);
                    JSONObject source = record.getJSONObject("_source");
                    // System.out.println(source);
                    // String subId = source.getString("events_tag");
                    String userID = source.getString("uid");
                    String download_type = source.getString("download_type");
                    String create_at = source.getString("created_at");
                    String reports_count = source.getString("reports_count");
                    String comments = source.getString("comments_count");
                    String zans = source.getString("zans_count");
                    String sourcemid = source.getString("sourcemid");
                    String midO = source.getString("mid");
                    String content = source.getString("text");
                    // String urls = source.getString("weibo_url");
                    String subType = source.getString("article_type");
                    String name = source.getString("name");
                    String updateTime = source.getString("updatetime");
                    String site_id = source.getString("site_id");
                    String sourceUrl = source.getString("source");
                    String mid_p = source.getString("mid_p");
                    String views = source.getString("read_count");
                    String mid_f = source.getString("mid_f");
                    String grade_all = source.getString("grade_all");
                    String reposts_depth = source.getString("reposts_depth");
//                    String events_tag = source.getString("events_tag");
                    mid_p = mid_p == null ? "0" : mid_p;
                    subType = subType == null ? "0" : subType;
                    reports_count = reports_count == null ? "0" : reports_count;
                    reposts_depth = reposts_depth == null ? "0" : reposts_depth;
                    zans = (zans == null ? "0" : zans);
                    comments = (comments == null ? "0" : comments);
                    views = (views == null ? "0" : views);
                    mid_f = (mid_f == null ? "0" : mid_f);
                    grade_all = (grade_all == null ? "0" : grade_all);
                    name = (name == null ? "-" : name);
                    site_id = (site_id == null ? "-" : site_id);


                    //下载类型同化
                    if (!"3".equals(download_type)) {
                        mid_f = mid_p;
                        mid_p = "0";
                    }
                    mid_f = (mid_f == null ? "0" : mid_f);
                    if (mid_f.contains("_source")) {
                        mid_f = "0";
                    }
                    String original = "0";
                    if ("0".equals(mid_p) && "0".equals(mid_f)) {
                        original = "1";
                    }

                    if (mid_f != null && !mid_f.equals("0")) {
                        mid_f = site_id + "_" + mid_f;
                    }
                    if (isNumeric(mid_p) && !mid_p.equals("0")) {
                        mid_p = site_id + "_" + mid_p;
                    }
             /*
           * if
           * (df.parse(maxDate).getTime()<df.parse(updateTime).getTime
           * ()) { maxDate=updateTime;
           * 
           * }
           */
                    // System.out.println(source.getString("reports_count"));
                    // System.out.println(source);
                    String sql = "replace into " + tableName + "(midOld,mid,mid_p,mid_f,created_at,`hot spot`,original,article_type,special_ID,seport_count,reposts_depth,name"
                            + ") " + " values ('" + sourcemid + "','" + midO + "','" + mid_p + "','" + mid_f + "','" + create_at + "',"
                            + grade_all + ",'" + original + "','" + subType + "','" + objectID + "','"
                            + reports_count + "','" + reposts_depth + "','" + name + "')";

                    try {
                        stateExec.executeUpdate(sql);
                    } catch (Exception e) {
                        System.out.println(sql);
                        e.printStackTrace();
                    }

                }
            }
            ////////////////////
            for (int l = 2; l < 4; l++) {
                // System.out.println(formArr[l]);
                String post = esHttpHelper.query(url, formArr[l]);
                JSONObject jsonObject = JSON.parseObject(post);
                JSONArray hits = jsonObject.getJSONObject("aggregations").getJSONObject("top_tags")
                        .getJSONArray("buckets");
                System.out.println("ES2条数：" + hits.size());
                for (int i = 0; i < hits.size(); i++) {
                    JSONObject record = hits.getJSONObject(i);
                    JSONObject source = record.getJSONObject("top_grade_hits");
                    // System.out.println(source.getString("hits"));
                    JSONArray hits2 = source.getJSONObject("hits").getJSONArray("hits");
                    System.out.println(hits2.size());
                    for (int j = 0; j < hits2.size(); j++) {
                        // JSONObject record2 = hits2.getJSONObject(j);
                        JSONObject source2 = hits2.getJSONObject(j).getJSONObject("_source");

                        // String subId = source.getString("events_tag");
                        String userID = source2.getString("uid");
                        String create_at = source2.getString("created_at");
                        String download_type = source2.getString("download_type");
                        String reports_count = source2.getString("reports_count");
                        String comments = source2.getString("comments_count");
                        String zans = source2.getString("zans_count");
                        String name = source2.getString("name");
                        String mid = source2.getString("sourcemid");
                        String content = source2.getString("text");
                        // String urls = source2.getString("weibo_url");
                        String subType = source2.getString("article_type");
                        String updateTime = source2.getString("updatetime");
                        String source2Url = source2.getString("source2");
                        String mid_p = source2.getString("mid_p");
                        String views = source2.getString("read_count");
                        String mid_f = source2.getString("mid_f");
                        String site_id = source2.getString("site_id");
                        String grade_all = source2.getString("grade_all");
                        String reposts_depth = source2.getString("reposts_depth");
                        String events_tag = source2.getString("events_tag");
                        String midO = source2.getString("mid");
                        mid_p = mid_p == null ? "0" : mid_p;
                        subType = subType == null ? "0" : subType;
                        reports_count = reports_count == null ? "0" : reports_count;
                        reposts_depth = reposts_depth == null ? "0" : reposts_depth;
                        zans = (zans == null ? "0" : zans);
                        //        zans = (zans == null ? "0" : zans);
                        views = (views == null ? "0" : views);
                        grade_all = (grade_all == null ? "0" : grade_all);
                        name = (name == null ? "-" : name);
                        site_id = (site_id == null ? "-" : site_id);
                        if (isNumeric(mid_p) && !mid_p.equals("0")) {
                            mid_p = site_id + "_" + mid_p;
                            mid_f = (mid_f == null ? "0" : mid_f);
                            if (mid_f != null && !mid_f.equals("0")) {
                                mid_f = site_id + "_" + mid_f;
                            }

                            //下载类型同化
                            if (!"3".equals(download_type)) {
                                mid_f = mid_p;
                                mid_p = "0";
                            }
                            if (mid_f.contains("_source")) {
                                mid_f = "0";
                            }
                            String original = "0";
                            if ("0".equals(mid_p) && "0".equals(mid_f)) {
                                original = "1";
                            }

                            String sql = "replace into " + tableName + "(midOld,mid,mid_p,mid_f,created_at,`hot spot`,original,article_type,special_ID,seport_count,reposts_depth,name"
                                    + ") " + " values ('" + mid + "','" + midO + "','" + mid_p + "','" + mid_f + "','" + create_at + "',"
                                    + grade_all + ",'" + original + "','" + subType + "','" + events_tag
                                    + "','" + reports_count + "','" + reposts_depth + "','" + name + "')";
                            // System.out.println(sql);

                            try {
                                stateExec.executeUpdate(sql);
                            } catch (Exception e) {
                                System.out.println(sql);
                                // e.printStackTrace();
                            }
                        }

                    }
                }
            }
            ///////////////////////////////////////
            String mid_ps = "";

            try {
                ResultSet result = stateExec.executeQuery(
                        "select  GROUP_CONCAT(CONCAT('\"',mid_p,'\"')) mid_ps from  (select mid_p  from " + tableName + " where special_ID  = '" + objectID +
                                "' and mid_p<>'0' and mid_p not in (select distinct midOld from " + tableName + " where special_ID = '" + objectID + "') group by mid_p having count(*)>0 ) a");
                while (result.next()) {
                    // maxDate = result.getString("mxDt");
                    mid_ps = result.getString("mid_ps");//.replaceAll("\"", "\\\"");
                    //     System.out.println(mid_ps);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            while (mid_ps.length() > 9) {
                String form = "{\n" +
                        "    \"from\":0,\n" +
                        "    \"size\":9000,\n" +
                        "    \"query\":{\n" +
                        "        \"bool\":{\n" +
                        "            \"must\":[\n" +
                        "                {\n" +
                        "                    \"and\":[\n" +
                        "                        {\n" +
                        "                            \"exists\":{\n" +
                        "                                \"field\":\"events_tag\"\n" +
                        "                            }\n" +
                        "                        },\n" +
                        "                        {\"or\":  \n" +
                        "                            [{\"terms\":{\n" +
                        "                                \"sourcemid\":[" + mid_ps + "]\n" +
                        "                                }\n" +
                        "                            },{\"terms\":{\n" +
                        "                                \"name\":[" + mid_ps + "]\n" +
                        "                                }\n" +
                        "                            }]},\n" +
                        "                        {\n" +
                        "                            \"range\":{\n" +
                        "                                \"reports_count\":{\n" +
                        "                                    \"gte\":0\n" +
                        "                                }\n" +
                        "                            }\n" +
                        "                        },\n" +
                        "                        {\n" +
                        "                            \"range\":{\n" +
                        "                                \"updatetime\":{\n" +
                        "                                    \"from\":\"" + startTime + " 00:00:00" + "\",\n" +
                        "                                    \"to\":\"2100-08-12 15:43:45\",\n" +
                        "                                    \"format\":\"yyyy-MM-dd HH:mm:ss\",\n" +
                        "                                    \"include_lower\":true,\n" +
                        "                                    \"include_upper\":true\n" +
                        "                                }\n" +
                        "                            }\n" +
                        "                        }\n" +
                        "                    ]\n" +
                        "                }\n" +
                        "            ],\n" +
                        "            \"must_not\":{\n" +
                        "                \"exists\":{\n" +
                        "                    \"field\":[\"\"\n" +
                        "                    ]\n" +
                        "                }\n" +
                        "            }\n" +
                        "        }\n" +
                        "    },\n" +
                        "    \"sort\":[\n" +
                        "        {\n" +
                        "            \"reports_count\":{\n" +
                        "                \"order\":\"desc\"\n" +
                        "            }\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"_source\":{\n" +
                        "        \"includes\":[\n" +
                        "            \"article_type\",\n" +
                        "            \"download_type\",\n" +
                        "            \"grade_all\",\n" +
                        "            \"sourcemid\",\n" +
                        "            \"created_at\",\n" +
                        "            \"reports_count\",\n" +
                        "            \"mid_f\",\n" +
                        "            \"mid_p\",\n" +
                        "            \"reposts_depth\",\n" +
                        "            \"events_tag\",\n" +
                        "            \"site_id\",\n" +
                        "            \"mid\",\n" +
                        "            \"events_tag\",\n" +
                        "            \"name\"\n" +
                        "        ]\n" +
                        "    }\n" +
                        "}";

                mid_ps = "\"-\"";
                esHttpHelper = new ESHttpHelper();
                //   System.out.println(form);
                // System.out.println(mid_ps);
                String post = esHttpHelper.query(url, form);
                JSONObject jsonObject = JSON.parseObject(post);
                JSONArray hits = jsonObject.getJSONObject("hits").getJSONArray("hits");
                System.out.println("循环抽取条数：" + hits.size());
                for (int i = 0; i < hits.size(); i++) {
                    JSONObject record = hits.getJSONObject(i);
                    JSONObject source = record.getJSONObject("_source");
                    // System.out.println(source);
                    // String subId = source.getString("events_tag");
                    String userID = source.getString("uid");
                    String create_at = source.getString("created_at");
                    String reports_count = source.getString("reports_count");
                    String comments = source.getString("comments_count");
                    String zans = source.getString("zans_count");
                    String mid = source.getString("sourcemid");
                    String content = source.getString("text");
                    // String urls = source.getString("weibo_url");
                    String subType = source.getString("article_type");
                    String name = source.getString("name");
                    String updateTime = source.getString("updatetime");
                    String sourceUrl = source.getString("source");
                    String pid = source.getString("mid_p");
                    String views = source.getString("read_count");
                    String site_id = source.getString("site_id");
                    String mid_f = source.getString("mid_f");
                    String grade_all = source.getString("grade_all");
                    String reposts_depth = source.getString("reposts_depth");
                    String midO = source.getString("mid");
//                    String events_tag = source.getString("events_tag");
                    pid = pid == null ? "0" : pid;
                    subType = subType == null ? "0" : subType;
                    reports_count = reports_count == null ? "0" : reports_count;
                    reposts_depth = reposts_depth == null ? "0" : reposts_depth;
                    zans = (zans == null ? "0" : zans);
                    comments = (comments == null ? "0" : comments);
                    views = (views == null ? "0" : views);
                    mid_f = (mid_f == null ? "0" : mid_f);
                    grade_all = (grade_all == null ? "0" : grade_all);
                    name = (name == null ? "-" : name);
                    site_id = (site_id == null ? "-" : site_id);
                    if (isNumeric(pid) && !pid.equals("0")) {
                        pid = site_id + "_" + pid;
                        if (mid_f != null && !mid_f.equals("0")) {
                            mid_f = site_id + "_" + mid_f;
                        }
        /*
         * if
         * (df.parse(maxDate).getTime()<df.parse(updateTime).getTime
         * ()) { maxDate=updateTime;
         * 
         * }
         */
                        // System.out.println(source.getString("reports_count"));
                        // System.out.println(source);
                        if (!pid.equals('0')) {
                            mid_ps = mid_ps + ",\"" + pid + "\"";
                        }
                        String sql = "replace into " + tableName + "(midOld,mid,mid_p,mid_f,created_at,`hot spot`,original,article_type,special_ID,seport_count,reposts_depth,name"
                                + ") " + " values ('" + mid + "','" + midO + "','" + pid + "','" + mid_f + "','" + create_at + "',"
                                + grade_all + ",'" + (pid == "0" ? "1" : "0") + "','" + subType + "','" + objectID + "','"
                                + reports_count + "','" + reposts_depth + "','" + name + "')";
                        // System.out.println(sql);

                        try {
                            stateExec.executeUpdate(sql);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                }

            }

        }
        System.out.println("数据插入完毕等待更新mid_p1");

        ////////////////////////////////////////
        try {
//            stateExec.executeUpdate("update " + tableName + " a," + tableName + "  b set a.mid_p1=b.midOld where   b.special_ID= a.special_ID and  a.mid_p=b.name and a.mid_f=b.mid_f and a.mid_p1 is null");
            stateExec.executeUpdate("update " + tableName + " a," + tableName + "  b set a.mid_p1=b.midOld where   b.special_ID= a.special_ID and  a.mid_p=b.name and a.mid_f=b.mid_f and a.mid_p1 ='"+objectID+"'");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            stateExec.executeUpdate("update " + tableName + "  set mid_p1=mid_p  where   mid_p<>'0' and (mid_p REGEXP '[^0-9_]')=0   and mid_p1 is null");
        } catch (Exception e) {
            e.printStackTrace();
        }

        ///////////////////////////////////////
        MySQLHelperPool mySQLHelperPool = new MySQLHelperPool(user_name, password, url_jdbc);
        List<Map<String, Object>> modeResult = mySQLHelperPool.findModeResult("select mid,mid_f,original,midOld,mid_p1 from " + tableName + " where special_ID='" + objectID + "'");
        for (Map<String, Object> objectMap : modeResult) {
            String original = objectMap.get("original") + "";
            String mid = objectMap.get("mid") + "";
            String midOld = objectMap.get("midOld") + "";
            String mid_p1 = objectMap.get("mid_p1") + "";
            if ("1".equals(original)) {
                boolean flag = false;
                for (Map<String, Object> map : modeResult) {
                    String mid_f = map.get("mid_f") + "";
                    if (midOld.equals(mid_f) || midOld.equals(mid_p1)) {
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    System.out.println("有路径:" + mid);
                    mySQLHelperPool.updateByPreparedStatement("update " + tableName + " set path_count=1 where mid='" + mid + "'", null);
                }
            }
        }

        mySQLHelperPool.releaseConnectionPool();
        System.exit(0);

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
update "+tableName+" a,"+tableName+"  b set a.mid_p1=b.mid where a.mid_p=b.name and a.mid_f=b.mid_f and a.mid_p1 is null

 */
