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

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import source.Tuple4WithTimeSource;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * DataSource
 */
public class FlinkTypes4WithTimeNoWatermarksTest {
	public static void main(String[] args) throws Exception {
		SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

		StreamExecutionEnvironment env = StreamExecutionEnvironment
				.getExecutionEnvironment().setParallelism(1);

		/*
		此方法在1.12已经废弃。当前不需要显示的声明出来。
		In Flink 1.12 the default stream time characteristic has been changed to TimeCharacteristic.EventTime,
		thus you don't need to call this method for enabling event-time support anymore.
		 */
//		env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
//		env.getConfig().setAutoWatermarkInterval(1000);// 每5s生成一次水位线。

		DataStreamSource dataSource = env.addSource(new Tuple4WithTimeSource());

		SingleOutputStreamOperator singleOutputStreamOperator = dataSource.assignTimestampsAndWatermarks(
		        WatermarkStrategy
						.<Tuple4<String, String, String, Integer>>noWatermarks()//水印追随时间戳。
		);

//        dataSource.print();

        singleOutputStreamOperator.keyBy(new KeySelector<Tuple4<String,String,String,Integer>,String>() {
			@Override
			public String getKey(Tuple4<String, String, String, Integer> value) throws Exception {
				return value.f1;
			}
		})
				// 采用处理时间，不需要水印推进
		.windowAll(TumblingProcessingTimeWindows.of(Time.milliseconds(5000)))
		.sum(3).print();


		env.execute("Collection ");
	}
}
