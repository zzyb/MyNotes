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
import source.Tuple3IntSource;

/**
 * DataSource
 */
public class FlinkTuple3IntKeyByTest {
	public static void main(String[] args) throws Exception {

//		getExecutionEnvironment()
//		createLocalEnvironment()
		StreamExecutionEnvironment env = StreamExecutionEnvironment
				.getExecutionEnvironment();
//		.createRemoteEnvironment("3.7.191.174", 8088);

		DataStreamSource dataSource = env.addSource(new Tuple3IntSource());
//		dataSource.print();

		KeyedStream keyedStream = dataSource.keyBy(new KeySelector<Tuple3<Integer, Integer, Integer>, Integer>() {
			@Override
			public Integer getKey(Tuple3<Integer, Integer, Integer> value) throws Exception {
				int max = Math.max(value.f0, value.f1);
				System.out.println("key is : " + max);
				return max;//三元组第一位和第二位比较，谁大谁做key。（即通过计算得到键）
			}
		});

		keyedStream.sum(2).print();


		env.execute("Collection ");
	}
}
