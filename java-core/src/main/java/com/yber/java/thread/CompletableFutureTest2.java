package com.yber.java.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompletableFutureTest2 {
  public static void main(String[] args) {
    //
    ExecutorService executorService = Executors.newFixedThreadPool(3);
    CompletableFuture<String> future = new CompletableFuture<String>();

    executorService.execute(
        () -> {
          try {
            System.out.println("run 111");
            Thread.sleep(5000);
            // 得到其中一个结果后，可以利用这个信息(isDown)停止另一个工作。
            boolean done = future.isDone();
            if (done) {
              // 如果工作已经完成，什么都不做即可
            } else {
              System.out.println("end 111");
            }
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          future.complete("need 5 second!!!");
        });
    executorService.execute(
        () -> {
          try {
            System.out.println("run 222");
            Thread.sleep(2000);
            // 得到其中一个结果后，可以利用这个信息(isDown)停止另一个工作。
            boolean done = future.isDone();
            if (done) {
              // 如果工作已经完成，什么都不做即可
            } else {
              System.out.println("end 222");
            }
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          future.complete("only 2 second.");
        });

    try {
      // 得到最快的结果，同时，慢的将执行到底，非停止！
      System.out.println(future.get());
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }

    executorService.shutdown();
  }
}
