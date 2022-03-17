package source;

import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class StringWithTimeSource extends RichSourceFunction<String> {
    Random random = null;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String[] city = {"北京", "上海", "郑州", "广州", "香港", "唐山", "天津", "新郑"};


    @Override
    public void open(Configuration parameters) throws Exception {
        random = new Random();
    }

    @Override
    public void run(SourceContext sourceContext) throws Exception {
        for (long i = 1; i < 101; i++) {
            StringBuffer sb = new StringBuffer();

            Date now = new Date();
            long nowLong = now.getTime();
            String nowFormat = format.format(new Date(nowLong));

            String cityValue = city[random.nextInt(city.length)];
            StringBuffer result = sb.append(cityValue).append(",").append(nowFormat).append(",").append(nowLong);

            sourceContext.collect(result.toString());
            Thread.sleep(1000);
        }
    }

    @Override
    public void cancel() {

    }
}
