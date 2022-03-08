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

import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import source.Tuple4WithTimeProcessMoreKeySource;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * DataSource
 */
public class FlinkTypes4WindowProcessFunctionTest {
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
        WindowedStream window = keyedStream.window(TumblingProcessingTimeWindows.of(Time.milliseconds(5000L),Time.milliseconds(2000L)));

        // 使用process执行更加复杂的计算
        // 4个参数分别是：IN, OUT, KEY, W extends Window
        // 其中W表示继承了Window，当前flink提供了两种：1、TimeWindow（表示从开始（包括）到结束（不包括）的时间间隔的一个窗口。） 2、GlobalWindow（放置所有数据的默认窗口）
        window.process(new ProcessWindowFunction<Tuple4<String,String,String,Integer>,Tuple4<String,ArrayList<String>, String,Integer>,String, TimeWindow>() {
            Tuple4<String,ArrayList<String>,String,Integer> t4;
            @Override
            public void open(Configuration parameters) throws Exception {
                t4 = new Tuple4<String,ArrayList<String>,String,Integer>();
            }

            @Override
            // 这里我们实现了一个功能，记录购买售出过程，并根据购买售出进行积分加减。
            public void process(String key, Context context, Iterable<Tuple4<String, String, String, Integer>> elements, Collector<Tuple4<String, ArrayList<String>, String, Integer>> out) throws Exception {
                ArrayList<String> types = new ArrayList<>();
                AtomicInteger sum = new AtomicInteger();
                elements.forEach(value -> {
                    types.add(value.f1);
                    // 购买加一积分,否则减一积分
                    if(value.f1.equals("购买")){
                        sum.addAndGet(1);
                    } else {
                        sum.decrementAndGet(); //等效于 sum.addAndGet(-1);
                    }
                });
                t4.setFields(key,types,"",sum.get());
                out.collect(t4);


            }
        }).print();


        env.execute("Collection ");
    }
}
