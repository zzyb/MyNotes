package operator.single;

import org.apache.flink.api.common.functions.FilterFunction;

public class EvenNumFilterFunction implements FilterFunction<Long> {

    @Override
    public boolean filter(Long aLong) throws Exception {
        if(aLong % 2 == 0){
            return true;
        } else {
            return false;
        }
    }
}
