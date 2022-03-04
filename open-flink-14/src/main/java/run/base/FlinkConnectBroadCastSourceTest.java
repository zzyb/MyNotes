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

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.ConnectedStreams;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoFlatMapFunction;
import org.apache.flink.util.Collector;
import source.MultiRecordSource;
import source.MultiThreeDigitRecordSource;

/**
 * DataSource
 */
public class FlinkConnectBroadCastSourceTest {
	public static void main(String[] args) throws Exception {

//		getExecutionEnvironment()
//		createLocalEnvironment()
		StreamExecutionEnvironment env = StreamExecutionEnvironment
				.getExecutionEnvironment().setParallelism(3);
//		.createRemoteEnvironment("3.7.191.174", 8088);

        // 自定义数据源：从1开始的、按1递增的数据源。
		DataStreamSource dataSource1 = env.addSource(new MultiRecordSource());
		// 自定义数据源：从101开始的、按1递增的数据源。
		DataStreamSource dataSource2 = env.addSource(new MultiThreeDigitRecordSource());

		// 连接两个流，并对后一个流进行广播。后续的每一个算子实例都会包含被广播的流，
		ConnectedStreams connect = dataSource1.connect(dataSource2.broadcast());

		// 通过CoMapFunction分别对连接起来的两个流数据进行处理。
		SingleOutputStreamOperator connectFlatMap = connect.flatMap(new CoFlatMapFunction<Tuple2<String, Long>, Tuple2<String, Long>, String>() {
			@Override
			public void flatMap1(Tuple2<String, Long> value, Collector<String> collector) throws Exception {
				StringBuffer sb = new StringBuffer();
				sb.append("Source-1: ")
						.append(value.f0)
						.append(": "+ String.valueOf(value.f1 <=10?"平稳":"增大"))
						.append(value.f1);
				collector.collect(sb.toString());
			}

			@Override
			public void flatMap2(Tuple2<String, Long> value, Collector<String> collector) throws Exception {
				StringBuffer sb = new StringBuffer();
				sb.append("Source-2: ")
						.append(value.f0)
						.append(": "+ String.valueOf(value.f1 <=110?"平稳":"增大"))
						.append(value.f1);
				collector.collect(sb.toString());
			}
		});

		connectFlatMap.print();


		env.execute("Collection ");
	}
}
