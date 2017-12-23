package com.tyaer.elasticsearch.search;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.index.query.QueryBuilder;

import java.io.File;
import java.io.IOException;

/**
 * Created by Twin on 2017/11/2.
 */
public class DSL_Test {
    public static void main(String[] args) throws IOException {
        String key= FileUtils.readFileToString(new File("keywords.txt"),"utf-8");
        System.out.println(key);
        QueryBuilder queryBuilder = CommonSearcher.builderQuery(key, null);
        System.out.println(queryBuilder);
        String pathname = "DSL.txt";
        FileUtils.writeStringToFile(new File(pathname),queryBuilder.toString(),"utf-8",false);
        System.out.println("转换成功，输出在："+pathname);
    }
}
