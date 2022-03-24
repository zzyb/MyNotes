package example.run.job;/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import example.keyby.Tuple4WithTimeKeyBy;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * DataSource
 * 使用状态并清除无用状态（防止状态不断扩大）
 */
public class FlinkTypes4KeyByProcessUseProcessingWithStateTest {
    public static void main(String[] args) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment().setParallelism(1);

        // 数据源为：(北京,购买,2022-03-04 17:20:57,1) 四元组
        DataStreamSource dataSource = env.fromCollection(
                new ArrayList<Tuple4<String, String, String, Integer>>(
                        Arrays.asList(
                                // 00秒的3条数据
                                new Tuple4<String, String, String, Integer>("郑州", "售出", "2022-01-01 00:00:00", 1),
                                new Tuple4<String, String, String, Integer>("上海", "售出", "2022-01-01 00:00:00", 1),
                                new Tuple4<String, String, String, Integer>("上海", "购买", "2022-01-01 00:00:00", 1),
                                // 9s后的2条数据
                                new Tuple4<String, String, String, Integer>("郑州", "售出", "2022-01-01 00:00:09", 6),
                                new Tuple4<String, String, String, Integer>("上海", "售出", "2022-01-01 00:00:09", 7),
                                // 10s后的5条数据
                                new Tuple4<String, String, String, Integer>("郑州", "售出", "2022-01-01 00:01:20", 4),
                                new Tuple4<String, String, String, Integer>("郑州", "售出", "2022-01-01 00:01:20", 4),
                                new Tuple4<String, String, String, Integer>("郑州", "购买", "2022-01-01 00:01:20", 4),
                                new Tuple4<String, String, String, Integer>("郑州", "购买", "2022-01-01 00:01:20", 4),
                                new Tuple4<String, String, String, Integer>("南京", "购买", "2022-01-01 00:01:20", 4),
                                new Tuple4<String, String, String, Integer>("上海", "购买", "2022-01-01 00:01:20", 4)
                        )
                )
        );

        OutputTag<String> warnOutPut = new OutputTag<String>("warn") {
        };

        SingleOutputStreamOperator process = dataSource
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<Tuple4<String, String, String, Integer>>forBoundedOutOfOrderness(Duration.ZERO)
                                .withTimestampAssigner((element, timestamp) -> {
                                    try {
                                        return format.parse(element.f2).getTime();
                                    } catch (ParseException e) {
                                        return 0L;
                                    }
                                })
                )
                .keyBy(new Tuple4WithTimeKeyBy())
                // 创建处理函数 <key,In,Out>
                .process(new KeyedProcessFunction<String, Tuple4<String, String, String, Integer>, Tuple4<String, String, String, String>>() {

                    // 声明状态，用于在处理过程中收集上一个元素的特定值。
                    private transient ValueStateDescriptor<String> lastValueStateDesc;
                    private transient ValueState<String> lastValueState;
                    // 声明时间戳，用于清除状态使用!!!
                    // 声明时间戳，用于清除状态使用!!!
                    // 声明时间戳，用于清除状态使用!!!
                    private transient ValueStateDescriptor<Long> lastTimeStateDesc;
                    private transient ValueState<Long> lastTimeState;

                    @Override
                    //状态在open方法中被创建。
                    public void open(Configuration parameters) throws Exception {
                        lastValueStateDesc = new ValueStateDescriptor<String>("lastValue", Types.STRING);
                        lastValueState = getRuntimeContext().getState(lastValueStateDesc);

                        lastTimeStateDesc = new ValueStateDescriptor<Long>("lastTime", Types.LONG);
                        lastTimeState = getRuntimeContext().getState(lastTimeStateDesc);
                    }

                    @Override
                    // 不断地处理数据，如果得到连续两个出售，则设定告警
                    public void processElement(Tuple4<String, String, String, Integer> value, Context ctx, Collector<Tuple4<String, String, String, String>> out) throws Exception {
                        // 获取上一个的状态（操作状态）
                        String pervType = lastValueState.value();
                        if (pervType == null) {
                            pervType = "";
                        }

                        // 设置清理状态计时器时间为记录时间戳晚10秒
                        long newTimer = ctx.timestamp() + 1000L * 60;
                        if (lastTimeState.value() == null) {
                            lastTimeState.update(0L);
                        }
                        long curTimer = lastTimeState.value();
                        ctx.timerService().deleteEventTimeTimer(curTimer);
                        ctx.timerService().registerEventTimeTimer(newTimer);
                        lastTimeState.update(newTimer);

                        // 更新最新的状态（操作状态）
                        lastValueState.update(value.f1);
                        // 关键逻辑，如果本次数据 与 上一条均为出售，则在时间服务中创建计时器
                        // 此处创建处理时间计时器。
                        if (pervType.equals("售出") && value.f1.equals("售出")) {
                            // 达到条件，此处设置当前时间两秒后告警。
                            ctx.output(warnOutPut, value.f0 + value.f2 + "   连续售出告警！！！");
                        }
                        Tuple4 t4 = new Tuple4<String, String, String, String>();
                        t4.setFields(value.f0, value.f1, value.f2, String.valueOf(value.f3));
                        out.collect(t4);
                    }

                    @Override
                    // 计时器完成时调用
                    public void onTimer(long timestamp, OnTimerContext ctx, Collector<Tuple4<String, String, String, String>> out) throws Exception {
                        // 为了展示出状态被清除
                        System.out.println(ctx.getCurrentKey() + "clear State！！！");
                        // 清除所有状态
                        lastTimeState.clear();
                        lastValueState.clear();
                    }
                });

        DataStream warnSide = process.getSideOutput(warnOutPut);

        process.print();
        warnSide.print();


        env.execute("Collection ");
    }
}
