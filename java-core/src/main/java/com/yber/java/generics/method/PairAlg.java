package com.yber.java.generics.method;

public class PairAlg {

  // static 后面是'变量修饰符', 然后是返回类型
  public static <T> T getMiddle(T... a) {
    return a[a.length / 2];
  }

  public static void main(String[] args) {
    // 可以把具体类型包括在尖括号中，也可以省略（编译器推断）
    final String middle = PairAlg.<String>getMiddle("hello", "Q", "nice");
    final String middle_2 = PairAlg.getMiddle("hello", "Q", "nice");

    System.out.println(middle);
    System.out.println(middle_2);
  }
}
