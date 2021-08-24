package com.yber.java.thread;

import java.util.concurrent.*;

public class StaticAnyOfTest {

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
              System.out.println("f3 is down");
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
              System.out.println("f2 is down");
              return "-XYZ";
            });

    CompletableFuture<Object> voidAllF3 = CompletableFuture.anyOf(f1, f2);

    try {
      voidAllF3.get();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
  }
}
