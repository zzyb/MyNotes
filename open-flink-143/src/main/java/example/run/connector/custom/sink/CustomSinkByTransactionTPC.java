package example.run.connector.custom.sink;

import example.connector.sink.custom.CustomTwoPhaseCommitSinkUseFile;
import example.connector.source.Tuple2TwoStringWithTimeSource;
import org.apache.flink.api.common.typeutils.base.StringSerializer;
import org.apache.flink.api.common.typeutils.base.VoidSerializer;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class CustomSinkByTransactionTPC {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment().setParallelism(1);

//        DataStreamSource<Tuple2<String, String>> source = env.fromCollection(new ArrayList<Tuple2<String, String>>(Arrays.asList(
//                new Tuple2<String, String>("郑州", "购买--2022-01-01 00:00:00"),
//                new Tuple2<String, String>("上海", "购买--2022-01-01 00:00:00"),
//                new Tuple2<String, String>("郑州", "购买--2022-01-01 00:00:20"),
//                new Tuple2<String, String>("南京", "购买--2022-01-01 00:00:20"),
//                new Tuple2<String, String>("上海", "购买--2022-01-01 00:00:20")
//        )));

        env.enableCheckpointing(5000L);

        DataStreamSource<Tuple2<String, String>> source = env.addSource(new Tuple2TwoStringWithTimeSource());

        // 将数据发送到自定义数据输出
        DataStreamSink<Tuple2<String, String>> sink2PC = source.addSink(new CustomTwoPhaseCommitSinkUseFile(new StringSerializer(), new VoidSerializer()));


        env.execute("Sink ");
    }
}
