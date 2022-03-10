package run.window.join;/*
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

import keyby.Tuple4WithTimeKeyBy;
import org.apache.flink.api.common.eventtime.TimestampAssigner;
import org.apache.flink.api.common.eventtime.TimestampAssignerSupplier;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;
import source.Tuple4WithTimeProcessMoreKeySource;
import source.join.Tuple4WithTimeASource;
import source.join.Tuple4WithTimeBSource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;

/**
 * DataSource
 */
public class FlinkTypes4IntervalJoinTest {
    public static void main(String[] args) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment().setParallelism(1);

        // 数据源为：(北京,购买,17:20:57,1) 四元组
        DataStreamSource sourceA = env.addSource(new Tuple4WithTimeASource());
        DataStreamSource sourceB = env.addSource(new Tuple4WithTimeBSource());

        SingleOutputStreamOperator s1 = sourceA.assignTimestampsAndWatermarks(
                WatermarkStrategy
                        .<Tuple4<String, String, String, Integer>>forBoundedOutOfOrderness(Duration.ZERO)
                        .withTimestampAssigner((element, recordTimestamp) -> {
                            try {
                                return format.parse(element.f2).getTime();
                            } catch (ParseException e) {
                                return 0L;
                            }
                        })
        );
        SingleOutputStreamOperator s2 = sourceB.assignTimestampsAndWatermarks(
                WatermarkStrategy
                        .<Tuple4<String, String, String, Integer>>forBoundedOutOfOrderness(Duration.ZERO)
                        .withTimestampAssigner((element, recordTimestamp) -> {
                            try {
                                return format.parse(element.f2).getTime();
                            } catch (ParseException e) {
                                return 0L;
                            }
                        })
        );

        // 间隔Join 仅支持事件时间，一定要设置！！！
        // 间隔Join 仅支持事件时间，一定要设置！！！
        // 间隔Join 仅支持事件时间，一定要设置！！！
        s1
                .keyBy(new Tuple4WithTimeKeyBy())
                .intervalJoin(s2.keyBy(new Tuple4WithTimeKeyBy()))
                .between(Time.milliseconds(-5000L), Time.milliseconds(5000L))
                .process(new ProcessJoinFunction<Tuple4<String, String, String, Integer>, Tuple4<String, String, String, Integer>, Tuple4<String, String, String, Integer>>() {
                    @Override
                    public void processElement(Tuple4<String, String, String, Integer> left, Tuple4<String, String, String, Integer> right, Context ctx, Collector<Tuple4<String, String, String, Integer>> out) throws Exception {
                        Tuple4<String, String, String, Integer> result = new Tuple4<>();
                        StringBuffer typeSb = new StringBuffer();
                        result.setFields(
                                left.f0,
                                typeSb.append(left.f1).append(" ").append(right.f1).toString(),
                                "",
                                left.f3 + right.f3
                        );

                        out.collect(result);
                    }
                }).print();


        env.execute("Collection ");
    }
}
