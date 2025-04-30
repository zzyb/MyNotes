package example.connector.sink.custom;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

/**
 * 数据汇阐释更新，并在对应值不存在时执行插入方式upsert(update + insert)操作
 */
public class CustomIdempotentSink extends RichSinkFunction<Tuple2<String, String>> {

    Connection conn;
    PreparedStatement updatePs;
    PreparedStatement insertPs;

    @Override
    // 开始前，调用
    public void open(Configuration parameters) throws Exception {
        // 这里准备了 一个 表-city[name,info] 其中name为主键
        Class.forName("com.mysql.cj.jdbc.Driver");
        conn = DriverManager.getConnection("jdbc:mysql://192.168.1.42:3306/flink", "root", "123456");
        insertPs = conn.prepareStatement("insert into flink.city (name,info) values(?,?)");
        updatePs = conn.prepareStatement("update flink.city set info = ? where name =?");
    }

    @Override
    // 在该方法中接收数据并操作输出
    public void invoke(Tuple2<String, String> value, Context context) throws Exception {
        // 注意update语句结构,不要搞错了顺序
        // 首先尝试更新
        updatePs.setString(2, value.f0);// name
        updatePs.setString(1, value.f1);// info
        updatePs.execute();
        int updateCount = updatePs.getUpdateCount();
        // 如果没有更新结果,转换为插入
        if (updateCount == 0) {
            insertPs.setString(1, value.f0);
            insertPs.setString(2, value.f1);
            insertPs.execute();
        }
    }

    @Override
    // 结束时，调用。
    public void close() throws Exception {
        updatePs.close();
        insertPs.close();
        conn.close();
    }
}
