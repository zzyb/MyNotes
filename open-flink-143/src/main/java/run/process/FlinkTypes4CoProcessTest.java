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

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.ConnectedStreams;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;
import connector.source.Tuple4WithTimeProcessMoreKeySource;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * DataSource
 */
public class FlinkTypes4CoProcessTest {
    public static void main(String[] args) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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

        connectedStreams.process(new CoProcessFunction<Tuple4<String, String, String, Integer>, Tuple2<String, Long>, Tuple4<String, String, String, Integer>>() {

            private transient ValueStateDescriptor<Boolean> dataEnable;
            private transient ValueState<Boolean> dataEnableState;

            private transient ValueStateDescriptor<Long> disableTimer;
            private transient ValueState<Long> disableTimerState;

            @Override
            public void open(Configuration parameters) throws Exception {
                //带默认值的已弃用
                //                dataEnable = new ValueStateDescriptor<Boolean>("dataEnable",Types.BOOLEAN,false);
                dataEnable = new ValueStateDescriptor<Boolean>("dataEnable", Types.BOOLEAN);
                dataEnableState = getRuntimeContext().getState(dataEnable);

                //带默认值的已弃用
                //                disableTimer = new ValueStateDescriptor<Long>("disableTimer",Types.LONG,0L);
                disableTimer = new ValueStateDescriptor<Long>("disableTimer", Types.LONG);
                disableTimerState = getRuntimeContext().getState(disableTimer);
            }

            @Override
            public void processElement1(Tuple4<String, String, String, Integer> value, Context context, Collector<Tuple4<String, String, String, Integer>> collector) throws Exception {
                if (dataEnableState.value()) {
                    collector.collect(value);
                }
            }

            @Override
            public void processElement2(Tuple2<String, Long> filterValue, Context context, Collector<Tuple4<String, String, String, Integer>> collector) throws Exception {
                if (dataEnableState.value() == null) {
                    dataEnableState.update(false);
                }
                if (disableTimerState.value() == null) {
                    disableTimerState.update(0L);
                }
                // 此分支用于控制是否开启。
                // 首先，拿到对应的数据就开启
                dataEnableState.update(true);
                // 然后，设定停止时间
                long stopTime = context.timerService().currentProcessingTime() + filterValue.f1;
                // 获取之前存在的停止时间戳
                long currentStopTime = disableTimerState.value();
                if (stopTime > currentStopTime) {
                    context.timerService().deleteProcessingTimeTimer(currentStopTime);
                    context.timerService().registerProcessingTimeTimer(stopTime);
                    disableTimerState.update(stopTime);
                }
            }

            @Override
            public void onTimer(long timestamp, OnTimerContext ctx, Collector<Tuple4<String, String, String, Integer>> out) throws Exception {
                // 定时到达时，清楚开启状态和定时状态。
                dataEnableState.clear();
                disableTimerState.clear();
            }
        }).print();


        env.execute("Collection ");
    }
}
