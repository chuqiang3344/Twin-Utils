import com.alibaba.fastjson.JSON;
import com.izhonghong.api.sina.SinaApiClient;
import com.izhonghong.api.sina.bean.ArticleType;
import com.izhonghong.api.sina.bean.ICrawlBean;
import com.weibo.api.motan.common.MotanConstants;
import com.weibo.api.motan.util.MotanSwitcherUtil;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;


public class Demo {

    private static SinaApiClient sinaApiClient;
    private static ApplicationContext ctx;

    static {
        MotanSwitcherUtil.setSwitcherValue(MotanConstants.REGISTRY_HEARTBEAT_SWITCHER, true);
        ctx = new ClassPathXmlApplicationContext("classpath:motan_client.xml");
        sinaApiClient = (SinaApiClient) ctx.getBean("SinaApiClient");
    }

    public static void main(String[] args) {
        String url = "http://mp.weixin.qq.com/s?src=11&timestamp=1503382326&ver=345&signature=HJ-g0JA7CnkrywAnrNWD4sIJSwittROMB8bzVm9Qw5HhPTUskjP8cBAv0upD8omcScOHZYr-EjMaMb-yXcRh3ltd*AAYypA2qRWXPHAGW7oBaCYf0yJ3xv*2-ofGX5gq&new=1";
        System.out.println(sinaApiClient.parseWechatArticle(url));
    }

    @Test
    public void getUserInfo() {
        String url = "http://weibo.com/cailiang";
        System.out.println(sinaApiClient.getUserInfo(url));
    }

    @Test
    public void parseWechatArticle(){
        String url = "http://mp.weixin.qq.com/s/pGbZyxu8R4GuE7SQnjx_KA";
//        String url = "http://mp.weixin.qq.com/s?src=11&timestamp=1503382326&ver=345&signature=HJ-g0JA7CnkrywAnrNWD4sIJSwittROMB8bzVm9Qw5HhPTUskjP8cBAv0upD8omcScOHZYr-EjMaMb-yXcRh3ltd*AAYypA2qRWXPHAGW7oBaCYf0yJ3xv*2-ofGX5gq&new=1";
        System.out.println(sinaApiClient.parseWechatArticle(url));
    }


    @Test
    public void search(){
        System.out.println(sinaApiClient.searchWbUser("金心异南方评论"));
    }


    @Test
    public void articleAdd() {
//        String url = "http://news.southcn.com/c/2010-10/29/content_17144691.htm";
        String url = "https://mbd.baidu.com/newspage/data/landingsuper?context=%7B%22nid%22%3A%22news_4934791543695426373%22%7D&n_type=0&p_from=1";
        boolean b = sinaApiClient.articleAdd(url, false);
    }

    @Test
    public void articleAdd2() {
        ICrawlBean iCrawlBean = new ICrawlBean();
        iCrawlBean.setArticle_type(1);//0:微博,1:新闻,2:微信,3:论坛,4:博客,5:报纸,6:视频,7:qq,8:跟帖,9:境外,10:twitter,11:新闻客户端
        iCrawlBean.setUrl("https://mbd.baidu.com/newspage/data/landingsuper?context=%7B%22nid%22%3A%22news_10489440142895024555%22%7D&n_type=0&p_from=1");
        iCrawlBean.setTitle("女子将手机“当”了1700元 骗回再“当”1700元");
        iCrawlBean.setText("手机“当”了1700元\n" +
                "骗回再“当”1700元\n" +
                "店主发现上当立即报警，女子故伎重演后被警方抓获\n" +
                "广西新闻网-南国今报柳州讯（记者许洁琳 通讯员陈曦）近日，一名女子在柳州市谷埠街以1700元“当”了一部手机，12月4日中午空手去赎回手机，趁店主不备拿着手机溜之大吉。当日下午，又故伎重施到另一家手机店“当”掉了这部手机，正当她想拿着再次到手的1700元去挥霍时，在飞鹅路被柳北巡警大队民警抓获。\n" +
                "据报警人李女士介绍，11月24日，一名姓熊的女子拿来一部价值2900元的手机，以1700元的价格作抵押。双方约定抵押期限为一个星期，若到期不赎回，手机就归李所有。\n" +
                "直到12月4日中午12时许，熊才再次出现。熊说，因为自己手头紧，所以拖了几天，现在有一名朋友答应借钱帮她赎回手机，晚点就会过来，在朋友赶到之前，她想先检查一下手机。李于是把手机交给熊，熊坐在一旁，边摆弄手机边“等人”，李则忙着招呼别的客人去了。\n" +
                "半小时后，李才发现熊某连人带手机都不见了。于是赶紧报警。\n" +
                "民警及时调取附近天网监控进行案件侦查，于4日傍晚6时许，在飞鹅路某宾馆门前将诈骗嫌疑人熊某抓获。\n" +
                "这时，熊某刚在谷埠街另一家手机店以同样的价钱把手机又抵押出去了，身上还有刚拿到的1700元。熊某承认，自己根本没有能力还钱，也没有朋友可以借钱，本打算几天后再以同样手段将手机骗回的，没想到仅几个小时便落入法网。\n" +
                "目前，熊某已被警方依法行政拘留。");
        iCrawlBean.setCreated_at(1512541209000L);
        boolean b = sinaApiClient.articleAdd(iCrawlBean, false);
    }

