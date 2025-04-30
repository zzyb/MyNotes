package example.run.rdd.pair;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.DoubleFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;

public class CreatePairRddByMapDemo {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf()
                .setMaster("local[1]") // 注意:
                .setAppName("firstSpark");

        SparkContext sparkContext = new SparkContext(sparkConf);

        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);

        ArrayList<String> lines = new ArrayList<String>(Arrays.asList(
                "spark",
                "flink",
                "hadoop",
                "spark"
        ));

        JavaRDD<String> stringJavaRDD = javaSparkContext.parallelize(lines);

        JavaRDD<Tuple2<String, Integer>> pairByMapRDD = stringJavaRDD.map(new Function<String, Tuple2<String, Integer>>() {
            @Override
            public Tuple2<String, Integer> call(String v1) throws Exception {
                return new Tuple2<>(v1,1);
            }
        });

        pairByMapRDD.foreach(new VoidFunction<Tuple2<String, Integer>>() {
            @Override
            public void call(Tuple2<String, Integer> value) throws Exception {
                System.out.println(value.toString());
            }
        });


        javaSparkContext.stop();

    }
}
