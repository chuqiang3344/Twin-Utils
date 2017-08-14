package com.tyaer.elasticsearch.bean;

/**
 * @Author guohongdou.
 * @Date 16/8/18.
 * @Version 0.0.1.
 * @Desc <p>VMSES es常量定义</p>.
 * @Update 16/8/18.
 */
public enum ESConstant {
    //es 集群名称.
    CLUSTER_NAME {
        public String toString(){ return "izhonghong";}
    }, 
    INDEX_AMS {
        public String toString(){ return "ams_data";}
    },

    WEIBO_TYPE {
        public String toString(){ return "t_status_weibo";}
    },
    ARTICLE_TYPE {
        public String toString(){ return "t_article";}
    }
    ;
    
    
}
