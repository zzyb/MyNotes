package example.run.connector.kafka.sink;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.partitioner.FlinkKafkaPartitioner;
import example.connector.source.StringWithTimeSource;

public class KafkaSinkCustomPartitioner {
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
                                // 自定义分区，如果record包含北京发往分区1，其他的发往分区0
                                // 自定义分区，如果record包含北京发往分区1，其他的发往分区0
                                // 自定义分区，如果record包含北京发往分区1，其他的发往分区0
                                .setPartitioner(new FlinkKafkaPartitioner<String>() {
                                    @Override
                                    public int partition(String record, byte[] key, byte[] value, String targetTopic, int[] partitions) {
                                        if (!record.contains("北京")) {
                                            return 0;
                                        } else {
                                            return 1;
                                        }
                                    }
                                })
                                .build()
                )
                .setDeliverGuarantee(DeliveryGuarantee.NONE) // 无
                .build();


        sourceString.sinkTo(sink);

        env.execute("Kafka ");
    }

}
