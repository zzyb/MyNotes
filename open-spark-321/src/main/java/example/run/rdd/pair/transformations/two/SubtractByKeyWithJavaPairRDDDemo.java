package example.run.rdd.pair.transformations.two;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.VoidFunction;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;

public class SubtractByKeyWithJavaPairRDDDemo {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf()
                .setMaster("local[1]") // 注意:
                .setAppName("firstSpark");

        SparkContext sparkContext = new SparkContext(sparkConf);

        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);

        ArrayList<Tuple2<String, Integer>> first = new ArrayList<Tuple2<String, Integer>>(Arrays.asList(
                new Tuple2<>("spark", 1),
                new Tuple2<>("zookeeper", 1),
                new Tuple2<>("flink", 1),
                new Tuple2<>("apache", 1),
                new Tuple2<>("hadoop", 1)
        ));

        ArrayList<Tuple2<String, Integer>> other = new ArrayList<Tuple2<String, Integer>>(Arrays.asList(
                new Tuple2<>("spark", 1),
                new Tuple2<>("flink", 1)
        ));

        JavaPairRDD<String, Integer> firstRDD = javaSparkContext.parallelizePairs(first);
        JavaPairRDD<String, Integer> otherRDD = javaSparkContext.parallelizePairs(other);

        JavaPairRDD<String, Integer> subtractByKey = firstRDD.subtractByKey(otherRDD);

        subtractByKey.foreach(new VoidFunction<Tuple2<String, Integer>>() {
            @Override
            public void call(Tuple2<String, Integer> value) throws Exception {
                System.out.println(value);
            }
        });

        javaSparkContext.stop();

    }
}
