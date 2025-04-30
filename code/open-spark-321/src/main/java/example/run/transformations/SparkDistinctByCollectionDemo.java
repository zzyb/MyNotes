package example.run.transformations;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.VoidFunction;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;

public class SparkDistinctByCollectionDemo {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf()
                .setMaster("local[1]") // 注意:
                .setAppName("firstSpark");

        SparkContext sparkContext = new SparkContext(sparkConf);

        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);

        ArrayList<String> lines = new ArrayList<>(Arrays.asList(
                "spark",
                "spark",
                "hadoop",
                "hadoop",
                "flink",
                "flink"
        ));


        JavaRDD<String> stringJavaRDD = javaSparkContext.parallelize(lines);

        // 去重
        JavaRDD<String> distinct = stringJavaRDD.distinct();

        // 测试输出
        distinct.foreach(new VoidFunction<String>() {
            @Override
            public void call(String s) throws Exception {
                System.out.println(s);
            }
        });
//        spark
//        hadoop
//        flink

        javaSparkContext.stop();

    }
}
