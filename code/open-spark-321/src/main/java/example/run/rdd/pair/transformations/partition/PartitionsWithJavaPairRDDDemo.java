package example.run.rdd.pair.transformations.partition;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;

public class PartitionsWithJavaPairRDDDemo {
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

        // 获取当前分区
        int partitionSize = pairRDD.partitions().size();

        // 通过网络混洗，并创建新的分区集合。
        JavaPairRDD<String, Integer> repartitionRDD = pairRDD.repartition(2);

        // 混洗后的分区集合
        int repartitionSize = repartitionRDD.partitions().size();

        System.out.println(partitionSize);
        System.out.println(repartitionSize);

        javaSparkContext.stop();

    }
}
