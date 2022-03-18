package source.custom;

import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * 可重置的数据源
 * 可重置的数据源
 * 可重置的数据源
 */
public class StringWithTimeCheckPointedCustomSource extends RichSourceFunction<String> implements CheckpointedFunction {
    boolean isRunning = true;
    Random random = null;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String[] city = {"北京", "上海", "郑州", "广州", "香港", "唐山", "天津", "新郑"};

    private Long count = 1L;
    private ListState<Long> state;


    @Override
    public void open(Configuration parameters) throws Exception {
        random = new Random();
    }

    @Override
    // 只会在flink中被调用一次！！！
    // 只会在flink中被调用一次！！！
    // 只会在flink中被调用一次！！！
    // 运行在单独的线程中
    // 运行在单独的线程中
    // 运行在单独的线程中
    public void run(SourceContext sourceContext) throws Exception {
        while (isRunning && count < 101) {
            // 输出和状态更新是原子的,检查点的时候锁住输出.
            Object checkpointLock = sourceContext.getCheckpointLock();
            synchronized (checkpointLock) {
                Date now = new Date();
                long nowLong = now.getTime();
                String nowFormat = format.format(new Date(nowLong));
                String cityValue = city[random.nextInt(city.length)];

                StringBuffer sb = new StringBuffer();
                String result = sb.append(count).append(",").append(cityValue).append(",").append(nowFormat).append(",").append(nowLong).toString();

                sourceContext.collect(result);

                count++;
                Thread.sleep(1000);
            }
        }
    }

    @Override
    // flink会在应用被取消或关闭时调用cancel
    // flink会在应用被取消或关闭时调用cancel
    // flink会在应用被取消或关闭时调用cancel
    public void cancel() {
        isRunning = false;
    }

    @Override
    // 生成检查点的时候调用
    public void snapshotState(FunctionSnapshotContext context) throws Exception {
        // 先清理状态
        state.clear();
        // 然后存储当前状态要记录的值
        state.add(count);
    }

    @Override
    // 初始化 或者 错误恢复时调用
    public void initializeState(FunctionInitializationContext context) throws Exception {
        // 获取状态存储值
        state = context.getOperatorStateStore().getListState(new ListStateDescriptor<Long>("state", Types.LONG));
        // 将状态值恢复到程序中
        Iterable<Long> longs = state.get();
        if (null == longs || !longs.iterator().hasNext()) {
            count = 1L;
        } else {
            count = longs.iterator().next();
        }
    }
}
