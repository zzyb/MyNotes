package com.yber.java.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AcceptEitherTest {

  public static void main(String[] args) {
    ExecutorService executorService = Executors.newFixedThreadPool(3);

    // 异步计算，得到ABCD
    CompletableFuture<String> f1 =
        CompletableFuture.supplyAsync(
            () -> {
              try {
                Thread.sleep(4000);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              return "ABCD";
            });

    CompletableFuture<String> f2 =
        CompletableFuture.supplyAsync(
            () -> {
              try {
                Thread.sleep(2000);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              return "-XYZ";
            });

    CompletableFuture<Void> voidF3 =
        f1.acceptEither(
            f2,
            (value) -> {
              System.out.println(value);
            });
    try {
      voidF3.get(); // 此处的get很重要。获取执行的结果。如果没有，则可能造成没有输出。
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
  }
}
