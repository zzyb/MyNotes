package example.run.window.join;/*
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
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * DataSource 根据下方链接图形理解
 * 详细见：https://nightlies.apache.org/flink/flink-docs-release-1.14/docs/dev/datastream/operators/joining/
 */
public class FlinkTypes4IntervalJoinTest {
    public static void main(String[] args) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment().setParallelism(1);

        // 数据源为：(北京,购买,17:20:57,1) 四元组
        // 数据源为：
        // ("郑州", "购买", "2022-01-01 00:00:00", 1) 四元组
        DataStreamSource sourceA = env.fromCollection(
                new ArrayList<Tuple4<String, String, String, Integer>>(
                        Arrays.asList(
                                // 00秒的3条数据
                                new Tuple4<String, String, String, Integer>("郑州", "购买1", "2022-01-01 00:05:30", 1),
                                new Tuple4<String, String, String, Integer>("上海", "购买1", "2022-01-01 00:05:30", 1),
                                // 10s后的2条数据
                                new Tuple4<String, String, String, Integer>("上海", "购买2", "2022-01-01 00:05:40", 1),
                                new Tuple4<String, String, String, Integer>("上海", "购买3", "2022-01-01 00:05:40", 1)
                        )
                )
        );

        DataStreamSource sourceB = env.fromCollection(
                new ArrayList<Tuple4<String, String, String, Integer>>(
                        Arrays.asList(
                                // 00秒的3条数据
                                new Tuple4<String, String, String, Integer>("上海", "出售1", "2022-01-01 00:05:30", 1),
                                // 10s后的2三条数据
                                new Tuple4<String, String, String, Integer>("上海", "出售2", "2022-01-01 00:05:30", 1),
                                new Tuple4<String, String, String, Integer>("上海", "出售3", "2022-01-01 00:05:36", 1)
                        )
                )
        );

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
        // 只要下界总是小于或等于上界，下界和上界都可以是负的或正的。
        // 只要下界总是小于或等于上界，下界和上界都可以是负的或正的。
        // 只要下界总是小于或等于上界，下界和上界都可以是负的或正的。
        s1
                .keyBy(new Tuple4WithTimeKeyBy())
                .intervalJoin(s2.keyBy(new Tuple4WithTimeKeyBy()))
                // 前5s ~ 后10s
                // 基于间隔的JOIN需要多记录进行缓冲： 详细见示例图
                // 对第一个流：缓冲 [ 时间戳 > （水位线 - 上界）]
                // 如果当前水位线是00:00:10，那么 example.time - 10s ,缓冲 00:00:00 以后数据
                // 对第二个流：缓冲 [ 时间戳 > （水位线 + 下界）]
                // 如果当前水位线是00:00:10，那么 example.time + (-5)s ,缓冲 00:00:05 以后数据
                .between(Time.milliseconds(-5 * 1000L), Time.milliseconds(10 * 1000L))
                .process(new ProcessJoinFunction<Tuple4<String, String, String, Integer>, Tuple4<String, String, String, Integer>, String>() {
                    @Override
                    public void processElement(Tuple4<String, String, String, Integer> left, Tuple4<String, String, String, Integer> right, Context ctx, Collector<String> out) throws Exception {
                        out.collect(left.toString() + " ---- " + right.toString());
                    }
                }).print();


        env.execute("Collection ");
    }
}
