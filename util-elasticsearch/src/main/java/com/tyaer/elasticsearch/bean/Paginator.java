package com.tyaer.elasticsearch.bean;

import org.elasticsearch.search.sort.SortOrder;

import java.io.Serializable;

public class Paginator implements Serializable{
    private int pageNO; // 页码.
    private int from=0; // 从第n行查询.
    private int size=20; //每页显示条数.
    private String field;
    private SortOrder sortOrder;

    public Paginator() {}
    public int getPageNO() {
        return pageNO;
    }
    public void setPageNO(int pageNO) {this.pageNO = pageNO;}

    public int getFrom() {
    	if(from==0){
    		return pageNO>0?size*(pageNO-1):0;
    	}
    	return from;
    }
    public void setFrom(int from) {
        this.from = from;
    }

    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }

    public String getField() {return field;}

    public SortOrder getSortOrder() {return sortOrder;}

    // 增加排序功能.
    public Paginator addSort(String field,SortOrder sortOrder){
        this.field = field;
        this.sortOrder = sortOrder;
        return this;
    }
	@Override
	public String toString() {
		return "Paginator [pageNO=" + pageNO + ", from=" + from + ", size="
				+ size + ", field=" + field + ", sortOrder=" + sortOrder + "]";
	}
    
    
}