package com.yber.java.innerclass.beans;

/**
 * 静态内部类：当内部类不需要访问外围类的对象，就应该使用静态内部类
 */
public class ArrayAlg {

  public static class Pair {
    private int max;
    private int min;

    public Pair(int max, int min) {
      this.max = max;
      this.min = min;
    }

    public int getMax() {
      return max;
    }

    public int getMin() {
      return min;
    }
  }

  public static Pair maxmin(int[] nums) {
    int max = Integer.MIN_VALUE;
    int min = Integer.MAX_VALUE;

    for (int num : nums) {
      if (num > max) max = num;
      if (num < min) min = num;
    }

    return new Pair(max, min);
  }
}
