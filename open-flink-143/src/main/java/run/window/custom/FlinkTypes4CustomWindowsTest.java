package run.window.custom;/*
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
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.WindowAssigner;
import org.apache.flink.streaming.api.windowing.triggers.ProcessingTimeTrigger;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import source.Tuple4WithTimeProcessMoreKeySource;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * DataSource
 */
public class FlinkTypes4CustomWindowsTest {
    public static void main(String[] args) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment().setParallelism(1);

        // 数据源为：(北京,购买,2022-03-04 17:20:57,1) 四元组
        DataStreamSource dataSource = env.addSource(new Tuple4WithTimeProcessMoreKeySource());

        SingleOutputStreamOperator map = dataSource.map(new MapFunction<Tuple4<String, String, String, Integer>, Tuple2<String, Integer>>() {
            @Override
            public Tuple2<String, Integer> map(Tuple4<String, String, String, Integer> value) throws Exception {
                Tuple2<String, Integer> t2 = new Tuple2<>();
                t2.setFields(value.f0, value.f3);
                return t2;
            }
        });

        KeyedStream keyedStream = map.keyBy(new KeySelector<Tuple2<String, Integer>, String>() {
            @Override
            public String getKey(Tuple2<String, Integer> value) throws Exception {
                return value.f0;
            }
        });

        WindowedStream window = keyedStream.window(
                new WindowAssigner<Object, TimeWindow>() {

                    private final long windowSize = 5 * 1000L;

                    @Override
                    public Collection<TimeWindow> assignWindows(Object element, long timestamp, WindowAssignerContext context) {
                        // 获取当前时间
                        long currentProcessingTime = context.getCurrentProcessingTime();
                        // 计算窗口开始时间：当前时间 - 当前时间对窗口大小取余
                        // 假设当前时间为 104s ； 那么就是 104 - （104 % 5） = 104 - 4 = 100s ，即窗口开始时间为100s整
                        // 假设当前时间为 100s ； 那么就是 100 - （100 % 5） = 100 - 0 = 100s ，即窗口开始时间为100s整
                        // 对应在时间上就是 00:00:00 、 00:00:05 、 00:00:10等于标准时间对齐的时间。(如果希望不是对齐，可以使用偏移量，这里不介绍)
                        long start = currentProcessingTime - (currentProcessingTime % windowSize);
                        // 窗口结束时间：开始时间 + 窗口大小
                        long end = start + windowSize;
                        return Collections.singletonList(new TimeWindow(start, end));
                    }

                    @Override
                    public Trigger<Object, TimeWindow> getDefaultTrigger(StreamExecutionEnvironment env) {
                        ProcessingTimeTrigger processingTimeTrigger = ProcessingTimeTrigger.create();
                        return processingTimeTrigger;
                    }

                    @Override
                    public TypeSerializer<TimeWindow> getWindowSerializer(ExecutionConfig executionConfig) {
                        return new TimeWindow.Serializer();
                    }

                    @Override
                    public boolean isEventTime() {
                        return false;
                    }
                }
        );

        window.sum(1).print();


        env.execute("Collection ");
    }
}
