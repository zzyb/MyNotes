package source;

import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class Tuple4WithTimeProcessMoreKeySource extends RichSourceFunction<Tuple4<String, String, String,Integer>> {
    Random random = null;
    Tuple4 value = null;
    SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
    String [] city = {"北京","上海","郑州"};
    String [] type = {"购买","出售"};

    long maxLong = 0L;
    String maxFormat = "";

    @Override
    public void open(Configuration parameters) throws Exception {
        value = new Tuple4();
        random = new Random();
    }

    @Override
    public void run(SourceContext sourceContext) throws Exception {
        for (long i = 1; i < 101; i++) {

            Date now = new Date();
            long nowLong = now.getTime();
            long lateLong = now.getTime() - 1000L * 11;
            String nowFormat = format.format(new Date(nowLong));
            String lateFormat = format.format(new Date(lateLong));

            // 每5条数据，模拟一条迟到数据，迟到时间为11s
            String realData = (i % 5 != 0) ? nowFormat : lateFormat;
            // 传递当前最大的时间
            if(i % 5 != 0){
                maxFormat = nowFormat;
                maxLong = nowLong;
            }

            value.setFields(
                    city[random.nextInt(city.length)], //城市
                    type[random.nextInt(type.length)], //类型
                    realData,
                    1
            );
            Thread.sleep(500);
            sourceContext.collect(value);
            Thread.sleep(500);
        }
    }

    @Override
    public void cancel() {

    }
}
