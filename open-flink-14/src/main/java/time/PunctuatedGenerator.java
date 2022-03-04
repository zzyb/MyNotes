package time;

import org.apache.flink.api.common.eventtime.Watermark;
import org.apache.flink.api.common.eventtime.WatermarkGenerator;
import org.apache.flink.api.common.eventtime.WatermarkOutput;
import org.apache.flink.api.java.tuple.Tuple4;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class PunctuatedGenerator implements WatermarkGenerator<Tuple4<String,String,String,Integer>> {

    @Override
    public void onEvent(Tuple4<String, String, String, Integer> event, long eventTimestamp, WatermarkOutput output) {
        // 此处使用了一个粗糙的逻辑，如果数据中心包含“北京”，将当前事件的时间作为水印发出。
        if(event.toString().contains("北京")){
            output.emitWatermark(new Watermark(eventTimestamp));
        }
    }

    @Override
    public void onPeriodicEmit(WatermarkOutput output) {
        // 定点水位线不需要该方法。
    }
}
