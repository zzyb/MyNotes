package example.run.rdd.pair;

import org.apache.spark.HashPartitioner;
import org.apache.spark.Partitioner;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.Optional;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;

public class GetPartitionAfterPartitionByWithJavaPairRDDDemo {
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

        // 对RDD进行PartitionBy分区操作！！！
        // 通常与持久化一起使用，否则后续RDD会一遍又一遍的对pairs进行哈希分区操作。
        JavaPairRDD<String, Integer> hashPartitionPairRDD = pairRDD.partitionBy(new HashPartitioner(3));

        // 获取RDD的partitioner属性。
        Optional<Partitioner> partitioner = hashPartitionPairRDD.partitioner();

        // 判断是否有值。
        boolean present = partitioner.isPresent();

        if (present) {
            System.out.println(partitioner.get());
        } else {
            System.out.println("partitioner is Null");
        }


        javaSparkContext.stop();

    }
}
