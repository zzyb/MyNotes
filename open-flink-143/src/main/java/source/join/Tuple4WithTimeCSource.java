package source.join;

import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class Tuple4WithTimeCSource extends RichSourceFunction<Tuple4<String, String, String, Integer>> {
    Random random = null;
    Tuple4 value = null;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String[] city = {"北京", "上海", "郑州"};
    String[] type = {"购买", "出售"};

    @Override
    public void open(Configuration parameters) throws Exception {
        value = new Tuple4();
        random = new Random();
    }

    @Override
    public void run(SourceContext sourceContext) throws Exception {
        for (long i = 1; i < 101; i++) {
            String nowFormat = format.format(new Date());
            value.setFields(
                    city[random.nextInt(city.length)], //城市
                    type[random.nextInt(type.length)], //类型
                    nowFormat,
                    1
            );
            sourceContext.collect(value);
//            System.out.println("C ## " + value);
            Thread.sleep(3000L);
        }
    }

    @Override
    public void cancel() {

    }
}
