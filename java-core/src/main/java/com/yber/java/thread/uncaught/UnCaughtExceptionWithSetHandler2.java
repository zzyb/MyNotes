package com.yber.java.thread.uncaught;

public class UnCaughtExceptionWithSetHandler2 {
  public static void main(String[] args) {

    ThreadWithEx2 r = new ThreadWithEx2();
    try {
      Thread thread = new Thread(r);

      // 在线程启动前设置！！！
      Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler2());

      thread.start();
      /**
       * 1、线程run方法不能抛出检查型异常；
       * 2、非检查型异常（Error/RunTimeException）会导致线程终止。
       *
       * 非检查型异常，会在线程死亡前，传递到一个用于处理未捕获异常的处理器！
       * 非检查型异常，会在线程死亡前，传递到一个用于处理未捕获异常的处理器！
       * 非检查型异常，会在线程死亡前，传递到一个用于处理未捕获异常的处理器！
       */
    } catch (Exception e) {
      System.out.println("-----Exception-----");
    }
  }
}

class ThreadWithEx2 implements Runnable {
  @Override
  public void run() {
    System.out.println(4 / 2);
    System.out.println(4 / 0); // 会抛RuntimeException异常
    System.out.println(4 / 3);
  }
}

class ExceptionHandler2 implements Thread.UncaughtExceptionHandler {

  @Override
  public void uncaughtException(Thread t, Throwable e) {
    System.out.println("get it : " + e.getMessage());
  }
}