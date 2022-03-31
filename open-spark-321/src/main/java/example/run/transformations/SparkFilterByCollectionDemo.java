package example.run.transformations;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class SparkFilterByCollectionDemo {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf()
                .setMaster("local[1]") // 注意:
                .setAppName("firstSpark");

        SparkContext sparkContext = new SparkContext(sparkConf);

        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);

        ArrayList<String> lines = new ArrayList<>(Arrays.asList(
                "spark jjj",
                "spark jjj",
                "spark jjj",
                "spark jjj",
                "flink jjj",
                "flink"
        ));

        JavaRDD<String> stringJavaRDD = javaSparkContext.parallelize(lines);

        // filter操作：接收字符串数据，如果包含flink则返回。
        JavaRDD<String> filterRDD = stringJavaRDD.filter(new Function<String, Boolean>() {
            @Override
            public Boolean call(String s) throws Exception {
                return s.contains("flink");
            }
        });


        // 测试输出
        filterRDD.foreach(new VoidFunction<String>() {
            @Override
            public void call(String value) throws Exception {
                System.out.println(value);
            }
        });

        javaSparkContext.stop();

    }
}
