package example.operator.keyvalue;

import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;

public class EvenOddKeyByFunction implements KeySelector<Tuple2<String,Long>,String> {


    @Override
    public String getKey(Tuple2<String, Long> stringLongTuple2) throws Exception {
        return stringLongTuple2.f0;
    }
}
