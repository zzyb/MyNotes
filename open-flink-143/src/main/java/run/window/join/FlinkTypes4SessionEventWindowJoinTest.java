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
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.EventTimeSessionWindows;
import org.apache.flink.streaming.api.windowing.assigners.SessionWindowTimeGapExtractor;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import source.join.Tuple4WithTimeASource;
import source.join.Tuple4WithTimeCSource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;

/**
 * DataSource 根据下方链接图形理解
 * 详细见：https://nightlies.apache.org/flink/flink-docs-release-1.14/docs/dev/datastream/operators/joining/
 */
public class FlinkTypes4SessionEventWindowJoinTest {
    public static void main(String[] args) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment().setParallelism(1);

        // 数据源为：(北京,购买,17:20:57,1) 四元组
        DataStreamSource sourceA = env.addSource(new Tuple4WithTimeASource());
        DataStreamSource sourceC = env.addSource(new Tuple4WithTimeCSource());

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
        SingleOutputStreamOperator s3 = sourceC.assignTimestampsAndWatermarks(
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


        DataStream apply = s1
                .join(s3)
                .where(new Tuple4WithTimeKeyBy())
                .equalTo(new Tuple4WithTimeKeyBy())
                .window(EventTimeSessionWindows.withGap(Time.milliseconds(2000L)))
                .apply(new JoinFunction<Tuple4<String, String, String, Integer>, Tuple4<String, String, String, Integer>, String>() {
                    @Override
                    public String join(Tuple4<String, String, String, Integer> first, Tuple4<String, String, String, Integer> second) throws Exception {
                        StringBuffer typeSb = new StringBuffer();
                        return typeSb.append(first.toString()).append(" --> ").append(second.toString()).toString();
                    }
                });

        apply.print();


        env.execute("Collection ");
    }
}
