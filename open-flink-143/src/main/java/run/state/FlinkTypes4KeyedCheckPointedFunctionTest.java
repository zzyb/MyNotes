package run.state;/*
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
import operator.state.KeyedCheckPointedFunctionByRichFlatMap;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * DataSource
 */
public class FlinkTypes4KeyedCheckPointedFunctionTest {
    public static void main(String[] args) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment().setParallelism(2);

        // 数据源为：
        // ("郑州", "购买", "2022-01-01 00:00:00", 1) 四元组
        DataStreamSource sourceA = env.fromCollection(
                new ArrayList<Tuple4<String, String, String, Integer>>(
                        Arrays.asList(
                                // 00秒的3条数据
                                new Tuple4<String, String, String, Integer>("郑州", "购买1", "2022-01-01 00:00:00", 1),
                                new Tuple4<String, String, String, Integer>("上海", "购买2", "2022-01-01 00:00:00", 1),
                                new Tuple4<String, String, String, Integer>("上海", "购买3", "2022-01-01 00:00:00", 1),
                                // 10s后的2条数据
                                new Tuple4<String, String, String, Integer>("郑州", "购买4", "2022-01-01 00:00:10", 6),
                                new Tuple4<String, String, String, Integer>("上海", "购买5", "2022-01-01 00:00:10", 7),
                                // 10s后的5条数据
                                new Tuple4<String, String, String, Integer>("广州", "购买4", "2022-01-01 00:00:20", 4),
                                new Tuple4<String, String, String, Integer>("新乡", "购买4", "2022-01-01 00:00:20", 4),
                                new Tuple4<String, String, String, Integer>("青岛", "购买4", "2022-01-01 00:00:20", 4),
                                new Tuple4<String, String, String, Integer>("海南", "购买4", "2022-01-01 00:00:20", 4),
                                new Tuple4<String, String, String, Integer>("深圳", "购买5", "2022-01-01 00:00:20", 4)
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
        KeyedStream keyedStream = s1.keyBy(new Tuple4WithTimeKeyBy());

        // 键值 keyBy
        // 键值 keyBy
        // 键值 keyBy
        SingleOutputStreamOperator checkPointedFunctionFlatMap = keyedStream.flatMap(new KeyedCheckPointedFunctionByRichFlatMap());
        checkPointedFunctionFlatMap.print();

        env.execute("Collection ");
    }
}
