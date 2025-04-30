package example.connector.source;

import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.util.Random;

public class Tuple3IntSource extends RichSourceFunction<Tuple3<Integer,Integer,Integer>> {
    Random random = null;
    Tuple3 value = null;
    int [] city = {100,200,300,400,500};
    int [] city_copy = {101,202,303,404,505};

    @Override
    public void open(Configuration parameters) throws Exception {
        value = new Tuple3();
        random = new Random();


    }

    @Override
    public void run(SourceContext sourceContext) throws Exception {
        for (long i = 0; i < 100; i++) {
            value.setFields(city[random.nextInt(city.length)],city_copy[random.nextInt(city.length)],1);
            sourceContext.collect(value);
            Thread.sleep(1500);

        }
    }

    @Override
    public void cancel() {

    }
}
