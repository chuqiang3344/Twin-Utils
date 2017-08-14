
package com.tyaer.elasticsearch;


import java.util.List;
import java.util.Map;


public class ElasticSearchUtils<T> {

	static final String indexDay = "ams_data";
	static final String indexWeek = "i_ams_week_data";
	static final String typeWeiboDay = "t_status_weibo";
	static final String typeArticleDay = "t_article";
	static final String typeWeiboWeek = "t_status_weibo";
	static final String typeGongzhonghao = "t_gzh";
	static final String T_USER="t_user";
	
	public  void insertWeiboDayInfo(List<Object> list) {
		if(list.size()==0){
			return ;
		}

		ElasticSearchHelper.BulkIndex(indexDay,typeWeiboDay, list);
	}
	
	
	public  void insertArticleDayInfo(List<Object> list) {
		if(list.size()==0){
			return ;
		}

		ElasticSearchHelper.BulkIndex(indexDay,typeArticleDay, list);
	}
	
	public  void insertGongzhonghao(List<Object> list) {
		if(list.size()==0){
			return ;
		}

		ElasticSearchHelper.BulkIndex(indexDay,typeGongzhonghao, list);
	}
	
	

	public void insertWeiboWeekInfo(List<Object> list) {
//		if(list==null||list.size()==0){
//			return ;
//		}
//		ChangeMidUtils changeMidUtils = new ChangeMidUtils();
//		List<Object> input = changeMidUtils.changeMid(list);
//		
//		ElasticSearchHelper e = new ElasticSearchHelper<>();
		//e.BulkIndex(indexWeek, typeWeiboDay, input);
		
	}


	public void insertHasParent(String type, List<Object> list) {
		if(list.size()==0){
			return ;
		}
		ElasticSearchHelper e = new ElasticSearchHelper<>();
		e.BulkInsertHasParent(indexDay, type, list);
	}

	private void insertHasParentSingle(String type, Object bean, String id) {
		ElasticSearchHelper e = new ElasticSearchHelper<>();
		e.InsertHasParent(indexDay, type, id, bean, id);
	}

	public void updateWeibo(String id, Map map) {
		if(map ==null||map.size()==0){
			return ;
		}
		ElasticSearchHelper e = new ElasticSearchHelper<>();
		e.update(indexDay, typeWeiboDay, id, map);
	//	e.update(indexWeek, typeWeiboDay, id, map);
	}
	
	public boolean exists(String type,String id){
		 ElasticSearchHelper e = new ElasticSearchHelper<>();
		return e.exists(indexDay, type, id);
	}
}
