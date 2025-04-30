package example.run.rdd.pair.transformations.single;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.VoidFunction;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;

public class CombineByKeyWithJavaPairRDDDemo {
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
                new Tuple2<>("spark", 3)
        ));

        JavaPairRDD<String, Integer> pairRDD = javaSparkContext.parallelizePairs(lines);


        // 这里计算每个key的value的平均值
        JavaPairRDD<String, Tuple2<Double, Integer>> combineByKey = pairRDD.combineByKey(
                // value,value累加器 (初次遇到)
                new Function<Integer, Tuple2<Double, Integer>>() {
                    @Override
                    public Tuple2<Double, Integer> call(Integer v1) throws Exception {
                        return new Tuple2<Double, Integer>((double) v1, 1);
                    }
                },
                // value,value累加器 (非第一次遇到)
                new Function2<Tuple2<Double, Integer>, Integer, Tuple2<Double, Integer>>() {
                    @Override
                    public Tuple2<Double, Integer> call(Tuple2<Double, Integer> v1, Integer v2) throws Exception {
                        return new Tuple2<>(v1._1 + (double) v2, v1._2 + 1);
                    }
                },
                // 不同分区累加器合并
                new Function2<Tuple2<Double, Integer>, Tuple2<Double, Integer>, Tuple2<Double, Integer>>() {
                    @Override
                    public Tuple2<Double, Integer> call(Tuple2<Double, Integer> v1, Tuple2<Double, Integer> v2) throws Exception {
                        return new Tuple2<>(v1._1 + v2._1, v1._2 + v2._2);
                    }
                }
        );


        combineByKey.foreach(new VoidFunction<Tuple2<String, Tuple2<Double, Integer>>>() {
            @Override
            public void call(Tuple2<String, Tuple2<Double, Integer>> value) throws Exception {
                System.out.println(value._1 + "----" + value._2._1 / value._2._2);
            }
        });

        javaSparkContext.stop();

    }
}
