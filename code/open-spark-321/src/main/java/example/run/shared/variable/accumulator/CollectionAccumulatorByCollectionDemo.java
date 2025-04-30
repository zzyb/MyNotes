package example.run.shared.variable.accumulator;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.util.CollectionAccumulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class CollectionAccumulatorByCollectionDemo {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf()
                .setMaster("local[1]") // 注意:
                .setAppName("firstSpark");

        SparkContext sparkContext = new SparkContext(sparkConf);

        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);

        ArrayList<String> lines = new ArrayList<>(Arrays.asList(
                "spark aaa",
                "spark bbb",
                "spark ccc",
                "hadoop",
                "zookeeper",
                "spark ddd",
                "flink jjj",
                "flink"
        ));

        JavaRDD<String> stringJavaRDD = javaSparkContext.parallelize(lines);

        // 累加器：记录包含spark的数据
        CollectionAccumulator<Object> hasSparkAccumulator = sparkContext.collectionAccumulator();
        // 初始化累加器
        hasSparkAccumulator.setValue(new ArrayList<>());

        JavaRDD<String> flatMapRDD = stringJavaRDD.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(String s) throws Exception {
                // 向集合累加器中添加元素
                if (s.contains("spark")) {
                    hasSparkAccumulator.add(s);
                }
                String[] values = s.split(" ");
                return Arrays.stream(values).iterator();
            }
        });

        System.out.println(Arrays.toString(flatMapRDD.collect().toArray()));
        System.out.println("包含spark的数据为：" + Arrays.toString(hasSparkAccumulator.value().toArray()));

        javaSparkContext.stop();

    }
}
