package run.window.lateData.redirect;/*
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

import org.apache.flink.api.common.eventtime.TimestampAssigner;
import org.apache.flink.api.common.eventtime.TimestampAssignerSupplier;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.*;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import source.Tuple4WithTimeProcessMoreKeySource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DataSource
 */
public class FlinkTypes4TumblingEventTimeWindowProcessLateDataTest {
    public static void main(String[] args) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment().setParallelism(1);

        // 数据源为：(北京,购买,2022-03-04 17:20:57,1) 四元组
        DataStreamSource dataSource = env.addSource(new Tuple4WithTimeProcessMoreKeySource());

        SingleOutputStreamOperator singleOutputStreamOperator = dataSource.assignTimestampsAndWatermarks(
                WatermarkStrategy
                        .<Tuple4<String, String, String, Integer>>forBoundedOutOfOrderness(Duration.ZERO)
                        .withTimestampAssigner(new TimestampAssignerSupplier<Tuple4<String, String, String, Integer>>() {
                            @Override
                            public TimestampAssigner<Tuple4<String, String, String, Integer>> createTimestampAssigner(Context context) {
                                return (element, recordTimestamp) -> {
                                    try {
                                        return format.parse(element.f2).getTime();
                                    } catch (ParseException e) {
                                        return 0L;
                                    }
                                };
                            }
                        })
        );

        KeyedStream keyedStream = singleOutputStreamOperator.keyBy(new KeySelector<Tuple4<String, String, String, Integer>, String>() {
            @Override
            public String getKey(Tuple4<String, String, String, Integer> value) throws Exception {
                return value.f0;
            }
        });

        OutputTag<Tuple4<String, String, String, Integer>> lateDataOutput = new OutputTag<Tuple4<String, String, String, Integer>>("lateData") {
        };

        WindowedStream window = keyedStream.window(TumblingEventTimeWindows.of(Time.milliseconds(5000L)));

        SingleOutputStreamOperator process = window
                .sideOutputLateData(lateDataOutput) // 获取迟到数据!!! (不需要根据水位线比较)
                // 处理函数中，进行逻辑计算
                .process(new ProcessWindowFunction<Tuple4<String, String, String, Integer>, Tuple4<String, String, String, Integer>, String, TimeWindow>() {
                    @Override
                    public void process(String key, Context context, Iterable<Tuple4<String, String, String, Integer>> elements, Collector<Tuple4<String, String, String, Integer>> out) throws Exception {
                        Tuple4<String, String, String, Integer> t4 = new Tuple4<>();
                        AtomicInteger sum = new AtomicInteger();
                        elements.forEach(
                                element -> {
                                    sum.getAndAdd(1);
                                }
                        );
                        t4.setFields(key, "", "", sum.get());
                        out.collect(t4);
                    }
                });

        // 从处理函数获取迟到数据的输出
        DataStream sideOutput = process.getSideOutput(lateDataOutput);

        process.print();
        sideOutput.print();

        env.execute("Collection ");
    }
}
