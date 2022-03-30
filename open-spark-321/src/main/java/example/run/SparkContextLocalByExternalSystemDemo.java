package example.run;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;

import java.util.ArrayList;
import java.util.Arrays;

public class SparkContextLocalByExternalSystemDemo {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf()
                .setMaster("local[1]") // 注意:
                .setAppName("firstSpark");

        SparkContext sparkContext = new SparkContext(sparkConf);

        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);

        JavaRDD<String> stringJavaRDD = javaSparkContext.textFile("C:\\Users\\Lenovo\\Desktop\\spark.txt");

        stringJavaRDD.map(new Function<String, Integer>() {
            @Override
            public Integer call(String s) throws Exception {
                return s.length();
            }
        }).foreach(new VoidFunction<Integer>() {
            @Override
            public void call(Integer integer) throws Exception {
                System.out.println(integer);
            }
        });

        javaSparkContext.stop();

    }
}
