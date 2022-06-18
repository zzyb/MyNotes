package com.yber.java.lambda;

import java.util.function.IntConsumer;

public class IntConsumerDemo {
  public static void main(String[] args) {
    //
    repeat(5, (i) -> System.out.println(i + "consumer"));
  }

  public static void repeat(int n, IntConsumer intConsumer){
    for (int i = 0; i < n; i++) {
      intConsumer.accept(i);
    }
  }

}
