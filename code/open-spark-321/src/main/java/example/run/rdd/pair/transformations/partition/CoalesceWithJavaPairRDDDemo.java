package example.run.rdd.pair.transformations.partition;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;

public class CoalesceWithJavaPairRDDDemo {
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

        // 通过网络混洗，并创建新的分区集合。
        JavaPairRDD<String, Integer> repartitionRDD = pairRDD.repartition(4);
        // 混洗后的分区数目
        int repartitionSize = repartitionRDD.partitions().size();

        // 通过coalesce缩减分区
        JavaPairRDD<String, Integer> coalesceRDD = repartitionRDD.coalesce(4);
        // 指定更多的分区不会起作用，最多和之前的一样 此处指定为5，返回的还是4
//        JavaPairRDD<String, Integer> coalesceRDD = repartitionRDD.coalesce(5);
        // 缩减后的分区数目
        int coalesceSize = coalesceRDD.partitions().size();


        System.out.println(repartitionSize);
        System.out.println(coalesceSize);

        javaSparkContext.stop();

    }
}
