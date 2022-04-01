package example.run.actions;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class SparkCountByValueByCollectionDemo {
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
                new Tuple2<>("spark", 1)
        ));

        JavaRDD<Tuple2<String, Integer>> tuple2JavaRDD = javaSparkContext.parallelize(lines);


        Map<Tuple2<String, Integer>, Long> tuple2LongMap = tuple2JavaRDD.countByValue();

        Set<Map.Entry<Tuple2<String, Integer>, Long>> entries = tuple2LongMap.entrySet();

        for (Map.Entry<Tuple2<String, Integer>, Long> entry : entries) {
            System.out.println(entry.getKey() + "----------" + entry.getValue());
        }

        javaSparkContext.stop();

    }
}
