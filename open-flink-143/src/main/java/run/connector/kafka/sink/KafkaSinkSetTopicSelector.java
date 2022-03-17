package run.connector.kafka.sink;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.sink.TopicSelector;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;
import org.apache.flink.streaming.connectors.kafka.partitioner.FlinkKafkaPartitioner;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema;
import org.apache.kafka.clients.producer.ProducerRecord;
import source.StringWithTimeSource;

public class KafkaSinkSetTopicSelector {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment().setParallelism(1);

        DataStreamSource<String> sourceString = env.addSource(new StringWithTimeSource());

        KafkaSink<String> sink = KafkaSink.<String>builder()
                .setBootstrapServers("192.168.1.41:9092")
                .setRecordSerializer(
                        KafkaRecordSerializationSchema.builder()
//                                .setTopic("new") // 这也是一种TopicSelector，自定义时要注释掉这个
                                // 根据记录将数据发往不同的topic
                                .setTopicSelector(new TopicSelector<String>() {
                                    @Override
                                    public String apply(String s) {
                                        if (s.contains("北京")) {
                                            return "new";
                                        } else {
                                            return "flink1";
                                        }
                                    }
                                })
                                .setValueSerializationSchema(new SimpleStringSchema())
                                .build()
                )
                .setDeliverGuarantee(DeliveryGuarantee.NONE) // 无
                .build();


        sourceString.sinkTo(sink);

        env.execute("Kafka ");
    }

}
