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

public class SparkCartesianByCollectionDemo {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf()
                .setMaster("local[1]") // 注意:
                .setAppName("firstSpark");

        SparkContext sparkContext = new SparkContext(sparkConf);

        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);

        ArrayList<String> lines = new ArrayList<>(Arrays.asList(
                "spark",
                "hadoop",
                "flink"
        ));

        ArrayList<String> lines2 = new ArrayList<>(Arrays.asList(
                "123",
                "abc"
        ));

        JavaRDD<String> stringJavaRDD = javaSparkContext.parallelize(lines);
        JavaRDD<String> stringJavaRDD2 = javaSparkContext.parallelize(lines2);


        //  返回所有可能的(a,b)对，a来自第一个RDD，b来自另一个RDD。
        // 注意：输出类型和两个RDD类型不一样：是两个RDD组成的对！！！
        JavaPairRDD<String, String> cartesianRDD = stringJavaRDD.cartesian(stringJavaRDD2);


        // 测试输出
        cartesianRDD.foreach(new VoidFunction<Tuple2<String, String>>() {
            @Override
            public void call(Tuple2<String, String> tuple2Value) throws Exception {
                System.out.println(tuple2Value);
            }
        });

        javaSparkContext.stop();

    }
}
