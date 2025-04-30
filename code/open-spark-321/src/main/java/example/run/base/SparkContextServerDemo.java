package example.run.base;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;

import java.util.ArrayList;
import java.util.Arrays;

public class SparkContextServerDemo {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf()
                .setMaster("spark://k8s-node09:7077") // 注意，不是webUI的地址。
                .setAppName("firstSpark")
                // 提交到远程Spark运行：远程运行是通过运行jar的形式，可以先使用mvn打包，然后设置对应的jar。
                .setJars(new String[]{"D:\\zyb\\MyNotes\\open-spark-321\\target\\open-spark-321-1.0.jar"});

        SparkContext sparkContext = new SparkContext(sparkConf);

        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);

        ArrayList<String> strings = new ArrayList<>(Arrays.asList(
                "spark jjj",
                "spark jjj",
                "spark jjj",
                "spark jjj",
                "flink jjj",
                "flink"
        ));

        JavaRDD<String> stringJavaRDD = javaSparkContext.parallelize(strings);

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
