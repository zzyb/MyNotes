package com.yber.java.generics.cls;

public class PairTest1 {
  public static void main(String[] args) {
    //
      String[] words = {"tom","jack","joe","keep","lili","mask"};
      final Pair<String> minmax = PairAlg.minmax(words);

    System.out.println(minmax.toString());
  }
}
