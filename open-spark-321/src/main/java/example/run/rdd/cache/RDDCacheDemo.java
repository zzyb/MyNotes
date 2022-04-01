package example.run.rdd.cache;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.DoubleFlatMapFunction;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.storage.StorageLevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class RDDCacheDemo {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf()
                .setMaster("local[1]") // 注意:
                .setAppName("firstSpark");

        SparkContext sparkContext = new SparkContext(sparkConf);

        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);

        ArrayList<Integer> lines = new ArrayList<Integer>(Arrays.asList(
                1,
                2,
                3,
                4
        ));

        JavaRDD<Integer> stringJavaRDD = javaSparkContext.parallelize(lines);

        JavaRDD<Integer> mapRDD = stringJavaRDD.map(new Function<Integer, Integer>() {
            @Override
            public Integer call(Integer v1) throws Exception {
                return v1 * v1;
            }
        });

        // 持久化
        mapRDD.cache();
//        mapRDD.persist(StorageLevel.MEMORY_AND_DISK_2());

        // count 和 collect 如果没有持久化的代码，都会重新运算
        System.out.println(mapRDD.count());
        System.out.println(
                Arrays.toString(
                        mapRDD.collect().toArray()
                )
        );

        javaSparkContext.stop();

    }
}
