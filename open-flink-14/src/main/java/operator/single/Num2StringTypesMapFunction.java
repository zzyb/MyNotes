package operator.single;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.typeutils.ResultTypeQueryable;

// 显示的提供返回的类型信息。
public class Num2StringTypesMapFunction implements MapFunction<Long,String> , ResultTypeQueryable<String> {
    @Override
    public String map(Long aLong) throws Exception {
        String add = "";
        if(aLong >= 10){
            add = "JHR-> (大于10)： ";
        } else {
            add = "JHR-> (10以内)： ";
        }
        String result = new StringBuilder().append(add).append(aLong).toString();
        return result;
    }

    @Override
    //实现接口方法。
    public TypeInformation<String> getProducedType() {
        return Types.STRING;
    }
}
