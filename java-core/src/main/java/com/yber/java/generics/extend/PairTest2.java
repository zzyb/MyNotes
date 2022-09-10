package com.yber.java.generics.extend;

import com.yber.java.generics.cls.Pair;

import java.time.LocalDate;

public class PairTest2 {
  public static void main(String[] args) {
    //
    LocalDate[] birthdays = {
            LocalDate.of(1995,3,8),
            LocalDate.of(2001,3,7),
            LocalDate.of(1995,12,25)
    };

    final Pair<LocalDate> minmax = ArrayAlg.minmax(birthdays);
    System.out.println(minmax.toString());
  }
}
