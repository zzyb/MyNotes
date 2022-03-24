package example.run.window.calculate;/*
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

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import example.connector.source.Tuple4WithTimeProcessMoreKeySource;

import java.text.SimpleDateFormat;

/**
 * DataSource
 */
public class FlinkTypes4WindowAggregateFunctionTest {
    public static void main(String[] args) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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

        // aggregate
        window.aggregate(new AggregateFunction<Tuple4<String,String,String,Integer>,Tuple3<String,String,Integer>, Tuple3<String,String,Integer>>() {

            @Override
            // 创建累加器，启动聚合
            public Tuple3<String, String, Integer> createAccumulator() {
                Tuple3<String, String, Integer> t3 = new Tuple3<>();
                t3.setFields("","",0);
                return t3;
            }

            @Override
            // 向累加器添加一个输入元素，返回输入元素后的累加器
            public Tuple3<String, String, Integer> add(Tuple4<String, String, String, Integer> value, Tuple3<String, String, Integer> accumulator) {
                // 此处一个简单的逻辑：一单购买+100积分，一单出售+1积分
                if(value.f1.equals("购买")){
                    accumulator.setFields(value.f0,value.f1,accumulator.f2+100);
                } else {
                    accumulator.setFields(value.f0,value.f1,accumulator.f2+1);
                }
                return accumulator;
            }

            @Override
            // 根据累加器计算，返回最终结果。（此处直接返回累加器：因为例子中累加器和最终结果类型一致！！！）
            // 当累加器和最终结果类型不一致时，往往需要从累加器计算得到最终结果。
            public Tuple3<String, String, Integer> getResult(Tuple3<String, String, Integer> accumulator) {
                return accumulator;
            }

            @Override
            // 两个累加器合并，返回合并结果
            public Tuple3<String, String, Integer> merge(Tuple3<String, String, Integer> a, Tuple3<String, String, Integer> b) {
                a.setFields(a.f0,a.f1,a.f2+b.f2);
                return a;
            }
        }).print();


        env.execute("Collection ");
    }
}
