package com.yber.java.thread;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class CallableTest {
  public static void main(String[] args) throws ExecutionException, InterruptedException {
    //
    Callable<Integer> r =
        () -> {
        int sum = 0;
          for (int i = 0; i <= 3; i++) {
            System.out.println(i);
            sum += i;
          }
          return sum;
        };

    FutureTask<Integer> integerFutureTask = new FutureTask<>(r);
    Thread thread = new Thread(integerFutureTask);
    thread.start();

    Integer integer = integerFutureTask.get();
    System.out.println("----> " + integer);
  }
}
