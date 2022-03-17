package run.connector.kafka.source;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.common.TopicPartition;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;

/**
 * 方法使用，从网上看是没问题的。
 * 方法使用，从网上看是没问题的。
 * 方法使用，从网上看是没问题的。
 * 当前截止偏移量，总是报错！！！
 * 当前截止偏移量，总是报错！！！
 * 当前截止偏移量，总是报错！！！
 * 暂时搁置
 * 暂时搁置
 * 暂时搁置
 */
public class KafkaSourceBoundednessOffSet {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment().setParallelism(1);

        HashMap<TopicPartition, Long> startHashMap = new HashMap<>();
        startHashMap.put(new TopicPartition("flink1", 0), 5L);
        startHashMap.put(new TopicPartition("flink1", 1), 5L);
        startHashMap.put(new TopicPartition("flink1", 2), 0L);

//        HashMap<TopicPartition, Long> stopHashMap = new HashMap<>();
//        stopHashMap.put(new TopicPartition("flink1", 0), 8L);
//        stopHashMap.put(new TopicPartition("flink1", 1), 8L);
//        stopHashMap.put(new TopicPartition("flink1", 2), 0L);

        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers("192.168.1.41:9092")
                .setTopics("flink1")
                .setGroupId("group2")
                .setValueOnlyDeserializer(new SimpleStringSchema())
                // 未指定偏移量初始化器，则默认情况下将使用 OffsetsInitializer.early()
                // 未指定偏移量初始化器，则默认情况下将使用 OffsetsInitializer.early()
                // 未指定偏移量初始化器，则默认情况下将使用 OffsetsInitializer.early()
                .setStartingOffsets(OffsetsInitializer.offsets(startHashMap)) // 设置从哪里开始
//                .setBounded(OffsetsInitializer.offsets(stopHashMap))
                .setBounded(OffsetsInitializer.latest()) // 设置消费的结尾
                .build();

        DataStreamSource<String> kafka_source = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");


        kafka_source.print();


        env.execute("Kafka ");
    }

}
