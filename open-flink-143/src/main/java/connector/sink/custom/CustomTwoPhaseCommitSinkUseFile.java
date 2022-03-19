package connector.sink.custom;

import org.apache.flink.api.common.state.CheckpointListener;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.sink.TwoPhaseCommitSinkFunction;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/** */
public class CustomTwoPhaseCommitSinkUseFile
    extends TwoPhaseCommitSinkFunction<Tuple2<String, String>, String, Void> implements CheckpointListener {

  private PrintWriter printWriter;

  public CustomTwoPhaseCommitSinkUseFile(
      TypeSerializer<String> transactionSerializer, TypeSerializer<Void> contextSerializer) {
    super(transactionSerializer, contextSerializer);
  }

  @Override
  protected String beginTransaction() throws Exception {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd--HH_mm_ss");
    Date current = new Date();
    String currentString = simpleDateFormat.format(current);
    int indexOfThisSubtask = getRuntimeContext().getIndexOfThisSubtask();
    // 通过获取当前时间和任务索引号，组成唯一的事务文件路径
    String transactionFile = currentString + "-" + String.valueOf(indexOfThisSubtask);

    String tmpPathString = System.getProperty("java.io.tmpdir");
    String useTransactionFile = tmpPathString + File.separator + transactionFile;

    printWriter = new PrintWriter(useTransactionFile);
    System.out.println(useTransactionFile);
    return useTransactionFile;
  }

  @Override
  protected void invoke(String s, Tuple2<String, String> value, Context context) throws Exception {
    printWriter.println(value.toString());
    printWriter.flush();
  }

  @Override
  protected void preCommit(String s) throws Exception {
    printWriter.close();
  }

  @Override
  protected void commit(String s) {
    String tmpDir = System.getProperty("java.io.tmpdir");

    Path path = Paths.get(s);
    boolean exists = Files.exists(path);
    if (exists) {
      String[] split = s.split("/");
      Path targetPath = Paths.get("/Users/zhangyanbo/Downloads", split[split.length-1]);
      try {
        Files.move(path, targetPath);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  protected void abort(String s) {
    String tmpDir = System.getProperty("java.io.tmpdir");

    Path path = Paths.get(s);
    boolean exists = Files.exists(path);
    if (exists) {
      try {
        Files.delete(path);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
