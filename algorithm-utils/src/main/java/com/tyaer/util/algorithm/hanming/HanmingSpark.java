package com.tyaer.util.algorithm.hanming;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.IDF;
import org.apache.spark.ml.feature.IDFModel;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.mllib.linalg.SparseVector;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by Twin on 2017/12/18.
 */
public class HanmingSpark implements Serializable{
    private static final Logger logger = Logger.getLogger(HanmingSpark.class);
    private static final int numFeatures = 448;//特征向量的维度

    public static void main(String[] args) {
        String path = "file/hanming_topic.txt";
//        computer(path);
    }

    public List<EsArticle> computer(String path) {
        SparkConf sparkConf = new SparkConf().setAppName("ArticleHanmingCodeComputeJob");
        sparkConf.setMaster("local[2]");
        sparkConf.set("spark.streaming.stopGracefullyOnShutdown", "true");
        sparkConf.set("es.index.auto.create", "true");
        // sparkConf.set("es.nodes", "10.248.161.31");
//        sparkConf.set("es.nodes", Config.get(Config.KEY_ES_NODES));
//        sparkConf.set("es.cluster.name", Config.get(Config.KEY_ES_CLUSTER_NAME));

        sparkConf.set("es.nodes.discovery", "true");
        sparkConf.set("es.index.refresh_interval", "30");
        // sparkConf.set("es.batch.size.entries", "1000");
        sparkConf.set("es.write.operation", "upsert");
//        sparkConf.set("es.port", Config.get(Config.KEY_ES_PORT));

//        final JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, new Duration(2000));
//        JavaSparkContext sc = jssc.sc();
        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        final SQLContext sqlContext = new SQLContext(sc);

        logger.info("####count  begin");

        JavaRDD<String> jrdd = sc.textFile(path);

        //并行集合，是通过对于驱动程序中的集合调用JavaSparkContext.parallelize来构建的RDD
//        List<Integer> data = Arrays.asList(1, 2, 3, 4, 5);
//        JavaRDD<Integer> distData = sc.parallelize(data);

        JavaRDD<Row> map = jrdd.map(new Function<String, Row>() {
            @Override
            public Row call(String message) throws Exception {

                String[] fields = message.split("\\|");
                if (fields.length < 2) {
                    return null;
                }
                String mid = fields[0];

                String content = message.substring(message.indexOf("|") + 1);

                content = content.replaceAll("(//@[\\S]+:)|(@[\\S]+\\s)|(\\[花心\\])", "");//format
                content = content.replaceAll("\\s+", "");
                if (content.length() > 1000) {
                    content = content.substring(0, 1000);
                }
                String terms = WordSegmentationUtils.getTerms(content);
                if (StringUtils.isNotBlank(terms)) {
                    Row row = RowFactory.create(mid, terms, null, content);
//                System.out.println(row);
                    return row;
                } else {
                    return null;
                }
            }
        });

        int size = (int) jrdd.count();
        logger.info("####weibo2 size:" + size);
        if (size == 0) {
            return null;
        }
        logger.info("jrdd partition size:" + jrdd.partitions().size());

        StructType schema = new StructType(new StructField[]{
                new StructField("id", DataTypes.StringType, false, org.apache.spark.sql.types.Metadata.empty()),
                new StructField("sentence", DataTypes.StringType, false, org.apache.spark.sql.types.Metadata.empty())//sentence判决
                , new StructField("industry", DataTypes.StringType, false, org.apache.spark.sql.types.Metadata.empty())
                , new StructField("message", DataTypes.StringType, false, org.apache.spark.sql.types.Metadata.empty())
        });
        DataFrame sentenceData = sqlContext.createDataFrame(map, schema);
        //sentenceData.select("*").show();
        Tokenizer tokenizer = new Tokenizer().setInputCol("sentence").setOutputCol("words");
        DataFrame wordsData = tokenizer.transform(sentenceData);
        //wordsData.select("*").show(false);

        HashingTF hashingTF = new HashingTF().setInputCol("words").setOutputCol("rawFeatures")
                .setNumFeatures(numFeatures);
        DataFrame featurizedData = hashingTF.transform(wordsData);

        //featurizedData.select("*").show(false);
        IDF idf = new IDF().setInputCol("rawFeatures").setOutputCol("features");
        IDFModel idfModel = idf.fit(featurizedData);
        DataFrame rescaledData = idfModel.transform(featurizedData);
        //rescaledData.select("*").show(false);


        JavaRDD<Row> rddRows = rescaledData.select("features", "id").toJavaRDD();

        JavaRDD<EsArticle> rddHanmings = rddRows.map(new Function<Row, EsArticle>() {
            public EsArticle call(Row r) {
                try {
                    String id = r.getString(1);

                    String hanmingCode = getHanmingCode(r);

//                    logger.info("id:"+id+",hanmingCode:"+Long.toUnsignedString(hanmin, 16));
                    return new EsArticle(id, hanmingCode);
                } catch (Exception e) {
                    logger.info(e);

                    return null;
                }


            }
        });

        rddHanmings.foreach(new VoidFunction<EsArticle>() {
            @Override
            public void call(EsArticle esArticle) throws Exception {
                System.out.println(esArticle);
            }
        });

        List<EsArticle> esArticles = rddHanmings.toArray();
        return esArticles;
    }

    public static String getHanmingCode(Row row) throws Exception {
        SparseVector feature = row.getAs(0);

        Field field = SparseVector.class.getDeclaredField("indices");
        field.setAccessible(true);
        int[] indices = (int[]) field.get(feature);

        double[] vals = new double[numFeatures];
        for (int indice : indices) {
            vals[indice] = 1;
        }


        byte[] hanmin = new byte[numFeatures / 8];
        byte b = 0;
        int k = 0;
        for (double v : vals) {
            int sim = 0;
            if (v - 0 > 0.001) {
                sim = 1;
            }
            k++;

            b = (byte) ((b << 1) + sim);
            if (k % 8 == 0) {
                hanmin[k / 8 - 1] = b;

                b = 0;
            }
        }

        String hanmingCode = HanmingCode.encode(hanmin);//将汉明码编码为汉字，以节省空间。
        return hanmingCode;
    }
}
