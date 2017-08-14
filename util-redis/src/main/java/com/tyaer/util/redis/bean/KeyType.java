package com.tyaer.util.redis.bean;

/**
 * Created by Twin on 2017/3/24.
 */
public enum KeyType {
    none("key不存在"),
    string("字符串"),
    list("列表"),
    set("集合"),
    zset("有序集"),
    hash("哈希表");

    private String type;

    KeyType(String type) {
        this.type = type;
    }

    public static void main(String[] args) {
        
    }
}
