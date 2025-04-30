package example.run.actions;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;

public class SparkFoldByCollectionDemo {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf()
                .setMaster("local[1]") // 注意:
                .setAppName("firstSpark");

        SparkContext sparkContext = new SparkContext(sparkConf);

        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);

        ArrayList<Tuple2<String, Integer>> lines = new ArrayList<Tuple2<String, Integer>>(Arrays.asList(
                new Tuple2<>("spark", 1),
                new Tuple2<>("flink", 1),
                new Tuple2<>("hadoop", 1),
                new Tuple2<>("spark", 1)
        ));

        JavaRDD<Tuple2<String, Integer>> tuple2JavaRDD = javaSparkContext.parallelize(lines);

        // 创建一个初始状态
        Tuple2<String, Integer> beginState = new Tuple2<>("", 0);
        // fold参数：初始值，函数
        Tuple2<String, Integer> fold = tuple2JavaRDD.fold(beginState, new Function2<Tuple2<String, Integer>, Tuple2<String, Integer>, Tuple2<String, Integer>>() {
            @Override
            public Tuple2<String, Integer> call(Tuple2<String, Integer> first, Tuple2<String, Integer> second) throws Exception {
                String str = new StringBuffer().append(first._1).append(second._1).toString();
                int sum = first._2 + second._2;
                Tuple2<String, Integer> t2result = new Tuple2<>(str, sum);
                return t2result;
            }
        });

        // 测试输出
        System.out.println(fold.toString());


        javaSparkContext.stop();

    }
}
