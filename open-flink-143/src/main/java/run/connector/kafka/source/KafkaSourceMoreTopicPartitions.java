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
import java.util.HashSet;

public class KafkaSourceMoreTopicPartitions {
    public static void main(String[] args) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment().setParallelism(1);


        // 设置 HashSet 从哪里读取
        HashSet<TopicPartition> topicPartitions = new HashSet<>();
        topicPartitions.addAll(Arrays.asList(
                new TopicPartition("new", 0), // new的第1个分区
                new TopicPartition("flink1", 1) // flink1的第2个分区
        ));

        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers("192.168.1.41:9092")
//                .setTopics("new", "flink1")
                .setPartitions(topicPartitions)  // 此处传入topic和分区,注意：不是topics方法，是Partitions方法
                .setGroupId("group1")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        DataStreamSource<String> kafka_source = env.fromSource(source, WatermarkStrategy.noWatermarks(), "Kafka Source");

        kafka_source.print();


        env.execute("Kafka ");
    }

}
