package run.connector.kafka.sink;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.partitioner.FlinkFixedPartitioner;
import connector.source.StringWithTimeSource;

public class KafkaSinkFixedPartitioner {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment().setParallelism(1);


        DataStreamSource<String> sourceString = env.addSource(new StringWithTimeSource());


        KafkaSink<String> sink = KafkaSink.<String>builder()
                .setBootstrapServers("192.168.1.41:9092")
                .setRecordSerializer(
                        KafkaRecordSerializationSchema.builder()
                                .setTopic("new")
                                .setValueSerializationSchema(new SimpleStringSchema())
                                .setPartitioner(new FlinkFixedPartitioner<>()) //保每个内部 Flink 分区最终都在一个 Kafka 分区中。 注意，一个 Kafka 分区可以包含多个 Flink 分区。
                                .build()
                )
                .setDeliverGuarantee(DeliveryGuarantee.NONE) // 无
                .build();



        sourceString.sinkTo(sink);

        env.execute("Kafka ");
    }

}
