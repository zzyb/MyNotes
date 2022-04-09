package example.run.shared.variable.partition;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class mapPartitionsByCollectionDemo {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf()
                .setMaster("local[1]") // 注意:
                .setAppName("firstSpark");

        SparkContext sparkContext = new SparkContext(sparkConf);

        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);

        ArrayList<Integer> lines = new ArrayList<>(Arrays.asList(
                10,
                10,
                10,
                10,
                15
        ));

        JavaRDD<Integer> integerJavaRDD = javaSparkContext.parallelize(lines);

        /**
         * 使用map求平均数。
         * 这里会将每个值，转为为一个二元组Tuple2
         */
//        Tuple2<Integer, Integer> map = integerJavaRDD
//                .map(new Function<Integer, Tuple2<Integer, Integer>>() {
//                    @Override
//                    public Tuple2<Integer, Integer> call(Integer v1) throws Exception {
//                        return new Tuple2<>(v1, 1);
//                    }
//                }).reduce(new Function2<Tuple2<Integer, Integer>, Tuple2<Integer, Integer>, Tuple2<Integer, Integer>>() {
//                    @Override
//                    public Tuple2<Integer, Integer> call(Tuple2<Integer, Integer> v1, Tuple2<Integer, Integer> v2) throws Exception {
//                        return new Tuple2<>(v1._1 + v2._1, v1._2 + v2._2);
//                    }
//                });
//
//        System.out.println("map: " + (double) map._1 / map._2);


        /**
         * 使用mapPartitions来求平均数。
         * 这里每个分区只需要一个二元组Tuple2
         */
        JavaRDD<Tuple2<Integer, Integer>> mapPartitions = integerJavaRDD.mapPartitions(new FlatMapFunction<Iterator<Integer>, Tuple2<Integer, Integer>>() {
            @Override
            public Iterator<Tuple2<Integer, Integer>> call(Iterator<Integer> integerIterator) throws Exception {
                int sum = 0;
                int count = 0;
                while (integerIterator.hasNext()) {
                    sum = sum + integerIterator.next();
                    count = count + 1;
                }
                ArrayList<Tuple2<Integer, Integer>> resultArrayList = new ArrayList<>(
                        Arrays.asList(
                                new Tuple2<>(sum, count)
                        )
                );

                return resultArrayList.iterator();
            }
        });

        Tuple2<Integer, Integer> reduce = mapPartitions.reduce(new Function2<Tuple2<Integer, Integer>, Tuple2<Integer, Integer>, Tuple2<Integer, Integer>>() {
            @Override
            public Tuple2<Integer, Integer> call(Tuple2<Integer, Integer> v1, Tuple2<Integer, Integer> v2) throws Exception {
                return new Tuple2<Integer, Integer>(v1._1 + v2._1, v1._2 + v2._2);
            }
        });

        System.out.println("mapPartitions: " + (double) reduce._1 / reduce._2);


        javaSparkContext.stop();

    }
}
