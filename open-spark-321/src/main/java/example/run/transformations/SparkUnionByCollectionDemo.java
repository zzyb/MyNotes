package example.run.transformations;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;

import java.util.ArrayList;
import java.util.Arrays;

public class SparkUnionByCollectionDemo {
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
        JavaRDD<String> stringJavaRDD2 = javaSparkContext.parallelize(lines);

        // union操作：将两个RDD的数据聚合到一个RDD
        JavaRDD<String> unionRDD = stringJavaRDD.union(stringJavaRDD2);


        // 测试输出
        unionRDD.foreach(new VoidFunction<String>() {
            @Override
            public void call(String value) throws Exception {
                System.out.println(value);
            }
        });

        javaSparkContext.stop();

    }
}
