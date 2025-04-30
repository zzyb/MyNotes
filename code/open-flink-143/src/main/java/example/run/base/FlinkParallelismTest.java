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

import example.operator.single.Num2StringMapFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import example.connector.source.GenerateRecordSource;

/**
 * DataSource
 */
public class FlinkParallelismTest {
	public static void main(String[] args) throws Exception {

		StreamExecutionEnvironment env = StreamExecutionEnvironment
				.getExecutionEnvironment().setParallelism(1);// 设置执行环境并行度为1
		int defaultPara = env.getParallelism();// 获取当前env并行度

		DataStreamSource dataSource = env.addSource(new GenerateRecordSource());
//		dataSource.print();

		SingleOutputStreamOperator map = dataSource
				.map(new Num2StringMapFunction())
				.setParallelism(defaultPara * 3);// 设置map算子并行度为默认并行度的3倍，即3个并行度。
		map.print();


		env.execute("Collection ");
	}
}
