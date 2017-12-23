package com.tyaer.util.algorithm.hanming;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Twin on 2017/12/18.
 */
public class SimilarityComputer_Test {

    @Test
    public void t1(){
        double similiarity=0.75;

        List<String> midHanmings=new ArrayList<>();
        midHanmings.add("abc");
        midHanmings.add("efg");
        midHanmings.add("abc");
        List<List<String>> groups = SimilarityComputer.cluster(midHanmings,similiarity,false);
        System.out.println(groups);
    }

}
