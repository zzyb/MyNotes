package run.process;/*
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

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.ConnectedStreams;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;
import source.Tuple4WithTimeProcessMoreKeySource;
import source.Tuple4WithTimeProcessSingleKeySource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * DataSource
 */
public class FlinkTypes4CoProcessTest {
    public static void main(String[] args) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment().setParallelism(1);

        // 数据源为：(北京,购买,2022-03-04 17:20:57,1) 四元组
        DataStreamSource dataSource = env.addSource(new Tuple4WithTimeProcessMoreKeySource());

        DataStreamSource<Tuple2<String, Long>> filterSource = env.fromCollection(
                new ArrayList<Tuple2<String, Long>>(
                        Arrays.asList(
                                new Tuple2<>("北京", 1000L * 10),
                                new Tuple2<>("上海", 1000L * 20)
                        )
                )
        );

        ConnectedStreams connect = dataSource.connect(filterSource);

        ConnectedStreams connectedStreams = connect.keyBy(new KeySelector<Tuple4<String, String, String, Integer>, String>() {
                                                              @Override
                                                              public String getKey(Tuple4<String, String, String, Integer> value) throws Exception {
                                                                  return value.f0;
                                                              }
                                                          },
                new KeySelector<Tuple2<String, Long>, String>() {
                    @Override
                    public String getKey(Tuple2<String, Long> value) throws Exception {
                        return value.f0;
                    }
                }
        );

        connectedStreams.process(new CoProcessFunction<Tuple4<String,String,String,Integer>,Tuple2<String,Long>,Tuple4<String,String,String,Integer>>() {
            @Override
            public void processElement1(Tuple4<String, String, String, Integer> value, Context context, Collector<Tuple4<String, String, String, Integer>> collector) throws Exception {

            }

            @Override
            public void processElement2(Tuple2<String, Long> filterValue, Context context, Collector<Tuple4<String, String, String, Integer>> collector) throws Exception {

            }
        });


        env.execute("Collection ");
    }
}
