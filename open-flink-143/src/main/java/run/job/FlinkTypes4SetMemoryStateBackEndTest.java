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
import org.apache.flink.runtime.state.StateBackend;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.runtime.state.memory.MemoryStateBackend;
import org.apache.flink.runtime.state.storage.JobManagerCheckpointStorage;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * DataSource
 */
public class FlinkTypes4SetMemoryStateBackEndTest {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment()
                .setParallelism(1)
                .setMaxParallelism(128);
        /**
         创建一个新的内存状态后端，该后端接受序列化形式达到给定字节数的状态。
         检查点和默认保存点位置按照运行时配置中的指定使用。
         警告：将此值的大小增加到超出默认值 (5242880) 时应小心谨慎。 检查点状态需要通过有限大小的 RPC 消息发送到 JobManager，并且 JobManager 需要能够在其内存中保存所有聚合状态。
         参数：
         maxStateSize – 序列化状态的最大大小
         已经弃用
         MemoryStateBackend memoryStateBackend = new MemoryStateBackend();
         已经弃用
         MemoryStateBackend memoryStateBackend = new MemoryStateBackend(100);
         已经弃用
         MemoryStateBackend memoryStateBackend = new MemoryStateBackend(100);
         */

        // MemoryStateBackend()已经弃用 , 现在使用一下两行
        // MemoryStateBackend()已经弃用 , 现在使用一下两行
        // MemoryStateBackend()已经弃用 , 现在使用一下两行
        env.setStateBackend(new HashMapStateBackend()); //状态后端
        env.getCheckpointConfig().setCheckpointStorage(new JobManagerCheckpointStorage()); //检查点保存


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
                .print();


        env.execute("Collection ");
    }
}
