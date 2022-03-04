package run.time;/*
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

import org.apache.flink.api.common.eventtime.WatermarkGenerator;
import org.apache.flink.api.common.eventtime.WatermarkGeneratorSupplier;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.runtime.operators.util.AssignerWithPeriodicWatermarksAdapter;
import source.GenerateRecordSource;
import source.MultiRecordSource;
import source.MultiThreeDigitRecordSource;

import java.time.Duration;

/**
 * DataSource
 */
public class FlinkTypes2TimestampAssignerTest {
	public static void main(String[] args) throws Exception {

		StreamExecutionEnvironment env = StreamExecutionEnvironment
				.getExecutionEnvironment();

		/*
		此方法在1.12已经废弃。当前不需要显示的声明出来。
		In Flink 1.12 the default stream time characteristic has been changed to TimeCharacteristic.EventTime,
		thus you don't need to call this method for enabling event-time support anymore.
		 */
//		env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
		env.getConfig().setAutoWatermarkInterval(5000);// 每5s生成一次水位线。

		DataStreamSource dataSource = env.addSource(new MultiRecordSource());
//		dataSource.print();

//		dataSource.assignTimestampsAndWatermarks(new MyWatermark());

        dataSource.assignTimestampsAndWatermarks(
        		WatermarkStrategy
						.<Tuple2<String,Long>>forBoundedOutOfOrderness(Duration.ofSeconds(20)) // 水印 -- 有界无序水位线
						.withTimestampAssigner((event,timestamp) -> event.f1)	// 时间戳分配器 -- lambda获取时间戳;(可选的)
		);

        dataSource.print();



		env.execute("Collection ");
	}
}

//class MyWatermark implements WatermarkStrategy<Tuple2<String,Long>> {
//
//	@Override
//	public WatermarkGenerator<Tuple2<String, Long>> createWatermarkGenerator(WatermarkGeneratorSupplier.Context context) {
//		return null;
//	}
//}
