package source;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

public class MultiRecordSource extends RichSourceFunction<Tuple2<String,Long>> {
    @Override
    public void run(SourceContext sourceContext) throws Exception {
        long source = 0L;
        String key = "";
        while (source <= 100){
            source++;
            if(source%2==0){
                key = "even";
            } else {
                key = "odd";
            }
            Tuple2<String, Long> value = new Tuple2<>();
            value.setFields(key,source);
            sourceContext.collect(value);
            Thread.sleep(1500);
        }
    }

    @Override
    public void cancel() {

    }
}
