package example.run.transformations;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.VoidFunction;

import java.util.ArrayList;
import java.util.Arrays;

public class SparkIntersectionByCollectionDemo {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf()
                .setMaster("local[1]") // 注意:
                .setAppName("firstSpark");

        SparkContext sparkContext = new SparkContext(sparkConf);

        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);

        ArrayList<String> lines = new ArrayList<>(Arrays.asList(
                "spark",
                "hadoop",
                "hadoop",
                "flink"
        ));

        ArrayList<String> lines2 = new ArrayList<>(Arrays.asList(
                "hadoop",
                "flink"
        ));

        JavaRDD<String> stringJavaRDD = javaSparkContext.parallelize(lines);
        JavaRDD<String> stringJavaRDD2 = javaSparkContext.parallelize(lines2);

        // intersection操作：返回两个RDD都有的元素，同时去掉重复元素。（单个RDD中的重复元素也会被移除）
        JavaRDD<String> intersectionRDD = stringJavaRDD.intersection(stringJavaRDD2);


        // 测试输出
        intersectionRDD.foreach(new VoidFunction<String>() {
            @Override
            public void call(String value) throws Exception {
                System.out.println(value);
            }
        });

        javaSparkContext.stop();

    }
}
