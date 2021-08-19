package com.yber.java.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompletableFutureTest2
{
  public static void main(String[] args) {
    //
    ExecutorService executorService = Executors.newFixedThreadPool(3);
    CompletableFuture<String> future = new CompletableFuture<String>();

    executorService.execute(
        () -> {
          try {
            System.out.println("run 111");
            Thread.sleep(5000);
            System.out.println("end 111");
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          future.complete("need 5 second!!!");
        });
    executorService.execute(() ->{
      try {
        System.out.println("run 222");
        Thread.sleep(2000);
        System.out.println("end 222");
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      future.complete("only 2 second.");
    });

    try {
      System.out.println(future.get());
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }

    executorService.shutdown();
  }
}
