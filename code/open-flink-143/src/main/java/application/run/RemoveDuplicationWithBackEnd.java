package application.run;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

/**
 * 利用状态对海量数据去重复：
 * 1 将数据转换为keyed流，key为去重的数据
 * 2 利用ValueState状态，当第一次出现时，发出且ValueState赋值1
 * 第二次或N次出现时，ValueState增加1但不再发出。
 * 3 为了防止状态泄露，设置某个key最后出现的1小时清除状态！！！
 */
public class RemoveDuplicationWithBackEnd {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment().setParallelism(6);

        DataStreamSource<String> dataSource = env.readTextFile("D:\\temp\\hadoop_data.txt");

        SingleOutputStreamOperator<Tuple2<String, Long>> map2T2 = dataSource.map(new RichMapFunction<String, Tuple2<String, Long>>() {
            Tuple2<String, Long> t2;

            @Override
            public void open(Configuration parameters) throws Exception {
                t2 = new Tuple2<String, Long>();
            }

            @Override
            public Tuple2<String, Long> map(String value) throws Exception {
                t2.setFields(value, 0L);
                return t2;
            }
        });


        map2T2
                .keyBy(new KeySelector<Tuple2<String, Long>, String>() {
                    @Override
                    public String getKey(Tuple2<String, Long> value) throws Exception {
                        return value.f0;
                    }
                })
                .process(new KeyedProcessFunction<String, Tuple2<String, Long>, String>() {
                    // 用于去重
                    ValueState<Long> key;
                    ValueStateDescriptor<Long> keyDesc;

                    // 用于计时并清除状态
                    ValueState<Long> timer;
                    ValueStateDescriptor<Long> timerDesc;
                    // 清除间隔
                    long time_interval;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        keyDesc = new ValueStateDescriptor<Long>("keyDesc", Types.LONG);
                        key = getRuntimeContext().getState(keyDesc);
                        timerDesc = new ValueStateDescriptor<Long>("timerDesc", Types.LONG);
                        timer = getRuntimeContext().getState(timerDesc);
                        time_interval = 1000L * 60 * 60; // 1小时
                    }

                    @Override
                    public void processElement(Tuple2<String, Long> value, Context ctx, Collector<String> out) throws Exception {
                        if (null == timer.value()) {
                            timer.update(0L);
                        }
                        // 最新时间
//                        long newTimer = ctx.timestamp() + time_interval;
                        long newTimer = ctx.timerService().currentProcessingTime() + time_interval;
                        // 获取过去时间
                        long oldTimer = timer.value();
                        ctx.timerService().deleteProcessingTimeTimer(oldTimer);
                        ctx.timerService().registerProcessingTimeTimer(newTimer);
                        timer.update(newTimer);
                        if (null == key.value()) {
                            key.update(1L);
                            out.collect(value.f0);
                        } else {
                            key.update(key.value() + 1L);
                        }
                    }

                    @Override
                    public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
                        timer.clear();
                        key.clear();
                    }
                }).print();


        env.execute("Collection ");
    }
}
