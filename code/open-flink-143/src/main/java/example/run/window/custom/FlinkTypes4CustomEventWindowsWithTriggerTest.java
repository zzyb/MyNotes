package example.run.window.custom;/*
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

import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.eventtime.TimestampAssigner;
import org.apache.flink.api.common.eventtime.TimestampAssignerSupplier;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.WindowAssigner;
import org.apache.flink.streaming.api.windowing.triggers.EventTimeTrigger;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.triggers.TriggerResult;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import example.connector.source.Tuple4WithTimeProcessMoreKeySource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;

/**
 * DataSource
 */
public class FlinkTypes4CustomEventWindowsWithTriggerTest {
    public static void main(String[] args) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment().setParallelism(1);

        // 数据源为：(北京,购买,2022-03-04 17:20:57,1) 四元组
        DataStreamSource dataSource = env.addSource(new Tuple4WithTimeProcessMoreKeySource());

        SingleOutputStreamOperator singleOutputStreamOperator = dataSource.assignTimestampsAndWatermarks(
                WatermarkStrategy
                        .<Tuple4<String, String, String, Integer>>forBoundedOutOfOrderness(Duration.ofMillis(10 * 1000L))
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

        keyedStream
                .window(new WindowAssigner() {
                    private final long windowSize = 10 * 1000L;

                    @Override
                    public Collection assignWindows(Object element, long timestamp, WindowAssignerContext context) {
                        long start = timestamp - (timestamp % windowSize);
                        long end = start + windowSize; //这里时间是在开始时间上加，不要错写成时间时间本身
                        TimeWindow timeWindow = new TimeWindow(start, end);
                        return Collections.singletonList(timeWindow);
                    }

                    @Override
                    public Trigger getDefaultTrigger(StreamExecutionEnvironment env) {
                        return EventTimeTrigger.create();
                    }

                    @Override
                    public TypeSerializer getWindowSerializer(ExecutionConfig executionConfig) {
                        return new TimeWindow.Serializer();
                    }

                    @Override
                    public boolean isEventTime() {
                        return true;
                    }
                })
                .trigger(new Trigger<Tuple4<String, String, String, Integer>, TimeWindow>() {
                    @Override
                    public TriggerResult onElement(Tuple4<String, String, String, Integer> element, long timestamp, TimeWindow window, TriggerContext ctx) throws Exception {
                        // 仅第一个元素创建初始触发器
                        ValueState<Boolean> prevEnd = ctx.getPartitionedState(new ValueStateDescriptor<Boolean>("prevEnd", Types.BOOLEAN));
                        if (prevEnd.value() == null) {
                            prevEnd.update(false);
                        }
                        if (!prevEnd.value()) {
                            long currentWatermark = ctx.getCurrentWatermark();
                            // 一个触发器：水位线+5s
                            ctx.registerEventTimeTimer(currentWatermark + 5000L);
                            long end = window.getEnd();
                            // 另一个触发器：窗口结束时间
                            ctx.registerEventTimeTimer(end);
                            // 更新状态，后续元素不再进来
                            prevEnd.update(true);
                        }
                        return TriggerResult.CONTINUE;
                    }

                    @Override
                    public TriggerResult onProcessingTime(long time, TimeWindow window, TriggerContext ctx) throws Exception {
                        // 不使用处理时间。
                        return TriggerResult.CONTINUE;
                    }

                    @Override
                    public TriggerResult onEventTime(long time, TimeWindow window, TriggerContext ctx) throws Exception {
                        if (time == window.getEnd()) {
                            return TriggerResult.FIRE_AND_PURGE;
                        } else {
                            return TriggerResult.FIRE;
                        }
                    }

                    @Override
                    public void clear(TimeWindow window, TriggerContext ctx) throws Exception {
                        ValueState<Boolean> prevEnd = ctx.getPartitionedState(new ValueStateDescriptor<Boolean>("prevEnd", Types.BOOLEAN));
                        prevEnd.clear();
                    }
                })
                .sum(3)
                .print();


        env.execute("Collection ");
    }
}
