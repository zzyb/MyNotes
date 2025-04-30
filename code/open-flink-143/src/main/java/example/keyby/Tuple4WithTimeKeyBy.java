package example.keyby;

import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple4;

public class Tuple4WithTimeKeyBy implements KeySelector<Tuple4<String,String,String,Integer>,String> {
    @Override
    public String getKey(Tuple4<String, String, String, Integer> value) throws Exception {
        return value.f0;
    }
}
