package example.run.shared.variable.accumulator;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.util.LongAccumulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class LongAccumulatorByCollectionDemo {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf()
                .setMaster("local[1]") // 注意:
                .setAppName("firstSpark");

        SparkContext sparkContext = new SparkContext(sparkConf);

        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);

        ArrayList<String> lines = new ArrayList<>(Arrays.asList(
                "spark jjj",
                "spark jjj",
                "spark jjj",
                "hadoop",
                "zookeeper",
                "spark jjj",
                "flink jjj",
                "flink"
        ));

        JavaRDD<String> stringJavaRDD = javaSparkContext.parallelize(lines);

        // 累加器：记录一行多个单词的数据
        LongAccumulator notOnlyOneWord = sparkContext.longAccumulator();
        // 初始化累加器
        notOnlyOneWord.setValue(0L);

        JavaRDD<String> flatMapRDD = stringJavaRDD.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(String s) throws Exception {
                String[] values = s.split(" ");
                // 如果一行存在多个单词，就累加器加一
                if (values.length > 1) {
                    notOnlyOneWord.add(1L);
                }
                return Arrays.stream(values).iterator();
            }
        });

        System.out.println(Arrays.toString(flatMapRDD.collect().toArray()));
        System.out.println("一行多个单词的数据有：" + notOnlyOneWord.count());

        javaSparkContext.stop();

    }
}
