package excel.weibo;

import com.alibaba.fastjson.JSONObject;
import com.tyaer.net.httpclient.downloader.HttpClientDownloader;
import com.tyaer.util.StringHandle;
import excel.weibo.pojo.WeiboResult;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Twin on 2017/7/19.
 */
public class SinaParseHandle {
    private static final String eWho_regex = "usercard=\"name=(.+?)\"";
    private static final String topic_regex = ">#(.+?)#<";

//    /**
//     * 0 domain获取
//     *
//     * @param content
//     * @return
//     */
//    public DomainSource parseDomainTask(String content, String url) {
//        DomainSource domainSource = new DomainSource();
//        String uid = StringHandle.praseRegexSimple(url, "com/u/(\\d+)\\?");
//        domainSource.setUserId(uid);
//        Document document = Jsoup.parse(content);
//        String title = document.select("title").text();
//        if (!title.contains("的微博_微博")) {
//            if (title.contains("随时随地发现新鲜事")) {
//                logger.error(uid + " parseDomainTask帐号异常，不存在：" + url);
////                domainSource.setDomain("000");//错误的用户
//                domainSource.setUserStatus(2);
//                feedbackExcUser(0,uid,2);
//                return domainSource;
//            } else if (title.contains("404错误")) {
//                logger.error(uid + " parseDomainTask帐号异常：" + url);
////                domainSource.setDomain("404");//错误的用户
//                domainSource.setUserStatus(1);
//                feedbackExcUser(0,uid,1);
//                return domainSource;
//            } else {
//                return null;
//            }
//        }
//        String domain = StringHandle.praseRegexSimple(content, "\\['domain'\\]='(\\d*)';");
//        if (domain == null || domain.length() != 6) {
//            logger.error("parseDomainTask domain error: " + domain);
////            domain = "000";
////            domain = "100505";
//            domain = "";
//        }
//        domainSource.setDomain(domain);
//        Elements scripts = document.select("script");
//        domainSource.setVerified_type(getVerifiedType(scripts));
//        /**
//         * 粉丝、关注、微博
//         */
//        Document doc_T8CustomTriColumn = getScriptHtml(scripts, "Pl_Core_T8CustomTriColumn");//粉丝关注微博
//        if (doc_T8CustomTriColumn != null) {
//            try {
//                String follow_count = doc_T8CustomTriColumn.select("tr>td.S_line1:nth-child(1)").select("strong").text();
//                String fans_count = doc_T8CustomTriColumn.select("tr>td.S_line1:nth-child(2)").select("strong").text();
//                String weibo_count = doc_T8CustomTriColumn.select("tr>td.S_line1:nth-child(3)").select("strong").text();
//                domainSource.setConcernNum(getInteger(follow_count));
//                domainSource.setFansNum(getInteger(fans_count));
//                domainSource.setSumWeibo(getInteger(weibo_count));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        return domainSource;
//    }

