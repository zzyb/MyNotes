package example.run.rdd.transition;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.DoubleFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;

public class RDDTransitionMapDoubleDemo {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf()
                .setMaster("local[1]") // 注意:
                .setAppName("firstSpark");

        SparkContext sparkContext = new SparkContext(sparkConf);

        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);

        ArrayList<String> lines = new ArrayList<String>(Arrays.asList(
                "spark",
                "flink",
                "hadoop",
                "spark"
        ));

        JavaRDD<String> stringJavaRDD = javaSparkContext.parallelize(lines);

        JavaRDD<Double> mapRDD = stringJavaRDD.map(new Function<String, Double>() {
            @Override
            public Double call(String v1) throws Exception {
                return Double.valueOf(v1.length());
            }
        });
        mapRDD.foreach(new VoidFunction<Double>() {
            @Override
            public void call(Double aDouble) throws Exception {
                System.out.println(aDouble);
            }
        });

        JavaDoubleRDD javaDoubleRDD = stringJavaRDD.mapToDouble(new DoubleFunction<String>() {
            @Override
            public double call(String s) throws Exception {
                return s.length();
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
