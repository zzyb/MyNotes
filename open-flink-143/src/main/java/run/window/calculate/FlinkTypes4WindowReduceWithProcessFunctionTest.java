package run.window.calculate;/*
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

import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import source.Tuple4WithTimeProcessMoreKeySource;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

/**
 * DataSource
 */
public class FlinkTypes4WindowReduceWithProcessFunctionTest {
    public static void main(String[] args) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment().setParallelism(1);

        // 数据源为：(北京,购买,2022-03-04 17:20:57,1) 四元组
        DataStreamSource dataSource = env.addSource(new Tuple4WithTimeProcessMoreKeySource());

        KeyedStream keyedStream = dataSource.keyBy(new KeySelector<Tuple4<String, String, String, Integer>, String>() {
            @Override
            public String getKey(Tuple4<String, String, String, Integer> value) throws Exception {
                return value.f0;
            }
        });

        // 大小为5的窗口，偏移量为2s; 表示从每分钟的02秒开始计算大小为5s的窗口，例如：00:00:02、00:00:07、00:00:12等
        WindowedStream window = keyedStream.window(TumblingProcessingTimeWindows.of(Time.milliseconds(5000L), Time.milliseconds(2000L)));

        // reduce,增量聚合
        SingleOutputStreamOperator reduceWithProcess = window.reduce(
                new ReduceFunction<Tuple4<String, String, String, Integer>>() {
                    @Override
                    public Tuple4<String, String, String, Integer> reduce(Tuple4<String, String, String, Integer> value1, Tuple4<String, String, String, Integer> value2) throws Exception {
                        Tuple4<String, String, String, Integer> t4 = new Tuple4<>();
                        t4.setFields(value1.f0, value1.f1, value1.f2, value1.f3 + value2.f3);
                        return t4;
                    }
                },
                new ProcessWindowFunction<Tuple4<String,String,String,Integer>, Tuple3<String,String,Integer>,String, TimeWindow>() {
                    @Override
                    public void process(String key, Context context, Iterable<Tuple4<String, String, String, Integer>> elements, Collector<Tuple3<String, String, Integer>> out) throws Exception {
                        Iterator<Tuple4<String, String, String, Integer>> iterator = elements.iterator();
                        Tuple4<String,String,String,Integer> onlyValue = iterator.hasNext() ? iterator.next() : new Tuple4<String,String,String,Integer>();
                        long end = context.window().getEnd();
                        Tuple3<String, String, Integer> t3 = new Tuple3<>();
                        t3.setFields(key,format.format(new Date(end)),onlyValue.f3);
                        out.collect(t3);
                    }
                }
        );

        reduceWithProcess.print();


        env.execute("Collection ");
    }
}
