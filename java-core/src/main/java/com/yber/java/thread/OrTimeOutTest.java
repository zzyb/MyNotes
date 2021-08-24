package com.yber.java.thread;

import java.util.concurrent.*;

public class OrTimeOutTest {
    /**
     * in Java 9!!!
     * @param args
     */
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        //异步计算，得到ABC
        CompletableFuture<String> f1 = CompletableFuture.supplyAsync(()->{
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "ABCD";
        });

        //超时，抛出TimeoutException异常.
        CompletableFuture<String> f2 = f1.orTimeout(3,TimeUnit.SECONDS)
                .thenApply((value) -> {
                    return value;
                });

        try {
            System.out.println(f2.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
