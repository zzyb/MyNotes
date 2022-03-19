package connector.source;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class Tuple2TwoStringWithTimeSource extends RichSourceFunction<Tuple2<String,String>> {
    Random random = null;
    Tuple2<String,String> t2;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String[] city = {"北京", "上海", "郑州", "广州", "香港", "唐山", "天津", "新郑"};


    @Override
    public void open(Configuration parameters) throws Exception {
        random = new Random();
        t2 = new Tuple2<String,String>();
    }

    @Override
    public void run(SourceContext sourceContext) throws Exception {
        for (long i = 1; i < 51; i++) {
            StringBuffer sb = new StringBuffer();

            Date now = new Date();
            long nowLong = now.getTime();
            String nowFormat = format.format(new Date(nowLong));

            String cityValue = city[random.nextInt(city.length)];
            StringBuffer timeString = sb.append(nowFormat).append(",").append(nowLong);
            t2.setFields(cityValue,timeString.toString());

            sourceContext.collect(t2);
            Thread.sleep(1000);
        }
    }

    @Override
    public void cancel() {

    }
}
