package time;

import org.apache.flink.api.common.eventtime.Watermark;
import org.apache.flink.api.common.eventtime.WatermarkGenerator;
import org.apache.flink.api.common.eventtime.WatermarkOutput;
import org.apache.flink.api.java.tuple.Tuple4;

import java.text.SimpleDateFormat;


public class UseServerTimeGenerator implements WatermarkGenerator<Tuple4<String,String,String,Integer>> {

    private final long ex = 6000L;

    @Override
    public void onEvent(Tuple4<String, String, String, Integer> event, long eventTimestamp, WatermarkOutput output) {

    }

    @Override
    public void onPeriodicEmit(WatermarkOutput output) {
        long late6s = System.currentTimeMillis() - ex;
        output.emitWatermark(new Watermark(late6s));
//        //当周期性的调用此方法时，发出 当前机器 时间戳-6s 作为水位线。 // 最后减一表示发出这个时间之前一点的。
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(late6s));
    }
}
