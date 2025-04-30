package com.yber.java.innerclass.beans;

import java.util.Date;

/**
 *  静态方法获取类名
 */
public class Person6 {

  public Person6() {}

  public void startNotStatic(String name, int age) {
    System.out.println(name);
    System.out.println(age);
    System.out.println(this.getClass());
  }
  public static void startStatic(String name,int age) {
    System.out.println(name);
    System.out.println(age);
    System.out.println(new Object(){}.getClass());
    // new Object(){} 建立Object匿名子类的一个匿名对象。
    // getEnclosingClass 获取外围类（也就是包含这个静态方法的类）
    System.out.println(new Object(){}.getClass().getEnclosingClass());
  }


}
