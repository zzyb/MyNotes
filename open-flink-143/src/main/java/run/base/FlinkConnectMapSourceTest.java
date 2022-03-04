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

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.ConnectedStreams;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoMapFunction;
import source.GenerateRecordSource;
import source.GenerateThreeDigitRecordSource;

/**
 * DataSource
 */
public class FlinkConnectMapSourceTest {
	public static void main(String[] args) throws Exception {

//		getExecutionEnvironment()
//		createLocalEnvironment()
		StreamExecutionEnvironment env = StreamExecutionEnvironment
				.getExecutionEnvironment();
//		.createRemoteEnvironment("3.7.191.174", 8088);

        // 自定义数据源：从1开始的、按1递增的数据源。
		DataStreamSource dataSource1 = env.addSource(new GenerateRecordSource());
		// 自定义数据源：从101开始的、按1递增的数据源。
		DataStreamSource dataSource2 = env.addSource(new GenerateThreeDigitRecordSource());

		// 连接两个数据源
		ConnectedStreams connect = dataSource1.connect(dataSource2);

		// 通过CoMapFunction分别对连接起来的两个流数据进行处理。
		SingleOutputStreamOperator connectMap = connect.map(new CoMapFunction<Long, Long, String>() {
			@Override
			public String map1(Long aLong) throws Exception {
				return Math.abs(aLong) <=5 ? "5以内。": "超出范围";
			}

			@Override
			public String map2(Long aLong) throws Exception {
				return Math.abs(aLong) <=110 ? "110以内。": "超出范围";
			}
		});

		connectMap.print();

		env.execute("Collection ");
	}
}
