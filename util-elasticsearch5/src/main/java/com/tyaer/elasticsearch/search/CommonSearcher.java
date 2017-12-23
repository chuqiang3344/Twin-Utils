package com.tyaer.elasticsearch.search;

import org.apache.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CommonSearcher {
	
    private static Logger logger = Logger.getLogger(CommonSearcher.class);
	
	public static String format(String expression){
		System.out.println("begin format expression:"+expression);
		expression=expression.replace("（", "(").replace("）", ")");
		expression = expression.trim();
		if(expression.matches("(\\+|\\-).*")){
    		expression = expression.substring(1);
    	}
    	if(expression.matches(".*(\\+|\\-)")){
    		expression = expression.substring(0,expression.length()-1);
    	}
    	
    	String[] regexs = {"\\(([^\\+\\-\\(\\)\\s]+)\\)([\\*]?)\\(([^\\+\\-\\(\\)\\s]+)\\)",
    			"([^\\+\\-\\(\\)\\s]+)([\\*]?)\\(([^\\+\\-\\(\\)\\s]+)\\)",
    			"\\(([^\\+\\-\\(\\)\\s]+)\\)([\\*]?)([^\\+\\-\\(\\)\\s\\|]+)"};
    	for(String regex:regexs){
    		Pattern pattern = Pattern.compile(regex);
    		Matcher matcher = pattern.matcher(expression);
    		if (matcher.find()) {
    			 String match = matcher.group(0);
    			String group1 = matcher.group(1);
    			String group2 = matcher.group(2);
    			String group3 = matcher.group(3);
    			String[] sub1=group1.split("\\|");
    			String[] sub2 = group3.split("\\|");
    			StringBuilder sb = new StringBuilder();
    			String operator = "|";
    			if(group2.equals("-")){
    				operator="+";
    			}
    			sb.append("(");
    			for(String s1:sub1){
    				for(String s2:sub2){
    					if(sb.length()>1){
    						sb.append(operator);
    					}
    					sb.append(s1).append(group2).append(s2);
    				}
    			}
    			sb.append(")");
    			expression=expression.replace(match, sb.toString());
    			break;
    		}
    	}
		System.out.println("after format expression:"+expression);
    	return expression;
	}
	
	public static QueryBuilder builderQuery(String expression, List<Integer> searchScopes){
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		expression=expression.replace("（", "(").replace("）", ")");
		List<String> fields = ExpressionTransform.split(ExpressionTransform.resolveExpression(expression));
		for(String field:fields){
			queryBuilder.should(builderQuery2(field, searchScopes));
		}
	   return queryBuilder;
	}
	
	public static QueryBuilder builderQuery2(String expression, List<Integer> searchScopes){
		try{
		 	
			
			
			int start = 0;
	    	int end =0;
	    	int bracketLevel = 0;
	    		    	
	    	while(!expression.equals(expression=format(expression))){
	    		
	    	}
	    	
	    	expression = expression.replaceAll("\\s+", "+");
	    	expression = expression.replaceAll("\\++", "+");
	    	List<String> subExps = new ArrayList<String>();
	    	List<String> operators= new ArrayList<String>();
	    	for(int i=0;i<expression.length();i++){
	    		end=i;
	    		char c = expression.charAt(i);
	    		if(c=='('){
	    			bracketLevel++;
	    		}else if(c==')'){
	    			bracketLevel--;
	    		}   
	    		if(bracketLevel==0){
	    			if(c=='|'||c=='+'||c=='-'){
	        			
	        			String subString = expression.substring(start,end);
	        			subExps.add(subString);
	        			start=i+1;
	        			operators.add(c+"");
	        		}
	    		}
	    		
	    		
	    	}
	    	if(start<expression.length()){
	    		subExps.add(expression.substring(start, expression.length()));
	    	}
	    	System.out.println();

	    	if(subExps.size()==0){
	    		return null;
	    	}
	    	QueryBuilder beginQuery =null;
	    	String beginExp = subExps.get(0);
	    	if(beginExp.contains("(")){
	    		beginQuery = builderQuery(beginExp.substring(1,beginExp.length()-1),searchScopes);
	    	    if(beginQuery==null){
	    	    	return null;
	    	    }
				}else{
					if(beginExp.contains("*")){

			    		   String regex = beginExp.replace("*", " ");
						   //beginQuery= QueryBuilders.matchPhraseQuery("text", regex).slop(5);
						   beginQuery = matchPhraseQuery(regex,searchScopes,5);
						}else{
							//beginQuery = QueryBuilders.matchPhraseQuery("text", beginExp).slop(1);
							if(beginExp.length()>3){
								beginQuery = matchPhraseQuery(beginExp,searchScopes,1);
							}else{
								beginQuery = matchPhraseQuery(beginExp,searchScopes,0);
							}
							
						}    
				}
	    	
	    	
	    	BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().must(beginQuery);

			
	    	for(int i=0;i<operators.size();i++){
	    		
	    		String operator = operators.get(i);
	    		if(operator.equals("+")){
	    			String subExp = subExps.get(i+1);
	    			QueryBuilder subQuery = null;
	    			if(subExp.contains("(")){
	    				 subQuery = builderQuery(subExp.substring(1,subExp.length()-1),searchScopes);
	       			}else{
	       				if(subExp.contains("*")){
//	       					String regex = subExp.replace("*", ".{0,5}");
//	       					subQuery= QueryBuilders.regexpQuery("text", regex);
	       					String regex = subExp.replace("*", " ");
	       					//subQuery= QueryBuilders.matchPhraseQuery("text", regex).slop(5);
	       					subQuery = matchPhraseQuery(regex,searchScopes,5);
	       				}else{
	           				//subQuery =  QueryBuilders.matchPhraseQuery("text", subExp).slop(1);
	       					if(subExp.length()>3){
	       						subQuery = matchPhraseQuery(subExp,searchScopes,1);
							}else{
								subQuery = matchPhraseQuery(subExp,searchScopes,0);
							}
	           				
	       				}     			
	       			}
	    			queryBuilder= QueryBuilders.boolQuery().must(queryBuilder).must(subQuery);   			
	    		}else if(operator.equals("|")){
	    			String subExp = subExps.get(i+1);
	    			QueryBuilder subQuery = null;
	    			if(subExp.contains("(")){
	    				 subQuery = builderQuery(subExp.substring(1,subExp.length()-1),searchScopes);
	       			}else{
	       				if(subExp.contains("*")){
//	       					String regex = subExp.replace("*", ".{0,5}");
//	       					subQuery= QueryBuilders.regexpQuery("text", regex);
	       					String regex = subExp.replace("*", " ");
	       					//subQuery= QueryBuilders.matchPhraseQuery("text", regex).slop(5);
	       					subQuery = matchPhraseQuery(regex,searchScopes,5);
	       				}else{
	           				//subQuery =  QueryBuilders.matchPhraseQuery("text", subExp).slop(1);
	           			
	           				
	           				if(subExp.length()>3){
	       						subQuery = matchPhraseQuery(subExp,searchScopes,1);
							}else{
								subQuery = matchPhraseQuery(subExp,searchScopes,0);
							}
	       				}     	
	       			}
	    			queryBuilder = QueryBuilders.boolQuery().should(queryBuilder).should(subQuery);   			
	    		}else if(operator.equals("-")){
	    			String subExp = subExps.get(i+1);
	    			QueryBuilder subQuery = null;
	    			if(subExp.contains("(")){
	    				 subQuery = builderQuery(subExp.substring(1,subExp.length()-1),searchScopes);
	       			}else{
	       				if(subExp.contains("*")){
//	       					String regex = subExp.replace("*", ".{0,5}");
//	       					subQuery= QueryBuilders.regexpQuery("text", regex);
	       					String regex = subExp.replace("*", " ");
	       					//subQuery= QueryBuilders.matchPhraseQuery("text", regex).slop(5);
	       					subQuery = matchPhraseQuery(regex,searchScopes,5);
	       				}else{
	           				//subQuery =  QueryBuilders.matchPhraseQuery("text", subExp).slop(1);
	       					if(subExp.length()>3){
	       						subQuery = matchPhraseQuery(subExp,searchScopes,1);
							}else{
								subQuery = matchPhraseQuery(subExp,searchScopes,0);
							}
	       				}     	
	       			}
	    			queryBuilder = QueryBuilders.boolQuery().should(queryBuilder).mustNot(subQuery);   			
	    		}
	    		
	    	}
//	    	System.out.println(queryBuilder);
//	    	for(String operator:operators){
//	    		System.out.print(operator+",");
//	    	}
	    	
	    	return queryBuilder;
		}catch(Exception e){
			logger.error("###failed to build query,expression:"+expression);
		}
		return null;
    }
	
	
	public static QueryBuilder matchPhraseQuery(String exp,List<Integer> searchScopes,int slop){
		BoolQueryBuilder query = QueryBuilders.boolQuery();
		if(null==searchScopes){
			query.should(QueryBuilders.matchPhraseQuery("text", exp).slop(slop))
			.should(QueryBuilders.matchPhraseQuery("title", exp).slop(slop))
			.should(QueryBuilders.matchPhraseQuery("name", exp).slop(slop));
		}else{
			for(Integer searchScope:searchScopes){
				if(searchScope==1){
					query.should(QueryBuilders.matchPhraseQuery("title", exp).slop(slop));
				}else if(searchScope==2){
					query.should(QueryBuilders.matchPhraseQuery("text", exp).slop(slop));
				}else if(searchScope==3){
					query.should(QueryBuilders.matchPhraseQuery("name", exp).slop(slop));
				}
			}
		}
		return query;
	}
	
	public static void main(String[] args) {
		String expression;
//		String expression = format("未*(征收|补偿)*数目");
		//System.out.println(expression);
//		 expression="盐田|沙头角|沙头角*(田心|桥东|中英街|东和)|海山街道|海山*(田东|梧桐|鹏湾|海涛)|梅沙|梅沙*(滨海|东海岸|盐田港|三洲田)|深圳*海山|深圳+郭永航)";
//		 expression="黄牛|(衡阳-火车票)";
//		 expression="(茂名|信宜) (食堂|饭堂|饭菜) 老鼠";
		 expression="(茂名|信宜) (食堂|饭堂|饭菜) 老鼠 一中";
//		expression="深圳宝安";
//		 expression="深圳 宝安";
//		 expression="深圳+宝安";
//		 expression="深圳*宝安";
		System.out.println("format:"+format(expression));

		System.out.println(builderQuery(expression,null));
	
		System.out.println("k:"+ExpressionTransform.resolveExpression(expression));
		
	}

}
