package example.run.job;/*
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

import example.keyby.Tuple4WithTimeKeyBy;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ReadOnlyBroadcastState;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 * DataSource
 */
public class FlinkTypes4EnableCheckPointingTest {
    public static void main(String[] args) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment().setParallelism(2);

        // 开启检查点功能
        // 开启检查点功能
        // 开启检查点功能
        env.enableCheckpointing(1000L * 60);

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
                                new Tuple4<String, String, String, Integer>("郑州", "售出4", "2022-01-01 00:00:10", 6),
                                new Tuple4<String, String, String, Integer>("上海", "售出5", "2022-01-01 00:00:10", 7),
                                // 10s后的5条数据
                                new Tuple4<String, String, String, Integer>("郑州", "售出4", "2022-01-01 00:00:20", 4),
                                new Tuple4<String, String, String, Integer>("郑州", "售出4", "2022-01-01 00:00:20", 4),
                                new Tuple4<String, String, String, Integer>("郑州", "购买4", "2022-01-01 00:00:20", 4),
                                new Tuple4<String, String, String, Integer>("郑州", "购买4", "2022-01-01 00:00:20", 4),
                                new Tuple4<String, String, String, Integer>("南京", "购买4", "2022-01-01 00:00:20", 4),
                                new Tuple4<String, String, String, Integer>("上海", "购买5", "2022-01-01 00:00:20", 4)
                        )
                )
        );

        DataStreamSource<Tuple2<String, String>> sourceB = env.fromCollection(new ArrayList<Tuple2<String, String>>(Arrays.asList(
                new Tuple2<String, String>("购买", "送小花园"),
                new Tuple2<String, String>("售出", "增值5W劵")
        )));

        KeyedStream keyedStream = sourceA.keyBy(new Tuple4WithTimeKeyBy());

        // 广播状态描述符
        MapStateDescriptor<String, String> mapStateDescriptor = new MapStateDescriptor<String, String>(
                "mapStateDescriptor",
                TypeInformation.of(String.class),
                TypeInformation.of(String.class)
//                Types.STRING,
//                Types.STRING
        );

        // 通过MapStateDescriptor对象定义广播对象。
        BroadcastStream broadcast = sourceB.broadcast(mapStateDescriptor);

        SingleOutputStreamOperator connBroadcast = keyedStream
                .connect(broadcast)
                .process(new KeyedBroadcastProcessFunction<String, Tuple4<String, String, String, Integer>, Tuple2<String, String>, Tuple5<String, String, String, Integer, String>>() {
                    @Override
                    // 获得只读上下文，只能对广播状态进行只读操作。(安全机制：确保所有并行实例中广播状态所保存的信息完全相同。)
                    public void processElement(Tuple4<String, String, String, Integer> value, ReadOnlyContext ctx, Collector<Tuple5<String, String, String, Integer, String>> out) throws Exception {
                        // 通过向只读上下文传入广播状态描述符，获取广播状态。
                        ReadOnlyBroadcastState<String, String> broadcastState = ctx.getBroadcastState(mapStateDescriptor);

                        Tuple5<String, String, String, Integer, String> t5 = new Tuple5<>();
                        t5.setFields(value.f0, value.f1, value.f2, value.f3, "暂无优惠信息。"); // 基本值设置

                        // 这里获取广播状态的信息
                        // 此处根据广播状态对数据进行补充。
                        for (Map.Entry<String, String> entry : broadcastState.immutableEntries()) {
                            String k = entry.getKey();
                            String v = entry.getValue();
                            if (value.f1.startsWith(k)) {
                                t5.setField(v, 4);
                                break;
                            }
                        }

                        // 可以直接使用广播状态提供的contains方法，但是只适用于key完全相同，我们的value与广播状态的key开头相同，所以用不了（形如：“购买”、“购买N”）
//                        if (broadcastState.contains(value.f1)) {
//                            String broadcastValue = broadcastState.get(value.f1);
//                            t5.setFields(value.f0, value.f1, value.f2, value.f3, broadcastValue);
//                        } else {
//                            t5.setFields(value.f0, value.f1, value.f2, value.f3, "null");
//                        }

                        out.collect(t5);
                    }

                    @Override
                    public void processBroadcastElement(Tuple2<String, String> value, Context ctx, Collector<Tuple5<String, String, String, Integer, String>> out) throws Exception {
                        BroadcastState<String, String> broadcastState = ctx.getBroadcastState(mapStateDescriptor);
                        broadcastState.put(value.f0, value.f1);
                    }
                });

        connBroadcast.print();


        env.execute("Collection ");
    }
}
