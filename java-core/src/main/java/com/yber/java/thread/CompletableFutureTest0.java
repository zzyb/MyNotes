package com.yber.java.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompletableFutureTest0 {
  public static void main(String[] args) {
    //
    ExecutorService executorService = Executors.newFixedThreadPool(3);
    CompletableFuture<String> future =
        CompletableFuture.supplyAsync(
            () -> {
              try {
                Thread.sleep(5000);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              return new String("hello 异步计算");
            },
            executorService);
    CompletableFuture<String> future2 =
        CompletableFuture.supplyAsync(
            () -> {
              try {
                Thread.sleep(5000);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              return new String("hello2 异步计算2");
            },
            executorService);

    try {
      String s1 = future.get();
      String s2 = future2.get();
      System.out.println(s1 + "\t" + s2);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }

    executorService.shutdown();
  }
}
