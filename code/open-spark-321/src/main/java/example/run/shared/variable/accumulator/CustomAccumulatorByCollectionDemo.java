package example.run.shared.variable.accumulator;

import example.operator.shared.variable.CustomAccumulatorGetMaxInteger;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.util.LongAccumulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class CustomAccumulatorByCollectionDemo {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf()
                .setMaster("local[1]") // 注意:
                .setAppName("firstSpark");

        SparkContext sparkContext = new SparkContext(sparkConf);

        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);

        ArrayList<Integer> lines = new ArrayList<>(Arrays.asList(
                12,
                33,
                2,
                98,
                37
        ));

        JavaRDD<Integer> integerJavaRDD = javaSparkContext.parallelize(lines);

        // 创建自定义累加器对象
        CustomAccumulatorGetMaxInteger customAccumulatorGetMaxInteger = new CustomAccumulatorGetMaxInteger();

        // 注册自定义累加器
        sparkContext.register(customAccumulatorGetMaxInteger, "getMax");

        // 初始化自定义累加器。
        customAccumulatorGetMaxInteger.reset();

        JavaRDD<Integer> mapRDD = integerJavaRDD.map(new Function<Integer, Integer>() {
            @Override
            public Integer call(Integer v1) throws Exception {
                // 向累加器添加值
                customAccumulatorGetMaxInteger.add(v1);
                return v1;
            }
        });


        mapRDD.foreach(new VoidFunction<Integer>() {
            @Override
            public void call(Integer integer) throws Exception {
                System.out.println(integer);
            }
        });

        // 注意：要在行动算子之后
        System.out.println(" 累加的最大值是：" + customAccumulatorGetMaxInteger.value());

        javaSparkContext.stop();

    }
}
