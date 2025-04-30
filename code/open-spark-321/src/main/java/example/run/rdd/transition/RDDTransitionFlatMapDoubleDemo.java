package example.run.rdd.transition;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.PrimitiveIterator;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;

public class RDDTransitionFlatMapDoubleDemo {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf()
                .setMaster("local[1]") // 注意:
                .setAppName("firstSpark");

        SparkContext sparkContext = new SparkContext(sparkConf);

        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);

        ArrayList<String> lines = new ArrayList<String>(Arrays.asList(
                "spark elasticsearch",
                "flink elasticsearch",
                "hadoop hdfs",
                "spark elasticsearch"
        ));

        JavaRDD<String> stringJavaRDD = javaSparkContext.parallelize(lines);

        JavaRDD<Double> flatMapRDD = stringJavaRDD.flatMap(new FlatMapFunction<String, Double>() {
            @Override
            public Iterator<Double> call(String value) throws Exception {
                String[] values = value.split(" ");
                double[] doubles = Arrays.stream(values).mapToDouble(t -> t.length()).toArray();
                return Arrays.stream(doubles).iterator();
            }
        });
        flatMapRDD.foreach(new VoidFunction<Double>() {
            @Override
            public void call(Double aDouble) throws Exception {
                System.out.println(aDouble);
            }
        });

        JavaDoubleRDD javaDoubleRDD = stringJavaRDD.flatMapToDouble(new DoubleFlatMapFunction<String>() {
            @Override
            public Iterator<Double> call(String value) throws Exception {
                String[] values = value.split(" ");
                double[] doubles = Arrays.stream(values).mapToDouble(t -> t.length()).toArray();
                return Arrays.stream(doubles).iterator();
            }
        });

        javaDoubleRDD.foreach(new VoidFunction<Double>() {
            @Override
            public void call(Double aDouble) throws Exception {
                System.out.println(aDouble);
            }
        });

        javaSparkContext.stop();

    }
}
