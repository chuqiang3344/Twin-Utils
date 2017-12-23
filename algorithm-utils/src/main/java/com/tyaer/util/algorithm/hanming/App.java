package com.tyaer.util.algorithm.hanming;

import java.util.List;

/**
 * Created by Twin on 2017/12/20.
 */
public class App {

    public static void main(String[] args) {
        HanmingSpark hanmingSpark = new HanmingSpark();
        SimilarityComputer similarityComputer = new SimilarityComputer();
        String path = "file/hanming_topic.txt";
        List<EsArticle> esArticles = hanmingSpark.computer(path);
        System.out.println(esArticles);


        System.out.println(similarityComputer.cluster1(esArticles,0.75,true));
    }

}
