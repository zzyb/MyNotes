package example.operator.state;

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


public class KeyedCheckPointedFunctionByRichFlatMap extends RichFlatMapFunction<Tuple4<String, String, String, Integer>, Tuple2<String, Integer>> implements CheckpointedFunction {
    // 记录哪一个分区
    private transient int subTaskIds;
    // 记录分区中的某个条件数量，此处为记录分区中数据积分大于1的数量
    private transient int nums;

    // 列表状态
    private transient ListState<Tuple2<String, Integer>> listState;
    // 列表缓存
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
    // 当请求检查点的快照时调用此方法。
    // 保存状态
    public void snapshotState(FunctionSnapshotContext context) throws Exception {
        //        context.getCheckpointId(); 检查点id
        //        context.getCheckpointTimestamp(); 检查点时间戳
        // 在状态初始化期间恢复的 ListState 保存在一个类变量中，以便将来在 snapshotState ()中使用。
        // 在这里，ListState 将清除前一个检查点包含的所有对象，然后填充我们想要检查点的新对象。
        listState.clear();
        for (Tuple2<String, Integer> value : listBuffer) {
            listState.add(value);
        }
    }

    @Override
    // 在分布式执行期间创建并行函数实例时调用该方法。 函数通常在此方法中设置其状态存储数据结构。
    // 初始化、恢复状态
    public void initializeState(FunctionInitializationContext context) throws Exception {
        ListStateDescriptor<Tuple2<String, Integer>> tuple2ListStateDescriptor = new ListStateDescriptor<Tuple2<String, Integer>>(
                "buffer-list",
                TypeInformation.of(new TypeHint<Tuple2<String, Integer>>() {
                })
        );

//        listState = getRuntimeContext().getListState(tuple2ListStateDescriptor); //错误 ！！！

//        listState = context.getOperatorStateStore().getListState(tuple2ListStateDescriptor);

        // keyBy
        // keyBy
        // keyBy
        // getKeyedStateStore()返回状态对象
        listState = context.getKeyedStateStore().getListState(tuple2ListStateDescriptor); // keyBy
        // isRestored ()方法来检查是否在故障后恢复。如果这是真的，也就是说我们正在恢复，那么就应用恢复逻辑。
        if (context.isRestored()) {
            for (Tuple2<String, Integer> value : listState.get()) {
                listBuffer.add(value);
            }
        }
    }
}
