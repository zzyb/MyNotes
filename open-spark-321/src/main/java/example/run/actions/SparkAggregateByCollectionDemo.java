package example.run.actions;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function2;
import scala.Tuple2;
import scala.Tuple3;

import java.util.ArrayList;
import java.util.Arrays;

public class SparkAggregateByCollectionDemo {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf()
                .setMaster("local[1]") // 注意:
                .setAppName("firstSpark");

        SparkContext sparkContext = new SparkContext(sparkConf);

        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);

        ArrayList<Tuple2<String, Integer>> lines = new ArrayList<Tuple2<String, Integer>>(Arrays.asList(
                new Tuple2<>("spark", 1),
                new Tuple2<>("flink", 2),
                new Tuple2<>("hadoop", 3),
                new Tuple2<>("spark", 4)
        ));

        JavaRDD<Tuple2<String, Integer>> tuple2JavaRDD = javaSparkContext.parallelize(lines);

        // 创建一个初始状态
        Tuple3<String, Integer,Integer> beginState = new Tuple3<>("", 0,0);

        Tuple3<String, Integer, Integer> aggregate = tuple2JavaRDD.aggregate(
                beginState,
                new Function2<Tuple3<String, Integer, Integer>, Tuple2<String, Integer>, Tuple3<String, Integer, Integer>>() {
                    @Override
                    public Tuple3<String, Integer, Integer> call(Tuple3<String, Integer, Integer> v1, Tuple2<String, Integer> v2) throws Exception {
                        String str = v1._1() + v2._1;
                        int sum = v1._2() + v2._2();
                        int count = v1._3() + 1;
                        return new Tuple3<>(str,sum,count);
                    }
                },
                new Function2<Tuple3<String, Integer, Integer>, Tuple3<String, Integer, Integer>, Tuple3<String, Integer, Integer>>() {
                    @Override
                    public Tuple3<String, Integer, Integer> call(Tuple3<String, Integer, Integer> v1, Tuple3<String, Integer, Integer> v2) throws Exception {
                        return new Tuple3<>(v1._1()+v2._1(),v1._2()+ v2._2(),v1._3()+v2._3());
                    }
                }
        );

        System.out.println(aggregate._1());
        System.out.println(Double.valueOf(aggregate._2())/aggregate._3());


        javaSparkContext.stop();

    }
}
