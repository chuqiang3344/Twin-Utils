package excel.weibo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tyaer.net.httpclient.bean.RequestBean;
import com.tyaer.net.httpclient.bean.ResponseBean;
import com.tyaer.net.httpclient.downloader.HttpClientDownloader;
import com.tyaer.util.StringHandle;
import com.tyaer.util.excel.ExcelReader;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Twin on 2017/7/19.
 */
public class Demo {
    static HttpClientDownloader httpHelper = new HttpClientDownloader();
    static String cookie = "SUB=_2AkMuHpn1f8NxqwJRmPAUzGjjZIx3wgjEieKYQmguJRMxHRl-yj83qk8OtRAcFO1UqM9yEAgURLGVn8FCpnVDhw..; SUBP=0033WrSXqPxfM72-Ws9jqgMF55529P9D9WhDRoPbH_0pgTy8DrzrR0Ux; SINAGLOBAL=6297615652438.253.1497503416365; login_sid_t=1386d96a1538dddc72ea7901016d8392; SWB=usrmdinst_5; _s_tentry=-; Apache=5948390306439.251.1500447983013; ULV=1500447983024:5:2:1:5948390306439.251.1500447983013:1498980933814; WBStorage=cd7f674a73035f73|undefined";

    public static void main(String[] args) {
        ExcelReader excelReader = new ExcelReader();
        String[][] table = excelReader.readExceltoTable("App/file/weibo/深圳市网信办重点关注微博微信账号20170718.xls");
//        System.out.println(ArrayUtils.toString(table));

//        excelReader.writeExcel(table, "App/file/weibo/test.xlsx");

        ArrayList<ArrayList<String>> arrayLists = new ArrayList<>();
        for (int i = 1; i < table.length; i++) {
            ArrayList<String> list = new ArrayList<>();
            String[] strings = table[i];
            String n1 = strings[0];
            String name = strings[1];
            String type = strings[2];
            String url = strings[4].replaceAll("\\s", "");
            if (type.equals("微博") && !url.contains("qq.com")) {
//                System.out.println(ArrayUtils.toString(strings));
//                System.out.println(n1 + " " + name + " " + url);
                list.add(name);
                if (StringUtils.isNotBlank(url)) {
                    if (!url.contains("?is_")) {
                        url = url + "?is_all=1";
                    } else if (url.contains("?is_hot")) {
                        url = url.replace("is_hot", "is_all");
                    } else if (!url.contains("http")) {
                        url = "http://" + url;
                    } else if(url.contains("https://m.weibo.cn")){
                        url= url.replace("https://m.weibo.cn","http://weibo.com");
                    }
                }
                ResponseBean responseBean = httpHelper.sendRequest(url, cookie);
                if (responseBean != null) {
                    String rawText = responseBean.getRawText();
                    String domain = StringHandle.praseRegexSimple(rawText, "\\['domain'\\]='(\\d*)';");
                    String oid = StringHandle.praseRegexSimple(rawText, "\\['oid'\\]='(\\d*)';");
                    list.add(oid);
                    list.add(domain);
                    list.add(url);
//                getAccount(n2);
//                break;
                    System.out.println(n1);
                    System.out.println(list);
                } else {
                    System.out.println("null:" + n1);
                }
                arrayLists.add(list);
            }
        }
        excelReader.writeExcel(arrayLists, "App/file/weibo/result1.xlsx");
    }

    @Test
    public void process2(){
        ExcelReader excelReader = new ExcelReader();
        ArrayList<ArrayList<String>> arrayLists = excelReader.readExceltoList("D:\\IdeaProjects\\~Twin\\Twin-Utils\\App\\file\\weibo\\result1.xlsx", 0);
        for (ArrayList<String> arrayList : arrayLists) {
            if (arrayList.get(1).isEmpty()) {
                String key = arrayList.get(0);
                System.out.println(arrayList);
//                getAccount(key,arrayList);
                try{
                    getAccount2(key,arrayList);
                }catch (Exception e){
                    e.printStackTrace();
                }
                System.out.println(arrayList);
            }
        }
        excelReader.writeExcel(arrayLists,"D:\\IdeaProjects\\~Twin\\Twin-Utils\\App\\file\\weibo\\result2.xlsx");
    }

