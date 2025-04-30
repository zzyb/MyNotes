package example.run.transformations;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;

import java.util.ArrayList;
import java.util.Arrays;

public class SparkMaplByCollectionDemo {
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

        // map操作：接收的字符串数据转换为长度
        JavaRDD<Integer> mapRDD = stringJavaRDD.map(new Function<String, Integer>() {
            @Override
            public Integer call(String s) throws Exception {
                return s.length();
            }
        });

        // 测试输出
        mapRDD.foreach(new VoidFunction<Integer>() {
            @Override
            public void call(Integer integer) throws Exception {
                System.out.println(integer);
            }
        });

        javaSparkContext.stop();

    }
}
