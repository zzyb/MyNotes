package example.run.shared.variable.accumulator;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.util.LongAccumulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class CustomAccumulatorByCollectionDemo {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf()
                .setMaster("local[1]") // 注意:
                .setAppName("firstSpark");

        SparkContext sparkContext = new SparkContext(sparkConf);

        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);

        ArrayList<Integer> lines = new ArrayList<>(Arrays.asList(
                12,
                33,
                2,
                98,
                37
        ));

        JavaRDD<Integer> integerJavaRDD = javaSparkContext.parallelize(lines);




        javaSparkContext.stop();

    }
}
