package example.operator.single;

import org.apache.flink.api.common.functions.MapFunction;

public class Num2StringMapFunction implements MapFunction<Long,String> {
    @Override
    public String map(Long aLong) throws Exception {
        String add = "JHR-> ";
        String result = new StringBuilder().append(add).append(aLong).toString();
        return result;
    }
}
