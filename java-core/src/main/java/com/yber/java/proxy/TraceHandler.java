package com.yber.java.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

public class TraceHandler implements InvocationHandler {
  private Object target;

  public TraceHandler(Object t) {
    target = t;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    // 不是 proxy
    // System.out.println(proxy);
    System.out.print(target);
    System.out.print(".");
    System.out.print(method.getName());
    System.out.print("(");
    System.out.print(Arrays.toString(args));
    System.out.print(")");
    System.out.println();
    /* 调用真正的方法 */
    // 不是 proxy,是target
    // return method.invoke(proxy,args);
    return method.invoke(target, args);
  }
}
