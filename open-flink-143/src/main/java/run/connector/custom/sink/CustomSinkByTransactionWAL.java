package run.connector.custom.sink;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.runtime.state.storage.FileSystemCheckpointStorage;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.runtime.operators.CheckpointCommitter;
import sink.custom.CustomIdempotentSink;
import sink.custom.CustomWriteAheadLogSink;

import java.util.ArrayList;
import java.util.Arrays;

public class CustomSinkByTransactionWAL {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment().setParallelism(1);

        DataStreamSource<Tuple2<String, String>> source = env.fromCollection(new ArrayList<Tuple2<String, String>>(Arrays.asList(
                new Tuple2<String, String>("郑州", "购买--2022-01-01 00:00:00"),
                new Tuple2<String, String>("上海", "购买--2022-01-01 00:00:00"),
                new Tuple2<String, String>("郑州", "购买--2022-01-01 00:00:20"),
                new Tuple2<String, String>("南京", "购买--2022-01-01 00:00:20"),
                new Tuple2<String, String>("上海", "购买--2022-01-01 00:00:20")
        )));

        // 将数据发送到自定义数据输出
//        source.transform(new CustomWriteAheadLogSink());

        env.execute("幂等性 Idempotent Sink ");
    }
}
