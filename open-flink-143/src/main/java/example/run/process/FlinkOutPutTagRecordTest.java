package example.run.process;/*
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

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;
import example.connector.source.GenerateRecordSource;

/**
 * DataSource
 */
public class FlinkOutPutTagRecordTest {
	public static void main(String[] args) throws Exception {

//		getExecutionEnvironment()
//		createLocalEnvironment()
		StreamExecutionEnvironment env = StreamExecutionEnvironment
				.getExecutionEnvironment().setParallelism(3);
//		.createRemoteEnvironment("3.7.191.174", 8088);

		DataStreamSource dataSource = env.addSource(new GenerateRecordSource());
//		dataSource.print();

        // 创建一个侧输出流.
		OutputTag<Long> longOutputTag = new OutputTag<Long>("copySource"){};

		// 通过process算子进行复制流操作.
		SingleOutputStreamOperator process = dataSource.process(new ProcessFunction<Long, Long>() {
			@Override
			public void processElement(Long aLong, Context context, Collector<Long> collector) throws Exception {
				collector.collect(aLong);
				// 将数据收集到侧输出
				context.output(longOutputTag, aLong);
			}
		});

		// 从process获取复制的侧输出
		DataStream sideOutput = process.getSideOutput(longOutputTag);

		sideOutput.print();


		env.execute("Collection ");
	}
}
