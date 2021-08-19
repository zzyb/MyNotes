package com.yber.java.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompletableFutureTest
{
  public static void main(String[] args) {
    //
    ExecutorService executorService = Executors.newFixedThreadPool(3);
    CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return new String("hello 异步计算");
    }, executorService);
      CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
          try {
              Thread.sleep(5000);
          } catch (InterruptedException e) {
              e.printStackTrace();
          }
          return new String("hello 异步计算");
      }, executorService);

    future.whenComplete(
        (s, t) -> {
          if (t == null) System.out.println(s);
          else System.out.println("no");
        });
      future2.whenComplete(
              (s, t) -> {
                  if (t == null) System.out.println(s);
                  else System.out.println("no");
              });

      executorService.shutdown();
  }
}
