package com.jhr.thread;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExceptionallyTest {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        //异步计算，得到ABCD
        CompletableFuture<String> f1 = CompletableFuture.supplyAsync(()->{
            return "ABCD";
        });


//        异步计算f1结束后，如果出现异常，则得到一个假值abcd。
        CompletableFuture<String> f2 = f1.exceptionally((ex) -> {
            return "abcd";
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