    /**
     * 1 关键词解析
     *
     * @param content
     * @return
     */
    public List<WeiboResult> parseWeiboKeyWords(String content) {
        List<WeiboResult> list = new ArrayList<>();
        Document doc = Jsoup.parse(content);
        Elements ele = doc.select("script");
        String js = "";
        for (Element ele1 : ele) {
            if (ele1.toString().contains("\"pl_weibo_direct\"")) {
                js = ele1.toString();
                break;
            }
        }
        Pattern pattern = Pattern.compile("<script>STK && STK.pageletM && STK.pageletM.view\\((.*)\\)</script>");
        Matcher matcher = pattern.matcher(js);
        String weibo_json = "";
        String weibo = "";
        try {
            while (matcher.find()) {
                weibo_json = matcher.group(1);
            }
            JSONObject jsonObject = JSONObject.parseObject(weibo_json);
            weibo = jsonObject.getString("html");
        } catch (Exception e) {
            weibo = content;
        }
        doc = Jsoup.parse(weibo);
        Elements ele_weibos = doc.select("div.WB_cardwrap.S_bg2.clearfix>div[action-type=feed_list_item]");
        for (Element ele_weibo : ele_weibos) {
            WeiboResult weiboResult = new WeiboResult();
            Elements ele_weibo_1 = ele_weibo.select("div.WB_feed_detail.clearfix");
            Elements ele_weibo_2 = ele_weibo.select("div.feed_action.clearfix");
            String imgs = "";
            try {
                String head_url = ele_weibo_1.select("div.face").select("img").attr("src");
                String name = ele_weibo_1.select("div.face").select("a").attr("title");
                Elements clearfix_elements = ele_weibo_1.select("div.content.clearfix");
                String rz = clearfix_elements.select("a.approve").attr("title");
                String comment_txt = clearfix_elements.select("p.comment_txt").toString();
                String text = clearfix_elements.select("p.comment_txt").text();
                String date = clearfix_elements.select("div.feed_from.W_textb>a:nth-child(1)").attr("date");
                String url = clearfix_elements.select("div.feed_from.W_textb>a:nth-child(1)").attr("href");
                String uid = StringHandle.praseRegexSimple(url, "com\\/(.*?)\\/");
                String source = clearfix_elements.select("div.feed_from.W_textb>a:nth-child(2)").text();
                String repeat_count = ele_weibo_2.select("span.line.S_line1").get(1).select("em").text();
                String comment_count = ele_weibo_2.select("span.line.S_line1").get(2).select("em").text();
                String like_count = ele_weibo_2.select("span.line.S_line1").get(3).select("em").text();
                String mid = ele_weibo.select("div[action-type=feed_list_item]").attr("mid");
                Elements ele_imgs = ele_weibo_1.select("img.bigcursor");
//			System.out.println(mid);
                for (Element ele_img : ele_imgs) {
                    imgs += ele_img.attr("src").replace("square", "bmiddle") + ",";
                }
                String at_who = StringHandle.praseRegexMore(comment_txt, eWho_regex);
                String text_subject = StringHandle.praseRegexMore(comment_txt, topic_regex);
                weiboResult.setAt_who(at_who);
                weiboResult.setText_subject(text_subject);
                weiboResult.setUrl(url);
                weiboResult.setUid(uid);
                weiboResult.setProfileImageUrl(head_url);
                weiboResult.setName(name);
                weiboResult.setVerified_type(rz);
                weiboResult.setText(text);
                weiboResult.setCreated_at(date);
                weiboResult.setSource(source);
                weiboResult.setReposts_count(getInteger(repeat_count));
                weiboResult.setComments_count(getInteger(comment_count));
                weiboResult.setZan_count(getInteger(like_count));
                weiboResult.setMid(mid);
                weiboResult.setPic(imgs);
                list.add(weiboResult);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(ele_weibo);
            }
        }
        pattern = Pattern.compile("第(.*?)页");
        matcher = pattern.matcher(doc.toString());
        int maxPage = 0;
        int groupCount = matcher.groupCount();
        String[] result = new String[groupCount];
        while (matcher.find()) {
            for (int i = 1; i < groupCount + 1; i++) {
                result[i - 1] = matcher.group(i);
                int page = getInteger(result[i - 1]);
                if (page > maxPage) {
                    maxPage = page;//最大页数
                }
            }
        }
        return list;
    }

    /**
     * 1 关键词解析
     *
     * @param content
     * @return
     */
    public WeiboResult searchAccount(String content) {
        List<WeiboResult> list = new ArrayList<>();
        Document doc = Jsoup.parse(content);
        Elements ele = doc.select("script");
        String js = "";
        for (Element ele1 : ele) {
            if (ele1.toString().contains("\"pl_weibo_direct\"")) {
                js = ele1.toString();
                break;
            }
        }
        Pattern pattern = Pattern.compile("<script>STK && STK.pageletM && STK.pageletM.view\\((.*)\\)</script>");
        Matcher matcher = pattern.matcher(js);
        String weibo_json = "";
        String weibo = "";
        try {
            while (matcher.find()) {
                weibo_json = matcher.group(1);
            }
            JSONObject jsonObject = JSONObject.parseObject(weibo_json);
            weibo = jsonObject.getString("html");
        } catch (Exception e) {
            weibo = content;
        }
        doc = Jsoup.parse(weibo);
        System.out.println(doc);
        Element ele_weibo = doc.select("div.star_detail>p.star_name>a").get(0);
        String href = ele_weibo.attr("href");
        System.out.println(href);
        return null;
    }

    public Integer getInteger(String str) {
        Integer integer = 0;
        if (StringUtils.isNotBlank(str)) {
            str = str.trim();
            try {
                integer = Integer.valueOf(str);
            } catch (Exception e) {
                System.out.println("string getInteger fail:" + str);
            }
        }
        return integer;
    }
}
