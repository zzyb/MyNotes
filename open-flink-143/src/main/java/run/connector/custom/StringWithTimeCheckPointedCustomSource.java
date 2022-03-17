package run.connector.custom;

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
        long count = 1L;
        while (isRunning && count < 101) {
            count++;
            StringBuffer sb = new StringBuffer();

            Date now = new Date();
            long nowLong = now.getTime();
            String nowFormat = format.format(new Date(nowLong));
            String cityValue = city[random.nextInt(city.length)];
            String result = sb.append(count).append(",").append(cityValue).append(",").append(nowFormat).append(",").append(nowLong).toString();

            sourceContext.collect(result);

            Thread.sleep(1000);
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
    public void snapshotState(FunctionSnapshotContext context) throws Exception {

    }

    @Override
    public void initializeState(FunctionInitializationContext context) throws Exception {

    }
}
