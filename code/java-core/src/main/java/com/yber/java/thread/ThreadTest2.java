package com.yber.java.thread;

public class ThreadTest2 implements Runnable {

    @Override
    public void run() {
        for(int i = 0;i<= 9;i++){
            System.out.println(i+" hello from ThreadTest2");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

  public static void main(String[] args) {
        System.out.println("begin~~~~~~~~~~~~~");
        Thread thread = new Thread(new ThreadTest2());

        for(int i = 0;i<=10;i++){
            System.out.println("*****************");
            System.out.println(thread.getState());
            if(i==5){
                thread.start();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        thread.interrupt();
        System.out.println(thread.getState());
  }
}
