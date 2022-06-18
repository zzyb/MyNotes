package com.yber.java.lambda;

import com.yber.java.lambda.beans.Person;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class RunnableDemo {
  public static void main(String[] args) {
    //
    repeat(10, () -> System.out.println("hello!!!"));
  }

  public static void repeat(int n,Runnable r1){
    for (int i = 0; i < n; i++) {
      r1.run();
    }
  }

}
