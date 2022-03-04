package time;

import org.apache.flink.api.common.eventtime.Watermark;
import org.apache.flink.api.common.eventtime.WatermarkGenerator;
import org.apache.flink.api.common.eventtime.WatermarkOutput;
import org.apache.flink.api.java.tuple.Tuple4;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class BoundedOutOfOrdernessGenerator implements WatermarkGenerator<Tuple4<String,String,String,Integer>> {

    private final long ex = 6000L;
    private long maxTimestamp ;
    SimpleDateFormat format = new SimpleDateFormat("YYYY-HH-MM HH:mm:ss.SSS");

    @Override
    public void onEvent(Tuple4<String, String, String, Integer> event, long eventTimestamp, WatermarkOutput output) {
        try {
            // 获取数据流遇到的最大事件时间的时间戳。
            maxTimestamp = Math.max(format.parse(event.f2).getTime(),eventTimestamp);
        } catch (ParseException e) {
        }
    }

    @Override
    public void onPeriodicEmit(WatermarkOutput output) {
        //当周期性的调用此方法时，发出最大遇到的最大时间戳-6s 作为水位线。 // 最后减一表示发出这个时间之前一点的。
        output.emitWatermark(new Watermark(maxTimestamp - ex -1));
    }
}
