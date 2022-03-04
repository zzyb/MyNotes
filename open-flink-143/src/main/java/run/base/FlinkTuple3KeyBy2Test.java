package run.base;/*
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
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import source.Tuple3Source;

/**
 * DataSource
 */
public class FlinkTuple3KeyBy2Test {
    public static void main(String[] args) throws Exception {

//		getExecutionEnvironment()
//		createLocalEnvironment()
        StreamExecutionEnvironment env = StreamExecutionEnvironment
                .getExecutionEnvironment();
//		.createRemoteEnvironment("3.7.191.174", 8088);

        DataStreamSource dataSource = env.addSource(new Tuple3Source());
//		dataSource.print();

        //KeySelector<IN,Key>
        KeyedStream keyedStream = dataSource.keyBy(new KeySelector<Tuple3<String, String, Long>, Tuple2<String, String>>() {
            @Override
            public Tuple2<String, String> getKey(Tuple3<String, String, Long> value) throws Exception {
                return new Tuple2<>(value.f0, value.f1);
            }
        });

        //三元组第三位求最大，下标从0开始。
        keyedStream.max(2).print();

//		15047167219;

        env.execute("Collection ");
    }
}
