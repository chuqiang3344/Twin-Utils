package com.tyaer.elasticsearch.bean;

public class ESConstants {

	public final static String ESClusterName = "izhonghong";
	public final static String ESIP = "10.248.161.40:9300,10.248.161.40:9301,10.248.161.40:9302,10.248.161.35:9300,10.248.161.10:9300,10.248.161.10:9301,10.248.161.40:9302,10.248.161.31:9300,10.248.161.31:9301,10.248.161.31:9302,10.248.161.32:9300,10.248.161.32:9301,10.248.161.32:9302";
	//public final static String ESIP = "192.168.2.116:9300";
	public final static  String INDEX = "weibo2";
	public final static  String DAYINFO = "dayInfo";
	public final static  String WEEKINFO = "weekInfo";
	public final static  String ALARM = "alarm";
	public final static  String EVENT="event";
	public final static  String TOPIC = "topic";
	
	
	public final static String ESTypeTopic="t_orgin_subject";  //专题
	
	public final static String ESTypeEvent= "t_event"; //shijian  
	public final static String ESTypeAlarm= "t_alert_subject"; // yu jing
	public final static String ESTypeStataWei="vt_status_weibo";
	
	
	
	public final static String industries_tag= "industries_tag";
	public final static String subjects_tag= "subjects_tag";
	public final static String orgs_tag= "orgs_tag";
	public final static String orgs_event_tag= "orgs_event_tag";
	public final static String orgs_alert_tag= "orgs_alert_tag";
	public final static String events_tag= "events_tag";
	public final static String alerts_tag= "alerts_tag";
}
