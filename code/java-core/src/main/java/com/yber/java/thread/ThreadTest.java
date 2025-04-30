package com.yber.java.thread;

public class ThreadTest {
  public static void main(String[] args) {

    Runnable r1 = () -> {
      for(int i=0;i<=99;i++){
        System.out.println( i + " From r1.");
        try {
          Thread.sleep(3000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    };

    Runnable r2 = () -> {
      for(int i=0;i<=99;i++){
        System.out.println( i + " From r2~");
        try {
          Thread.sleep(3000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    };

    Runnable r3 = new Runnable() {
      @Override
      public void run() {
        for(int i=0;i<=99;i++){
          System.out.println( i + " From r3 !");
          try {
            Thread.sleep(3000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    };

    new Thread(r1).start();
    new Thread(r2).start();
    new Thread(r3).start();
  }
}
