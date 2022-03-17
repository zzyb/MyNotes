package run.connector.kafka.source;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.text.SimpleDateFormat;
import java.time.Duration;

public class KafkaSourceWatermarks {
    public static void main(String[] args) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment().setParallelism(1);


        KafkaSource<String> source = KafkaSource.<String>builder()
                .setBootstrapServers("192.168.1.41:9092")
                .setTopics("flink1")
                .setGroupId("group1")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

//        WatermarkStrategy<String> zeroWaterMarks = WatermarkStrategy.<String>forBoundedOutOfOrderness(Duration.ZERO);
        WatermarkStrategy<String> zeroWaterMarksWithIdleness = WatermarkStrategy.<String>forBoundedOutOfOrderness(Duration.ZERO).withIdleness(Duration.ofMinutes(1L));

        DataStreamSource<String> kafka_source = env.fromSource(
                source,
                zeroWaterMarksWithIdleness,
                "Kafka Source"
        );

        kafka_source.print();


        env.execute("Kafka ");
    }

}