    @Test
    public void dataAddSuper() {
        HashMap<String, ArticleType> hashMap = new HashMap<>();
        hashMap.put("https://mbd.baidu.com/newspage/data/landingsuper?context=%7B%22nid%22%3A%22news_5781681733635819852%22%7D&n_type=0&p_from=1", ArticleType.ARTICLE);
        hashMap.put("https://www.weibo.com/5044281310/FxvdWFyLV?from=page_1002065044281310_profile&wvr=6&mod=weibotime", ArticleType.WEIBO);
        hashMap.put("http://mp.weixin.qq.com/s?__biz=MzI3NDQzODU4Nw==&mid=2247486429&idx=1&sn=258664a5b8228bee6e9eb1ddd1bde07c&chksm=eb154b09dc62c21f1af1b5a74b3231572e845e2703bd533d7bac494d18e8f6a23127d6ef1a64&scene=0#wechat_redirect", ArticleType.WEIXIN);
        boolean b = sinaApiClient.dataAddSuper(hashMap);
    }


    @Test
    public void articleUpdate() {
        String data = "{\"hanmingCode\":\"尲筟蒲厖抬噓牐刃丆于渕溻狐和伝礼砈橦欝撁牌塃亄蚙分芘葉孇炅幐勭夔\",\"orgs_tag\":\"10032,61\",\"events_tag\":\"100574,100565,100566,100523,101669\",\"reports_count\":\"0\",\"mid\":\"ef201000875_4MMRMjGGPuIAvz9q+B36fg==\",\"created_at\":\"2017-11-10 00:00:00.0\",\"sourcemid\":\"1000875_4MMRMjGGPuIAvz9q+B36fg==\",\"source\":\"南方日报\",\"title\":\"万力集团总部 万力中心奠基\",\"uid\":\"96671000875_\",\"grade_all\":\"0\",\"zan_count\":\"0\",\"text_loc_country\":\"中国\",\"reposts_count\":\"0\",\"text\":\"11月9日，海珠区工业大道金沙路9号橡胶大院地块迎来新生，广州市属国企广州万力集团总部大厦——万力中心奠基仪式在此举行。仪式以“聚智新时代 启航新征程”为主题，寓意更新老工业区面貌，汇才引智，科技创新，积极行动在创新驱动发展的新时代，开启智能制造新征程。\\\\n 万力集团是广州市唯一一家聚焦大化工产业的国有全资大型实业集团，历经重组改革和创新发展，已逐步成长为跨轮胎、橡胶制品、化工、房地产、金融等领域，布局在广州、珠海、合肥、沈阳、梧州等省市，集资产经营与资本运营于一体的综合性集团。近年来，万力集团坚持“科技创新+总部经济”双轮驱动，立足广州，积极探索智能化、国际化发展；通过设立万力创新园，引入国外前端技术，着力打造国家级试验中心；通过跨界整合、协作创新，建成轮胎行业最先进智能工厂。\\\\n 万力中心坐落于白鹅潭商业圈，毗邻太古仓和中船汇两大商业区，距离沙园地铁站直线距离300米。万力中心总建筑用地2万平方米，建筑面积4.8万平方米，不仅承载集团总部办公的职能，更是聚焦技术创新、智能制造，吸引高端人才，着力打造智能制造和产业创新中心的举措，从而以实际行动落实习近平总书记对广东工作重要批示精神，助力广州国际科技创新枢纽建设。万力中心总投资10.8亿元，预计2020年竣工。\\\\n 万力集团董事长付守杰说：“100多年前，中国第一家橡胶加工厂在我们脚下这片热土诞生，开创了我国橡胶工业之先河。今天奠基的万力中心将依托百年橡胶工业深厚底蕴，广泛应用绿色环保新技术、新材料，着力打造海珠区工业文明商业新地标。”\\\\n 李广军\",\"orgs_event_tag\":\"10030,10017,10044\",\"text_loc\":\"广东\",\"text_loc_city\":\"广州市\",\"read_count\":\"0\",\"text_loc_county\":\"海珠区\",\"zans_count\":\"0\",\"article_type\":\"5\",\"crawler_time\":\"2017-11-10 06:57:49.000\",\"crawler_site_id\":\"1000875\",\"text_loc_province\":\"广东\",\"article_url\":\"http://epaper.southcn.com/nfdaily/html/2017-11/10/content_7680724.htm#3#\",\"emotion\":\"正面\",\"comments_count\":\"0\",\"download_type\":\"-1\",\"name\":\"李广军\",\"subjects_tag\":\"585,101807,101782,101783,101840,101863,101765,101766,101865,101767,101868,101870,101874,101875,101881\",\"created_date\":\"20171110\",\"_id\":\"ef201000875_4MMRMjGGPuIAvz9q+B36fg==\",\"updatetime\":\"2017-11-30 19:54:00.778\"}";
        boolean b = sinaApiClient.articleUpdate(data, true);
    }

    @Test
    public void zuz(){

    }


}
