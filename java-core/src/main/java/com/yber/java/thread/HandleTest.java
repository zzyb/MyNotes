package com.yber.java.thread;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HandleTest {
  public static void main(String[] args) {
    ExecutorService executorService = Executors.newFixedThreadPool(3);

    // 异步计算，得到ABCD
    CompletableFuture<String> f1 =
        CompletableFuture.supplyAsync(
            () -> {
              return "ABCD";
            });

    // 异步计算f1结束后，根据得到的值value，返回V类型CompletableFuture<V>类型。
    CompletableFuture<Serializable> handle =
        f1.handle(
            (value, ex) -> {
              if (ex != null) {
                System.out.println("exception:" + ex);
                return ex;
              } else {
                System.out.println("value:" + value);
                return value;
              }
            });

    try {
      System.out.println(handle.get());
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
  }
}
