package operator.single;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

public class CopyByNumFlatMapFunction implements FlatMapFunction<Long,Long> {
    @Override
    public void flatMap(Long aLong, Collector<Long> collector) throws Exception {
        for (long i = 0; i < aLong; i++) {
            collector.collect(aLong);
        }
    }
}
