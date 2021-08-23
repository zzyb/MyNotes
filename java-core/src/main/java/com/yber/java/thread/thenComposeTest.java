package com.yber.java.thread;

import java.util.concurrent.*;

public class thenComposeTest {
  public static void main(String[] args) {
    //
    ExecutorService executorService = Executors.newFixedThreadPool(3);

    CompletableFuture<String> f1 =
        CompletableFuture.supplyAsync(
            () -> {
              try {
                Thread.sleep(5000);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              System.out.println("A is OK");
              return "A";
            },
            executorService);



    for (int i = 0; i < 10; i++) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      System.out.println("waiting");
    }

    executorService.shutdown();
  }
}
