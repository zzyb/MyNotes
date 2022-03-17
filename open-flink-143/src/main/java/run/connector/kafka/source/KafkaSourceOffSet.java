package run.connector.kafka.source;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;

import java.text.SimpleDateFormat;
import java.util.HashMap;

public class KafkaSourceOffSet {
    public static void main(String[] args) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment().setParallelism(1);

        HashMap<TopicPartition, Long> startHashMap = new HashMap<>();
        startHashMap.put(new TopicPartition("flink1", 0), 5L);
        startHashMap.put(new TopicPartition("flink1", 1), 5L);
        startHashMap.put(new TopicPartition("flink1", 2), 0L);

        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers("192.168.1.41:9092")
                .setTopics("flink1")
                .setGroupId("group1")
                .setValueOnlyDeserializer(new SimpleStringSchema())
                // 未指定偏移量初始化器，则默认情况下将使用 OffsetsInitializer.early()
                // 未指定偏移量初始化器，则默认情况下将使用 OffsetsInitializer.early()
                // 未指定偏移量初始化器，则默认情况下将使用 OffsetsInitializer.early()
                .setStartingOffsets(OffsetsInitializer.earliest()) // 从最开始消费
//                .setStartingOffsets(OffsetsInitializer.latest()) // 从最新的开始消费
//                .setStartingOffsets(OffsetsInitializer.timestamp(1647488489292L)) // 从给定时间戳之后开始消费
//                .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.NONE)) // 从消费者组提交的偏移量开始消费,没有策略
//                .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.EARLIEST)) // 从消费者组提交的偏移量开始消费,如果不存在提交的偏移量则从 头 开始消费
//                .setStartingOffsets(OffsetsInitializer.committedOffsets(OffsetResetStrategy.LATEST)) // 从消费者组提交的偏移量开始消费,如果不存在提交的偏移量则从 最新的 开始消费
//                .setStartingOffsets(OffsetsInitializer.offsets(startHashMap)) // 设置开始的偏移量
                .build();

        DataStreamSource<String> kafka_source = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");

        kafka_source.print();


        env.execute("Kafka ");
    }

}
