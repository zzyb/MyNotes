package com.yber.java.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class thenAcceptTest {
  public static void main(String[] args) {
    //
    ExecutorService executorService = Executors.newFixedThreadPool(3);

    CompletableFuture<String> f1 = CompletableFuture.supplyAsync(()->{
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return "A";
    },executorService);

    f1.thenAccept(
        (f1Value) -> {
          if (f1Value.equalsIgnoreCase("a")) {
            System.out.println("is A/a");
          } else {
              System.out.println("not Is A/a");
          }
        });

    executorService.shutdown();

  }
}
