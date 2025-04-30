package example.connector.source.join;

import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class Tuple4WithTimeBSource extends RichSourceFunction<Tuple4<String, String, String, Integer>> {
    Random random = null;
    Tuple4 value = null;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String[] city = {"北京", "上海", "郑州"};
    String[] type1 = {"购买", "出售"};
    String[] type2 = {"回收", "出租"};

    @Override
    public void open(Configuration parameters) throws Exception {
        value = new Tuple4();
        random = new Random();
    }

    @Override
    public void run(SourceContext sourceContext) throws Exception {
        for (long i = 1; i < 101; i++) {
            long current = System.currentTimeMillis();
            // 对当前时间微调 10秒
            long interval = random.nextInt(10) * 1000L;
            long reviseIncrease = current + interval;
            long reviseReduce = current - interval;

            // 随机增加或减少
            String lateTime = format.format(
                    new Date(
                            random.nextInt(2) == 0 ? reviseIncrease : reviseReduce
                    )
            );
            value.setFields(
                    city[random.nextInt(city.length)], //城市
//                    type[random.nextInt(type.length)], //类型
                    Math.abs(interval) <= 5000L ? type1[random.nextInt(type1.length)] : type2[random.nextInt(type2.length)],
                    lateTime,
                    1
            );
            sourceContext.collect(value);
            System.out.println("B -- " + value);
            Thread.sleep(1000L);
        }
    }

    @Override
    public void cancel() {

    }
}
