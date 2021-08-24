package com.yber.java.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApplyToEitherTest {

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

      CompletableFuture<String> f3 = f1.applyToEither(f2, (value) -> {
          return "is :" + value;
      });

      try {
          System.out.println(f3.get());
      } catch (InterruptedException e) {
          e.printStackTrace();
      } catch (ExecutionException e) {
          e.printStackTrace();
      }

  }
}
