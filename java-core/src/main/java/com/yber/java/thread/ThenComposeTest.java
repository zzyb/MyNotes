package com.yber.java.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThenComposeTest {
  public static void main(String[] args) {
    ExecutorService executorService = Executors.newFixedThreadPool(3);

    // 异步计算，得到ABCD
    CompletableFuture<String> f1 =
        CompletableFuture.supplyAsync(
            () -> {
              return "ABCD";
            });

    //        异步计算f1结束后，根据得到的值value，返回V类型CompletableFuture<V>类型。
    CompletableFuture<Integer> f3 =
        f1.thenCompose(
            (value) -> {
              return CompletableFuture.supplyAsync(
                  () -> {
                    if (value.equalsIgnoreCase("abc")) {
                      return 123;
                    } else {
                      return 100;
                    }
                  });
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
