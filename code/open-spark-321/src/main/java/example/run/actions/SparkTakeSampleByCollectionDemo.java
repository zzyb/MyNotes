package example.run.actions;

import example.operator.transformations.TopComparator;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SparkTakeSampleByCollectionDemo {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf()
                .setMaster("local[1]") // 注意:
                .setAppName("firstSpark");

        SparkContext sparkContext = new SparkContext(sparkConf);

        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);

        ArrayList<String> lines = new ArrayList<>(Arrays.asList(
                "1spark jjj",
                "2spark jjjj",
                "3spark jjjjj",
                "4spark jjjjjj",
                "5flink jjjjjjj",
                "6flink"
        ));

        JavaRDD<String> stringJavaRDD = javaSparkContext.parallelize(lines);

        List<String> takeSampleValues = stringJavaRDD.takeSample(false, 2);

        for (String value : takeSampleValues) {
            System.out.println(value);
        }

        javaSparkContext.stop();

    }
}
