package example.run.base;/*
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

import org.apache.flink.api.common.functions.Partitioner;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import example.connector.source.MultiRecordSource;

import java.util.Random;

/**
 * DataSource
 */
public class FlinkPartitionCustomRecordTest {
    public static void main(String[] args) throws Exception {

//		getExecutionEnvironment()
//		createLocalEnvironment()
        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment().setParallelism(3);
//		.createRemoteEnvironment("3.7.191.174", 8088);

        DataStreamSource dataSource = env.addSource(new MultiRecordSource());

        // 数据均匀的发往各个分区。
//		dataSource.print();
        Random random = new Random();
        // partitionCustom方法接受两个参数：1、Partitioner 2、KeySelector 分别指定分区方式、分区参考键。
        // 其中：Partitioner泛型参数为Key类型
        //      KeySelector泛型参数为value、key。
        dataSource.partitionCustom(
                // 根据key，设置如何对元素分区
                new Partitioner<String>() {
                    @Override
                    public int partition(String s, int i) {
                        int partition = random.nextInt(i);
                        // 此处设置：key==odd时发往第一个分区，其他元素发往0-2任意分区
                        if (s.equalsIgnoreCase("odd")) {
                            return 0;
                        } else {
                            return partition;
                        }
                    }
                },
                // 从元素中指定key。此处指定二元组第一个元素为key
                new KeySelector<Tuple2<String, Long>, String>() {
                    @Override
                    public String getKey(Tuple2<String, Long> stringLongTuple2) throws Exception {
                        return stringLongTuple2.f0;
                    }
                }).print();


        env.execute("Collection ");
    }
}
