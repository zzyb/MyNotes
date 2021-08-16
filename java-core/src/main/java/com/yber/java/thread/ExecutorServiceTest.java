package com.yber.java.thread;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.*;

public class ExecutorServiceTest {
  public static void main(String[] args) throws ExecutionException, InterruptedException {
    //

    Callable<Integer> c1 =
        () -> {
        int j = 0;
        for (int i =0;i<3;i++){
            j = new Random().nextInt(100);
            System.out.println(j);
            Thread.sleep(1000);
        }
          return j;
        };

      Runnable r1 =
              () -> {
                  int j = 0;
                  for (int i =0;i<3;i++){
                      j = new Random().nextInt(100);
                      System.out.println(j);
                      try {
                          Thread.sleep(1000);
                      } catch (InterruptedException e) {
                          e.printStackTrace();
                      }
                  }
              };

      Runnable r2 =
              () -> {
                  int j = 0;
                  for (int i =0;i<3;i++){
                      j = new Random().nextInt(100);
                      System.out.println(j);
                      try {
                          Thread.sleep(1000);
                      } catch (InterruptedException e) {
                          e.printStackTrace();
                      }
                  }
              };
      ExecutorService executorService = Executors.newCachedThreadPool();

//      Future<Integer> submit = executorService.submit(c1);
      Future<ArrayList<String>> submit = executorService.submit(r2, new ArrayList<String>());


      System.out.println("---->" + submit.get().size());

      executorService.shutdown();
  }
}
