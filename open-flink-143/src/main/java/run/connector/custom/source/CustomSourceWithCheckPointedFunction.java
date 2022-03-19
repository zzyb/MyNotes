package run.connector.custom.source;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import connector.source.custom.StringWithTimeCheckPointedCustomSource;

import java.text.SimpleDateFormat;

public class CustomSourceWithCheckPointedFunction {
    public static void main(String[] args) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment().setParallelism(1);

        DataStreamSource<String> sourceWithCheckPointed = env.addSource(new StringWithTimeCheckPointedCustomSource());

        sourceWithCheckPointed.print();


        env.execute("Kafka ");
    }
}