    public static void getAccount2(String key,ArrayList<String> list) {
//        try {
//            key = URLEncoder.encode(key, "utf-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        System.out.println(key);
//        String url = "http://s.weibo.com/weibo/%25E7%258E%258B%25E5%25AE%259D%25E5%25BC%25BA?topnav=1&wvr=6&b=1";
//        String url = "http://s.weibo.com/weibo/" + key + "?topnav=1&wvr=6&b=1";
//        String url = "http://s.weibo.com/weibo/" + key + "&Refer=STopic_box";
        String url = "http://s.weibo.com/ajax/topsuggest.php?key=" + key;
        String rawText = httpHelper.sendRequest(url).getRawText();
//        System.out.println(rawText);
        Pattern pattern = Pattern.compile("window.&\\((.*?)\\)");
        Matcher matcher = pattern.matcher(rawText);
        if (matcher.find()) {
            String group = matcher.group(1);
//            System.out.println(group);
            JSONObject jsonObject = JSON.parseObject(group);
            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("user");
            if (!jsonArray.isEmpty()) {
                JSONObject user = jsonArray.getJSONObject(0);
                String uid = user.getString("u_id");
//                System.out.println(uid);
                String rawText1 = httpHelper.sendRequest("http://weibo.com/u/"+uid, cookie).getRawText();
                String domain = StringHandle.praseRegexSimple(rawText1, "\\['domain'\\]='(\\d*)';");
                list.set(1,uid);
                list.set(2,domain);
                list.set(3,"http://weibo.com/u/"+uid+"?is_all=1");
            }
        }
    }

    public static void getAccount(String key,ArrayList<String> list) {
        try {
            key = URLEncoder.encode(key, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
//        System.out.println(key);
//        String url = "http://s.weibo.com/weibo/%25E7%258E%258B%25E5%25AE%259D%25E5%25BC%25BA?topnav=1&wvr=6&b=1";
//        String url = "http://s.weibo.com/weibo/" + key + "?topnav=1&wvr=6&b=1";
//        String url = "http://s.weibo.com/weibo/" + key + "&Refer=STopic_box";
        String url = "http://s.weibo.com/user/" + key + "&Refer=SUer_box";
        System.out.println(url);
        RequestBean requestBean = new RequestBean(url);
//        requestBean.updateHeaders("cookie",cookie);
//        String html = httpHelper.sendRequest(requestBean).getRawText();
        String html = httpHelper.sendRequest(url, cookie).getRawText();
        if (html.contains("\\u6211\\u771f\\u6ef4\\u4e0d\\u662f\\u673a\\u5668\\u4eba")) {
            System.out.println(html);
            System.out.println("验证码");
            return;
        }
        SinaParseHandle sinaParseHandle = new SinaParseHandle();
//        List<WeiboResult> weiboResults = sinaParseHandle.parseWeiboKeyWords(html);
//        System.out.println(weiboResults.size());
//        System.out.println(weiboResults);
//        sinaParseHandle.searchAccount(html);
        Document pl_user_feedList = getScriptHtml(html, "pl_user_feedList");
//        System.out.println(pl_user_feedList);
        Elements elements = pl_user_feedList.select("p.person_name>a");
        if (!elements.isEmpty()) {
            Element first = elements.first();
            String href = first.attr("href");
//            System.out.println(href);
            String uid = first.attr("uid");
//            System.out.println(uid);
            String rawText = httpHelper.sendRequest(href, cookie).getRawText();
            String domain = StringHandle.praseRegexSimple(rawText, "\\['domain'\\]='(\\d*)';");
//            System.out.println(domain);
            list.set(1,uid);
            list.set(2,domain);
            list.set(3,"http://weibo.com/u/"+uid+"?is_all=1");
        }
    }


    private static Document getScriptHtml(String raw, String scriptId) {
        Elements elements = Jsoup.parse(raw).select("script");
        return getScriptHtml(elements, scriptId);
    }

    private static Document getScriptHtml(Elements elements, String scriptId) {
        Document document = null;
        scriptId = "\"pid\":\"" + scriptId;
        for (Element element : elements) {
            String script = element.toString();
            if (script.contains(scriptId)) {
                String jsonStr = StringHandle.praseRegexSimple(script, "STK.pageletM.view\\((.*)\\)");
                if (jsonStr != null && jsonStr.startsWith("{")) {
                    try {
                        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
                        String html = jsonObject.getString("html");
                        if (StringUtils.isNotBlank(html)) {
                            document = Jsoup.parse(html);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println(jsonStr);
                    }
                }
                break;
            }
        }
        return document;
    }
}
