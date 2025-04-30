package example.run.rdd.pair.transformations.single;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;

public class KeysWithJavaPairRDDDemo {
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

        JavaRDD<String> keys = pairRDD.keys();

        String keyArrays = Arrays.toString(keys.collect().toArray());
        System.out.println(keyArrays);


        javaSparkContext.stop();

    }
}
