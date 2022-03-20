package run.connector.async;

import connector.sink.custom.CustomTwoPhaseCommitSinkUseFile;
import connector.source.Tuple2TwoStringWithTimeSource;
import operator.async.AsyncFunction2Mysql;
import org.apache.flink.api.common.typeutils.base.StringSerializer;
import org.apache.flink.api.common.typeutils.base.VoidSerializer;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.concurrent.TimeUnit;

public class FlinkAsyncFunctionTest {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment().setParallelism(1);

        env.enableCheckpointing(5000L);

        DataStreamSource<Tuple2<String, String>> source = env.addSource(new Tuple2TwoStringWithTimeSource());

        SingleOutputStreamOperator<Tuple3<String, String, String>> asyncStream = AsyncDataStream.unorderedWait(source, new AsyncFunction2Mysql(), 5000, TimeUnit.MILLISECONDS, 10);

        asyncStream.print();


        env.execute("Sink ");
    }
}
