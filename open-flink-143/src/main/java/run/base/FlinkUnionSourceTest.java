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

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import source.GenerateRecordSource;
import source.GenerateThreeDigitRecordSource;

/**
 * DataSource
 */
public class FlinkUnionSourceTest {
	public static void main(String[] args) throws Exception {

//		getExecutionEnvironment()
//		createLocalEnvironment()
		StreamExecutionEnvironment env = StreamExecutionEnvironment
				.getExecutionEnvironment();
//		.createRemoteEnvironment("3.7.191.174", 8088);

		DataStreamSource dataSource1 = env.addSource(new GenerateRecordSource());
		DataStreamSource dataSource2 = env.addSource(new GenerateThreeDigitRecordSource());
		DataStream union = dataSource1.union(dataSource2);
		union.print();

		env.execute("Collection ");
	}
}