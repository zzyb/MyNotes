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

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import example.connector.source.MultiRecordSource;

/**
 * DataSource
 */
public class FlinkBroadCastRecordTest {
	public static void main(String[] args) throws Exception {

//		getExecutionEnvironment()
//		createLocalEnvironment()
		StreamExecutionEnvironment env = StreamExecutionEnvironment
				.getExecutionEnvironment().setParallelism(3);
//		.createRemoteEnvironment("3.7.191.174", 8088);

		DataStreamSource dataSource = env.addSource(new MultiRecordSource());

		// 数据均匀的发往各个分区。
//		dataSource.print();

		// 后续算子的每个分区都能收到该条记录！！
        dataSource.broadcast().print();


		env.execute("Collection ");
	}
}
