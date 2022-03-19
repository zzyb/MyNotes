package connector.source;

import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.util.Random;

public class Tuple3Source extends RichSourceFunction<Tuple3<String, String, Long>> {
    Random random = null;
    Tuple3 value = null;
    String [] city = {"北京","上海","郑州"};
    String [] type = {"A","B"};

    @Override
    public void open(Configuration parameters) throws Exception {
        value = new Tuple3();
        random = new Random();


    }

    @Override
    public void run(SourceContext sourceContext) throws Exception {
        long num = 0L;
        for (long i = 0; i < 100; i++) {
            value.setFields(city[random.nextInt(city.length)],type[random.nextInt(type.length)],++num);
            sourceContext.collect(value);
            Thread.sleep(1500);

        }
    }

    @Override
    public void cancel() {

    }
}
