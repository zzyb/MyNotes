package com.yber.java.innerclass;

import com.yber.java.innerclass.beans.ArrayAlg;

import java.util.Arrays;

public class InnerDemo7 {
  public static void main(String[] args) {
    int[] numbers = new int[20];
    for (int i = 0; i < numbers.length; i++) {
      numbers[i] = (int) (100 * Math.random());
    }
    System.out.println(Arrays.toString(numbers));
    ArrayAlg.Pair maxmin = ArrayAlg.maxmin(numbers);
    System.out.println(maxmin.getMax());
    System.out.println(maxmin.getMin());
  }
}
