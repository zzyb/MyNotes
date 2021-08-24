package com.yber.java.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThenAcceptBothTest {

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

      try {
          f1.thenAcceptBoth(
              f2,
              (value1, value2) -> {
                System.out.println(value1);
                System.out.println(value2);
              }).get(); // 此处的get很重要。获取执行的结果。如果没有，则可能造成没有输出。
      } catch (InterruptedException e) {
          e.printStackTrace();
      } catch (ExecutionException e) {
          e.printStackTrace();
      }
  }
}
