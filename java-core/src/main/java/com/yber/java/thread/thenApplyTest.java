package com.yber.java.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class thenApplyTest {
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

    CompletableFuture<Integer> f2 =
        f1.thenApply(
            (f1Value) -> {
              try {
                Thread.sleep(3000);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              if (f1Value.equalsIgnoreCase("a")) {
                System.out.println("is A/a");
                return 1;
              } else {
                System.out.println("not Is A/a");
                return 0;
              }
            });

    try {
      System.out.println(f2.get());
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }

    executorService.shutdown();
  }
}
