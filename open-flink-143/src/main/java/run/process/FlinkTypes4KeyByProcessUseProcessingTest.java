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
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import source.Tuple4WithTimeProcessSingleKeySource;

import java.text.SimpleDateFormat;

/**
 * DataSource
 */
public class FlinkTypes4KeyByProcessUseProcessingTest {
    public static void main(String[] args) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment().setParallelism(1);

        // 数据源为：(北京,购买,2022-03-04 17:20:57,1) 四元组
        DataStreamSource dataSource = env.addSource(new Tuple4WithTimeProcessSingleKeySource());


        dataSource
                // 以 第一个为 key分区
                .keyBy(new KeySelector<Tuple4<String, String, String, Integer>, String>() {
                    @Override
                    public String getKey(Tuple4<String, String, String, Integer> value) throws Exception {
                        return value.f0;
                    }
                })

                // 创建处理函数 <key,In,Out>
                .process(new KeyedProcessFunction<String, Tuple4<String, String, String, Integer>, Tuple4<String, String, String, String>>() {

                    // 声明状态，用于在处理过程中收集上一个元素的特定值。
                    private transient ValueStateDescriptor<String> last;
                    private transient ValueState<String> lastState ;

                    @Override
                    //状态在open方法中被创建。
                    public void open(Configuration parameters) throws Exception {
                        last = new ValueStateDescriptor<String>("lastValue",Types.STRING);
                        lastState = getRuntimeContext().getState(last);
                    }

                    @Override
                    // 不断地处理数据，如果得到连续两个出售，则设定告警
                    public void processElement(Tuple4<String, String, String, Integer> value, Context ctx, Collector<Tuple4<String, String, String, String>> out) throws Exception {
                        // 获取上一个的状态（操作状态）
                        String pervType = lastState.value();
                        if(pervType == null) {
                            pervType = "";
                        }
                        // 更新最新的状态（操作状态）
                        lastState.update(value.f1);
                        // 关键逻辑，如果本次数据 与 上一条均为出售，则在时间服务中创建计时器
                        // 此处创建处理时间计时器。
                        if(pervType.equals("出售") && value.f1.equals("出售")){
                            // 达到条件，此处设置当前时间两秒后告警。
                            ctx.timerService().registerProcessingTimeTimer(System.currentTimeMillis() + 2000L);
                        }
                        Tuple4 t4 = new Tuple4<String,String,String,String>();
                        t4.setFields(value.f0,value.f1,value.f2,"1");
                        out.collect(t4);
                    }

                    @Override
                    // 计时器完成时调用
                    public void onTimer(long timestamp, OnTimerContext ctx, Collector<Tuple4<String, String, String, String>> out) throws Exception {
                        Tuple4 t4 = new Tuple4<String,String,String,String>();
                        t4.setFields("","","","连续卖出！！！");
                        out.collect(t4);
                    }
                }).print();



        env.execute("Collection ");
    }
}
