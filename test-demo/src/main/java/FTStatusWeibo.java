import com.alibaba.fastjson.JSON;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @Author guohongdou.
 * @Date 16/8/8.
 * @Version 0.0.1.
 * @Desc
 * 		<p>
 *       VMSES vms二期 es 全库微博bean
 *       </p>
 *       .
 * @Update 16/8/8.
 */
public class FTStatusWeibo implements Cloneable,Serializable{
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}
	private String key;

	public String getKey() {
		return mid;
	}

	public void setKey(String key) {
		this.key = key;
	}

	private String mid; // 微博消息id，文档唯一标示.

	private String mid_f; // 首发微博消息id (站点Id_官网id).

	private String mid_p; // 转发微博消息id (站点Id_官网id).

	private Integer reposts_depth; // 转发层级.

	private String source; // 微博来源,多个用','隔开.

	private Timestamp created_at; // 创建时间，精确到秒（微博发布时间）.

	private String uid; // 作者id (站点Id_官网id).

	private String text_loc_country; // 微博国家（我们自己的地址编号）.

	private String text_loc_province; // 微博省份（我们自己的地址编号）.

	private String text_loc_city; // 微博城市（我们自己的地址编号）.
	private String text_loc_county; // 微博县（我们自己的地址编号）.

	private String emotion; // 微博正负面.

	public String getText_loc_county() {
		return text_loc_county;
	}

	public void setText_loc_county(String text_loc_county) {
		this.text_loc_county = text_loc_county;
	}

	private String text; // 信息内容.

	private String user_local; // 用户所在地（用户发微博时所在的地理位置）.

	private String text_subject; // 从属话题##.

	private String join_v; // 微博参与人-大V.

	private Integer reposts_count; // 转发数.
	
	private Integer reports_count;
	
	private Integer zans_count;

	private Integer comments_count; // 评论数.
	
	private Integer zan_count;

	private Integer grade_all; // 微博热度.

	private String pic_local; // 本地图片地址，多个图片用 “,” 号隔开.

	private String media_local; // 本地媒体类型:地址；多个用 “,” 号隔开.

	// private long score; // 热度汉明值.

	private String VIP; // VIP标签.

	private String industries_tag; // 行业标签.

	private String name;

	private String weibo_url;

	private String text_loc;
	
	private Integer site_id;
	
	private String updatetime;
	
    private String profileImageUrl;
    
     private String sourceMid;
     
     private String pic;
	
	
     private String created_date;
 	
 	private String title_top;
 	
 	
    private String address;
	
	private String download_type;
	
    private String verified_type;
	
    private String verifiedtype;
	
    private Integer crawler_site_id;
    
	private String crawler_time;
	
	private String push_types;
	
	

	private String hanmingCode;
	
	private String manual_update_time;
	
	private String rubbish;
	
	private String recommends_tag;
	
	
	private String at_who; // 该微博@了谁
	  
	  
	
	
	
	public String getAt_who() {
		return at_who;
	}

	public void setAt_who(String at_who) {
		this.at_who = at_who;
	}

	public String getRecommends_tag() {
		return recommends_tag;
	}

	public void setRecommends_tag(String recommends_tag) {
		this.recommends_tag = recommends_tag;
	}

	public String getRubbish() {
		return rubbish;
	}

	public void setRubbish(String rubbish) {
		this.rubbish = rubbish;
	}

	public String getManual_update_time() {
		return manual_update_time;
	}

	public void setManual_update_time(String manual_update_time) {
		this.manual_update_time = manual_update_time;
	}

	public String getHanmingCode() {
		return hanmingCode;
	}

	public void setHanmingCode(String hanmingCode) {
		this.hanmingCode = hanmingCode;
	}

	public String getPush_types() {
		return push_types;
	}

	public void setPush_types(String push_types) {
		this.push_types = push_types;
	}

	public String getCrawler_time() {
		return crawler_time;
	}

	public void setCrawler_time(String crawler_time) {
		this.crawler_time = crawler_time;
	}

	public Integer getCrawler_site_id() {
		return crawler_site_id;
	}

	public void setCrawler_site_id(Integer crawler_site_id) {
		this.crawler_site_id = crawler_site_id;
	}

	public String getVerifiedtype() {
	return verifiedtype;
}

public void setVerifiedtype(String verifiedtype) {
	this.verifiedtype = verifiedtype;
}

	
	
	public Integer getZans_count() {
	return zans_count;
}

