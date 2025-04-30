package example.run.actions;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.VoidFunction;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;

public class SparkReduceByCollectionDemo {
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

        Tuple2<String, Integer> reduce = tuple2JavaRDD.reduce(new Function2<Tuple2<String, Integer>, Tuple2<String, Integer>, Tuple2<String, Integer>>() {
            @Override
            public Tuple2<String, Integer> call(Tuple2<String, Integer> value1, Tuple2<String, Integer> value2) throws Exception {
                StringBuffer sb = new StringBuffer();
                String str = sb.append(value1._1).append("_").append(value2._1).toString();
                int sum = value1._2 + value2._2;
                Tuple2<String, Integer> t2 = new Tuple2<String, Integer>(str, sum);
                return t2;
            }
        });

        System.out.println(reduce.toString());

        // 测试输出

        javaSparkContext.stop();

    }
}
