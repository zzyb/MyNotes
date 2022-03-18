package run.connector.custom.sink;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import sink.custom.CustomSink;
import source.StringWithTimeSource;

import java.text.SimpleDateFormat;

public class CustomSinkBase {
    public static void main(String[] args) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String fileName = "sink.txt";

        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment().setParallelism(1);

        DataStreamSource<String> source = env.addSource(new StringWithTimeSource());

        // 将数据发送到自定义数据输出
        source.addSink(new CustomSink(fileName));

        env.execute("Sink ");
    }
}
