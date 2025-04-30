package example.run.transformations;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.VoidFunction;

import java.util.ArrayList;
import java.util.Arrays;

public class SparkSubtractByCollectionDemo {
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

        ArrayList<String> lines2 = new ArrayList<>(Arrays.asList(
                "hadoop",
                "flink"
        ));

        JavaRDD<String> stringJavaRDD = javaSparkContext.parallelize(lines);
        JavaRDD<String> stringJavaRDD2 = javaSparkContext.parallelize(lines2);

        // 返回只存在于第一个RDD而不存在于第二个RDD的所有元素组成的RDD。（不去重！！！）
        JavaRDD<String> subtractRDD = stringJavaRDD.subtract(stringJavaRDD2);


        // 测试输出
        subtractRDD.foreach(new VoidFunction<String>() {
            @Override
            public void call(String value) throws Exception {
                System.out.println(value);
            }
        });

        javaSparkContext.stop();

    }
}
