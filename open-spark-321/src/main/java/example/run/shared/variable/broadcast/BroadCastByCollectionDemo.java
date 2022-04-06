package example.run.shared.variable.broadcast;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.util.CollectionAccumulator;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class BroadCastByCollectionDemo {
    public static void main(String[] args) {

        SparkConf sparkConf = new SparkConf()
                .setMaster("local[1]") // 注意:
                .setAppName("firstSpark");

        SparkContext sparkContext = new SparkContext(sparkConf);

        JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);

        HashMap<String, String> codes = new HashMap<String, String>();
        codes.put("spark", "大规模数据处理的统一分析引擎。");
        codes.put("flink", "有状态的分布式流式计算引擎。");
        codes.put("hive", "数据仓库");
        codes.put("zookeeper", "分布式监控服务");
        codes.put("hadoop", "包含hdfs、yarn、mapreduce。");
        codes.put("hdfs", "分布式文件系统");
        codes.put("yarn", "集群资源管理系统");
        codes.put("kafka", "消息中间件");

        // 广播变量:将映射转化为广播变量
        Broadcast<HashMap<String, String>> hashMapBroadcast = javaSparkContext.broadcast(codes);


        ArrayList<String> lines = new ArrayList<>(Arrays.asList(
                "spark hdfs",
                "hadoop",
                "zookeeper",
                "flink kafka"
        ));

        JavaRDD<String> stringJavaRDD = javaSparkContext.parallelize(lines);


        JavaRDD<String> flatMapRDD = stringJavaRDD.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterator<String> call(String s) throws Exception {
                String[] values = s.split(" ");
                return Arrays.stream(values).iterator();
            }
        });

        JavaRDD<Tuple2<String, String>> mapRDD = flatMapRDD.map(new Function<String, Tuple2<String, String>>() {
            @Override
            public Tuple2<String, String> call(String v1) throws Exception {
                // 获取广播变量
                HashMap<String, String> values = hashMapBroadcast.value();
                // 使用广播变量
                String value = values.get(v1);
                if (null != value) {
                    return new Tuple2<>(v1, value);
                } else {
                    return new Tuple2<>(v1, "not found !");
                }
            }
        });

        mapRDD.foreach(new VoidFunction<Tuple2<String, String>>() {
            @Override
            public void call(Tuple2<String, String> value) throws Exception {
                System.out.println(value);
            }
        });

        javaSparkContext.stop();

    }
}
