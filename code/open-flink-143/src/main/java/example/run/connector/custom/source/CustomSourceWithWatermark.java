package example.run.connector.custom.source;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import example.connector.source.custom.StringWithTimeAndWatermark;

import java.text.SimpleDateFormat;

public class CustomSourceWithWatermark {
    public static void main(String[] args) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment().setParallelism(1);

        DataStreamSource<String> sourceWithCheckPointed = env.addSource(new StringWithTimeAndWatermark());

        sourceWithCheckPointed.print();


        env.execute("Kafka ");
    }
}
