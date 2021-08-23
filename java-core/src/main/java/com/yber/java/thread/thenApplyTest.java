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

    CompletableFuture<String> f2 =
        f1.thenApply(
            (f1Value) -> {
              try {
                Thread.sleep(3000);
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              if (f1Value.equalsIgnoreCase("a")) {
                System.out.println("is A/a");
                return "is A/a";
              } else {
                System.out.println("not Is A/a");
                return "not Is A/a";
              }
            });

    //    CompletableFuture<String> f3 =
    //        f1.thenApplyAsync(
    //            (f1Value) -> {
    //              try {
    //                Thread.sleep(3000);
    //              } catch (InterruptedException e) {
    //                e.printStackTrace();
    //              }
    //              if (f1Value.equalsIgnoreCase("a")) {
    //                System.out.println("is A/a");
    //                return "is A/a";
    //              } else {
    //                  System.out.println("not Is A/a");
    //                  return "not Is A/a";
    //              }
    //            });
    for (int i = 0; i < 10; i++) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      System.out.println("waiting");
    }

    try {
      System.out.println("1 " + f2.get());
      //          System.out.println("1 "+f3.get());
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }

    executorService.shutdown();
  }
}
