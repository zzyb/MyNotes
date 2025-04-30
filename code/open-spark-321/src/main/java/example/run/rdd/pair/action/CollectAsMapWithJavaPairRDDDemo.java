package example.run.rdd.pair.action;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class CollectAsMapWithJavaPairRDDDemo {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf()
                .setMaster("local[1]") // 注意:
                .setAppName("firstSpark");

        SparkContext sparkContext = new SparkContext(sparkConf);

        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);

        ArrayList<Tuple2<String, Integer>> lines = new ArrayList<Tuple2<String, Integer>>(Arrays.asList(
                new Tuple2<>("spark", 1),
                new Tuple2<>("flink", 1),
                new Tuple2<>("hadoop", 1),
                new Tuple2<>("spark", 3)
        ));

        JavaPairRDD<String, Integer> pairRDD = javaSparkContext.parallelizePairs(lines);

        Map<String, Integer> collectAsMap = pairRDD.collectAsMap();

        Set<Map.Entry<String, Integer>> entries = collectAsMap.entrySet();
        for (Map.Entry<String, Integer> entry : entries) {
            System.out.println(entry.getKey() + "----" + entry.getValue());
        }

        javaSparkContext.stop();

    }
}
