package com.yber.java.thread.uncaught;

public class UnCaughtExceptionDemo {
  public static void main(String[] args) {
    //
    Runnable r =
        () -> {
          // 注意：不是在run方法内try-catch
          //          try {
          System.out.println(4 / 2);
          System.out.println(4 / 0); // 会抛RuntimeException异常
          System.out.println(4 / 3);
          //          } catch (Exception e) {
          //            System.out.println("Catch Exception !!!");
          //          }
        };

    try {
      Thread thread = new Thread(r);
      thread.start();
        /**
         * 1、线程run方法不能抛出检查型异常；
         * 2、非检查型异常（Error/RunTimeException）会导致线程终止。
         *
         * 非检查型异常，会在线程死亡前，传递到一个用于处理未捕获异常的处理器！
         * 非检查型异常，会在线程死亡前，传递到一个用于处理未捕获异常的处理器！
         * 非检查型异常，会在线程死亡前，传递到一个用于处理未捕获异常的处理器！
         */
        // 主线程捕获不到多线程中抛出的异常（非检查型异常）
        // 主线程捕获不到多线程中抛出的异常（非检查型异常）
        // 主线程捕获不到多线程中抛出的异常（非检查型异常）,这段代码会报错，catch中的代码不会执行。
    } catch (Exception e) {
      System.out.println("-----Exception-----");
    }
  }
}
