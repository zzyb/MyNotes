package com.yber.java.lambda;

import java.util.function.Predicate;

public class PredicateDemo {
  public static void main(String[] args) {
    //
      String[] str = {"tom","lili","jack"};
      Predicate<String> stringPredicate = s -> s.length() == 3;

    for (String s : str) {
        System.out.println(stringPredicate.test(s));
    }

  }

}
