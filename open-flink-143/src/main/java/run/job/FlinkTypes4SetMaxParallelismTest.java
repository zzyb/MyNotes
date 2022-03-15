package run.job;/*
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
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * DataSource
 */
public class FlinkTypes4SetMaxParallelismTest {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment()
                .setParallelism(1)
                //设置所有算子最大并行度(应用级别)
                //设置所有算子最大并行度
                //设置所有算子最大并行度
                .setMaxParallelism(128);

        // 数据源为：
        // ("郑州", "购买", "2022-01-01 00:00:00", 1) 四元组
        DataStreamSource sourceA = env.fromCollection(
                new ArrayList<Tuple4<String, String, String, Integer>>(
                        Arrays.asList(
                                // 00秒的3条数据
                                new Tuple4<String, String, String, Integer>("郑州", "购买", "2022-01-01 00:00:00", 1),
                                new Tuple4<String, String, String, Integer>("上海", "购买", "2022-01-01 00:00:00", 1),
                                new Tuple4<String, String, String, Integer>("上海", "购买", "2022-01-01 00:00:00", 1),
                                // 10s后的2条数据
                                new Tuple4<String, String, String, Integer>("郑州", "售出", "2022-01-01 00:00:10", 6),
                                new Tuple4<String, String, String, Integer>("上海", "售出", "2022-01-01 00:00:10", 7)
                        )
                )
        );

        KeyedStream keyedStream = sourceA.keyBy(new Tuple4WithTimeKeyBy());

        keyedStream
                .map(value -> value)
                //为此算子设置最大并行度(会覆盖应用级别设置)
                //为此算子设置最大并行度(会覆盖应用级别设置)
                //为此算子设置最大并行度(会覆盖应用级别设置)
                .setMaxParallelism(64);


        env.execute("Collection ");
    }
}
