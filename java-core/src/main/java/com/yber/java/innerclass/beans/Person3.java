package com.yber.java.innerclass.beans;

import java.util.Date;

/**
 * 匿名内部类的实现
 */
public class Person3 {

  public Person3() {}

  public void start(String name,int age) {
    // 匿名内部类。（该类实现了Runnable接口，实现方法run在大括号中定义。）
//    Runnable r = new Runnable() {
      Runnable r = () -> { // lambda表达式
      Date today ;
      // 匿名内部类不能有构造器，但是可以有对象初始化块。
      {
        today = new Date();
      }
//      @Override // lambda 不再需要方法名
//      public void run() {// lambda 不再需要方法名
        System.out.println("匿名内部类 {lambda} 创建的");
        System.out.println("name = " + name);
        System.out.println("age = " + age);
        System.out.println("Date = " + today);
//      }// lambda 不再需要方法名
    };

    Thread thread = new Thread(r);
    thread.start();
  }


}
