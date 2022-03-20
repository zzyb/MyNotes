package operator.async;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;

import java.sql.*;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncFunction2Mysql
    extends RichAsyncFunction<Tuple2<String, String>, Tuple3<String, String, String>> {
    // 创建数据库连接、线程池、以及返回结果类型用到的Tuple3
  private ExecutorService serversPoll;
  private transient Connection conn;
  private Tuple3<String, String, String> t3;

  @Override
  // open中初始化线程池、数据库连接
  public void open(Configuration parameters) throws Exception {
    serversPoll = Executors.newFixedThreadPool(3);

    t3 = new Tuple3<>();

    Class.forName("com.mysql.cj.jdbc.Driver");
    conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/weibo", "root", "root");
  }

  @Override
  // 此处定义异步操作，并通过ResultFuture返回。
  // 注意：不要在asyncInvoke中发送阻塞请求，也不要在方法中等待请求完成！！！
  public void asyncInvoke(
      Tuple2<String, String> value, ResultFuture<Tuple3<String, String, String>> resultFuture)
      throws Exception {
      // 在future中定义异步操作。（这里模拟一个比较长的请求【1-睡眠2s，2-count查询】）
    CompletableFuture<String> future =
        CompletableFuture.supplyAsync(
            () -> {
              String count = "";
              try {
                PreparedStatement select =
                    conn.prepareStatement("select count(*) from weibo.dc_dwd_hot_title;");
                select.execute();
                Thread.sleep(2000L);
                ResultSet resultSet = select.getResultSet();
                if (resultSet.next()) {
                  count = resultSet.getString(1);
                }
              } catch (SQLException e) {
                e.printStackTrace();
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              return count;
            },
            serversPoll);

    // 这里采用whenComplete来回调结果。（也可以采用其他的方法，具体见java并发编程。）
    future.whenComplete(
        (s, e) -> {
          if (null == e) {
            t3.setFields(value.f0, value.f1, s);
            resultFuture.complete(Collections.singletonList(t3));
          } else {
            resultFuture.completeExceptionally(e);
          }
        });
  }

  @Override
  public void timeout(
      Tuple2<String, String> input, ResultFuture<Tuple3<String, String, String>> resultFuture)
      throws Exception {}

  @Override
  public void close() throws Exception {
    conn.close();
    serversPoll.shutdown();
  }
}
