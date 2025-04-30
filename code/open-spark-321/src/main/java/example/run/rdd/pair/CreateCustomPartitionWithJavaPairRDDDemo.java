package example.run.rdd.pair;

import org.apache.spark.Partitioner;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.Optional;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;

public class CreateCustomPartitionWithJavaPairRDDDemo {
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

        JavaPairRDD<String, Integer> pairRDDWithCustomPartition = pairRDD.partitionBy(new Partitioner() {
            @Override
            public int numPartitions() {
                return 3;
            }

            @Override
            public int getPartition(Object key) {
                String keyString = key.toString();
                if (keyString.length() == 4) {
                    return 0;
                } else if (keyString.length() == 5) {
                    return 1;
                } else {
                    return 2;
                }
            }
        });

        System.out.println(pairRDDWithCustomPartition.getNumPartitions());
        Optional<Partitioner> customPartitioner = pairRDDWithCustomPartition.partitioner();
        if (customPartitioner.isPresent()) {
            System.out.println(customPartitioner.get());
        } else {
            System.out.println("partitioner is Null !");
        }

//        // 获取RDD的partitioner属性。
//        Optional<Partitioner> partitioner = hashPartitionPairRDD.partitioner();
//
//        // 判断是否有值。
//        boolean present = partitioner.isPresent();
//
//        if (present) {
//            System.out.println(partitioner.get());
//        } else {
//            System.out.println("partitioner is Null");
//        }


        javaSparkContext.stop();

    }
}
