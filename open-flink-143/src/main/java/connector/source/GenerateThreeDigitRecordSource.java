package connector.source;

import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

public class GenerateThreeDigitRecordSource extends RichSourceFunction<Long> {
    @Override
    public void run(SourceContext sourceContext) throws Exception {
        long source = 0L;
        while (source <= 100){
            source++;
            sourceContext.collect(source+100);
            Thread.sleep(1500);
        }
    }

    @Override
    public void cancel() {

    }
}
