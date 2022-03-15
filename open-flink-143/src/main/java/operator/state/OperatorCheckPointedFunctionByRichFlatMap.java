package operator.state;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;


public class OperatorCheckPointedFunctionByRichFlatMap extends RichFlatMapFunction<Tuple4<String, String, String, Integer>, Tuple2<String, Integer>> implements CheckpointedFunction {
    private transient int subTaskIds;
    private transient int nums;

    private transient ListState<Tuple2<String, Integer>> listState;
    private List<Tuple2<String, Integer>> listBuffer;

    @Override
    public void open(Configuration parameters) throws Exception {
        subTaskIds = getRuntimeContext().getIndexOfThisSubtask();
        nums = 0;
        listBuffer = new ArrayList<>();
    }

    @Override
    public void flatMap(Tuple4<String, String, String, Integer> value, Collector<Tuple2<String, Integer>> out) throws Exception {
        if (value.f3 > 1) {
            nums++;
        }
        Tuple2<String, Integer> t2 = new Tuple2<>();
        t2.setFields(String.valueOf(subTaskIds), nums);
        listBuffer.add(t2);
        out.collect(t2);
    }

    @Override
    public void snapshotState(FunctionSnapshotContext context) throws Exception {
        listState.clear();
        for (Tuple2<String, Integer> value : listBuffer) {
            listState.add(value);
        }
    }

    @Override
    public void initializeState(FunctionInitializationContext context) throws Exception {
        ListStateDescriptor<Tuple2<String, Integer>> tuple2ListStateDescriptor = new ListStateDescriptor<Tuple2<String, Integer>>(
                "buffer-list",
                TypeInformation.of(new TypeHint<Tuple2<String, Integer>>() {
                })
        );
//        listState = getRuntimeContext().getListState(tuple2ListStateDescriptor); //错误 ！！！
        listState = context.getOperatorStateStore().getListState(tuple2ListStateDescriptor);
        if (context.isRestored()) {
            for (Tuple2<String, Integer> value : listState.get()) {
                listBuffer.add(value);
            }
        }
    }
}
