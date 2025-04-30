package example.connector.sink.custom;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.io.PrintWriter;

/**
 * 将数据输出到构造方法传入的文件名的文件中
 */
public class CustomSink extends RichSinkFunction<String> {

    public CustomSink() {
    }

    public CustomSink(String fileName) {
        this.fileName = fileName;
    }

    private String fileName = "";
    private PrintWriter printWriter;

    @Override
    // 开始前，调用
    public void open(Configuration parameters) throws Exception {
        printWriter = new PrintWriter(fileName);
    }

    @Override
    // 在该方法中接收数据并操作输出
    public void invoke(String value, Context context) throws Exception {
        printWriter.println("----------");
        printWriter.println(value);
        printWriter.flush();
    }

    @Override
    // 结束时，调用。
    public void close() throws Exception {
        printWriter.close();
    }
}
