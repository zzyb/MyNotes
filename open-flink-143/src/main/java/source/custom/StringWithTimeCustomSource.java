package source.custom;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class StringWithTimeCustomSource extends RichSourceFunction<String> {
    boolean isRunning = true;
    Random random = null;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String[] city = {"北京", "上海", "郑州", "广州", "香港", "唐山", "天津", "新郑"};


    @Override
    public void open(Configuration parameters) throws Exception {
        random = new Random();
    }

    @Override
    public void run(SourceContext sourceContext) throws Exception {
        long count = 1L;
        while (isRunning && count < 101) {
            count++;
            StringBuffer sb = new StringBuffer();

            Date now = new Date();
            long nowLong = now.getTime();
            String nowFormat = format.format(new Date(nowLong));
            String cityValue = city[random.nextInt(city.length)];
            String result = sb.append(cityValue).append(",").append(nowFormat).append(",").append(nowLong).toString();

            sourceContext.collect(result);

            Thread.sleep(1000);
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
    }
}