public void setZans_count(Integer zans_count) {
	this.zans_count = zans_count;
}

	public String getVerified_type() {
		return verified_type;
	}

	public void setVerified_type(String verified_type) {
		this.verified_type = verified_type;
	}
	
	
	

	public String getDownload_type() {
		return download_type;
	}

	public void setDownload_type(String download_type) {
		this.download_type = download_type;
	}

	public String getAddress() {
		return address;
	}
 	
 	
 	public void setAddress(String address) {
		this.address = address;
	}

	public String getTitle_top() {
		return title_top;
	}

	public void setTitle_top(String title_top) {
		this.title_top = title_top;
	}

	public String getCreated_date() {
 		return created_date;
 	}

 	public void setCreated_date(String created_date) {
 		this.created_date = created_date;
 	}
	
	
	public String getPic() {
		return pic;
	}

	public void setPic(String pic) {
		this.pic = pic;
	}

	public String getSourceMid() {
		return sourceMid;
	}
	public void setSourceMid(String sourceMid) {
		this.sourceMid = sourceMid;
	}
    
    public String getProfileImageUrl() {
		return profileImageUrl;
	}

	public void setProfileImageUrl(String profileImageUrl) {
		this.profileImageUrl = profileImageUrl;
	}

	

	public String getUpdatetime() {
		return new Timestamp(System.currentTimeMillis()).toString();
	}

	public void setUpdatetime(String updatetime) {
		this.updatetime = updatetime;
	}

	
	

	public Integer getSite_id() {
		return site_id;
	}

	public void setSite_id(Integer site_id) {
		this.site_id = site_id;
	}

	public String getText_loc() {
		return text_loc;
	}

	public void setText_loc(String text_loc) {
		this.text_loc = text_loc;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getWeibo_url() {
		return weibo_url;
	}

	public void setWeibo_url(String weibo_url) {
		this.weibo_url = weibo_url;
	}

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public String getMid_f() {
		return mid_f;
	}

	public void setMid_f(String mid_f) {
		this.mid_f = mid_f;
	}

	public String getMid_p() {
		return mid_p;
	}

	public void setMid_p(String mid_p) {
		this.mid_p = mid_p;
	}

	

	public Integer getReposts_depth() {
		return reposts_depth;
	}

	public void setReposts_depth(Integer reposts_depth) {
		this.reposts_depth = reposts_depth;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Timestamp getCreated_at() {
		return created_at;
	}

	public void setCreated_at(Timestamp created_at) {
		this.created_at = created_at;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getText_loc_country() {
		return text_loc_country;
	}

	public void setText_loc_country(String text_loc_country) {
		this.text_loc_country = text_loc_country;
	}

	public String getText_loc_province() {
		return text_loc_province;
	}

	public void setText_loc_province(String text_loc_province) {
		this.text_loc_province = text_loc_province;
	}

	public String getText_loc_city() {
		return text_loc_city;
	}

	public void setText_loc_city(String text_loc_city) {
		this.text_loc_city = text_loc_city;
	}

	public String getEmotion() {
		return emotion;
	}

	public void setEmotion(String emotion) {
		this.emotion = emotion;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getUser_local() {
		return user_local;
	}

	public void setUser_local(String user_local) {
		this.user_local = user_local;
	}

	public String getText_subject() {
		return text_subject;
	}

	public void setText_subject(String text_subject) {
		this.text_subject = text_subject;
	}

	public String getJoin_v() {
		return join_v;
	}

	public void setJoin_v(String join_v) {
		this.join_v = join_v;
	}

	

	

	public Integer getReposts_count() {
		return reposts_count;
	}

	public void setReposts_count(Integer reposts_count) {
		this.reposts_count = reposts_count;
	}

	public Integer getReports_count() {
		return reports_count;
	}

	public void setReports_count(Integer reports_count) {
		this.reports_count = reports_count;
	}

	public Integer getComments_count() {
		return comments_count;
	}

	public void setComments_count(Integer comments_count) {
		this.comments_count = comments_count;
	}

	public Integer getZan_count() {
		return zan_count;
	}

	public void setZan_count(Integer zan_count) {
		this.zan_count = zan_count;
	}

	public Integer getGrade_all() {
		return grade_all;
	}

	public void setGrade_all(Integer grade_all) {
		this.grade_all = grade_all;
	}

	public String getPic_local() {
		return pic_local;
	}

	public void setPic_local(String pic_local) {
		this.pic_local = pic_local;
	}

	public String getMedia_local() {
		return media_local;
	}

	public void setMedia_local(String media_local) {
		this.media_local = media_local;
	}

	// public long getScore() {
	// return score;
	// }
	//
	// public void setScore(long score) {
	// this.score = score;
	// }

	public String getVIP() {
		return VIP;
	}

	public void setVIP(String VIP) {
		this.VIP = VIP;
	}

	public String getIndustries_tag() {
		return industries_tag;
	}

	public void setIndustries_tag(String industries_tag) {
		this.industries_tag = industries_tag;
	}

	/**
	 * Returns a string representation of the object. In general, the
	 * {@code toString} method returns a string that "textually represents" this
	 * object. The result should be a concise but informative representation
	 * that is easy for a person to read. It is recommended that all subclasses
	 * override this method.
	 * <p>
	 * The {@code toString} method for class {@code Object} returns a string
	 * consisting of the name of the class of which the object is an instance,
	 * the at-sign character `{@code @}', and the unsigned hexadecimal
	 * representation of the hash code of the object. In other words, this
	 * method returns a string equal to the value of: <blockquote>
	 * 
	 * <pre>
	 * getClass().getName() + '@' + Integer.toHexString(hashCode())
	 * </pre>
	 * 
	 * </blockquote>
	 *
	 * @return 返回改实体bean名称以及标示字段.
	 */
//	@Override
//	public String toString() {
//		return "{'obj':{'index':'i_ams_total_data','type':'t_status_weibo','id':'mid'}}";
//	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
}
