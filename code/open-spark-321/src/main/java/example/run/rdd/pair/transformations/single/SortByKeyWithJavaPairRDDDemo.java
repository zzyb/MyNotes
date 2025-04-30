package example.run.rdd.pair.transformations.single;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.VoidFunction;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;

public class SortByKeyWithJavaPairRDDDemo {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf()
                .setMaster("local[1]") // 注意:
                .setAppName("firstSpark");

        SparkContext sparkContext = new SparkContext(sparkConf);

        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);

        ArrayList<Tuple2<String, Integer>> lines = new ArrayList<Tuple2<String, Integer>>(Arrays.asList(
                new Tuple2<>("spark", 1),
                new Tuple2<>("zookeeper", 1),
                new Tuple2<>("flink", 1),
                new Tuple2<>("apache", 1),
                new Tuple2<>("hadoop", 1)
        ));

        JavaPairRDD<String, Integer> pairRDD = javaSparkContext.parallelizePairs(lines);

        JavaPairRDD<String, Integer> sortByKey = pairRDD.sortByKey();

        sortByKey.foreach(new VoidFunction<Tuple2<String, Integer>>() {
            @Override
            public void call(Tuple2<String, Integer> value) throws Exception {
                System.out.println(value);
            }
        });

        javaSparkContext.stop();

    }
}
