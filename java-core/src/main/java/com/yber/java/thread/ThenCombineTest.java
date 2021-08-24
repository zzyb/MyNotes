package com.yber.java.thread;

import java.util.concurrent.*;

public class ThenCombineTest {

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        //异步计算，得到ABCD
        CompletableFuture<String> f1 = CompletableFuture.supplyAsync(()->{
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "ABCD";
        });

        CompletableFuture<String> f2 = CompletableFuture.supplyAsync(()->{
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "-XYZ";
        });

        CompletableFuture<String> f3 = f1.thenCombine(f2, (value1, value2) -> {
            return value1 + value2;
        });

        CompletableFuture<String> f4 = f3.thenApply((value) -> {
            return value;
        });

        try {
            System.out.println(f4.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
