package example.run.connector.kafka.sink;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import example.connector.source.StringWithTimeSource;

public class KafkaSinkBase {
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
                                .build()
                )
                .setDeliverGuarantee(DeliveryGuarantee.NONE) // 无
//                .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE) // 至少一次
//                .setDeliverGuarantee(DeliveryGuarantee.EXACTLY_ONCE) //精确一次
                .build();

        sourceString.sinkTo(sink);

        env.execute("Kafka ");
    }

}
