package com.yber.java.proxy;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Random;

public class ProxyTest {
  public static void main(String[] args) {
    Object[] values = new Object[10000000];
    for (int i = 0; i < values.length; i++) {
      Integer value = i + 1;
      TraceHandler traceValue = new TraceHandler(value);
      Object proxy =
          Proxy.newProxyInstance(
              ClassLoader.getSystemClassLoader(), new Class[] {Comparable.class}, traceValue);
      values[i] = proxy;
    }

    Integer key = new Random().nextInt(values.length) + 1;

    int result = Arrays.binarySearch(values, key);

    if (result >= 0) System.out.println(values[result]);
  }
}
