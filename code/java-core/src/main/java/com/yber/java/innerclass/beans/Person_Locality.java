package com.yber.java.innerclass.beans;

import java.util.Date;

/**
 * 包含一个{局部}内部类
 */
public class Person_Locality {

  private String name;
  private int age;

  public Person_Locality() {}

  public Person_Locality(String name, int age) {
    this.name = name;
    this.age = age;
  }

  public void start() {
    // 局部内部类
    class Clock {
      private Date date;
      public Clock() {
        System.out.println("调用了无参构造器");
      }
      public Clock(Date date) {
        this.date = date;
        System.out.println("调用了 {有参数}  构造器");
      }

      public void printAge() {
        // 对name 和 age 的引用类似于OuterClass.this.变量
        System.out.println(
            "你好，" + Person_Locality.this.name + " , 你已经：" + Person_Locality.this.age + "了。");
        System.out.println("你好，" + name + " , 你已经：" + age + "了。");
      }

      public void printAgeWithTime() {
        // 对name 和 age 的引用类似于OuterClass.this.变量
        System.out.println(
                "你好，" + Person_Locality.this.name + " , 你已经：" + Person_Locality.this.age + "了。");
        System.out.println("你好，" + name + " , 你已经：" + age + "了。");
        System.out.println("Time is : " + date.toString());
      }
    }
    // 该方法内部最后，创建了一个内部类的对象，并调用方法（内部类方法直接使用了外部类的参数）
    Clock clock = new Clock(new Date());
    clock.printAge();
    clock.printAgeWithTime();
  }

  @Override
  public String toString() {
    return "Person{" + "name='" + name + '\'' + ", age=" + age + '}';
  }

}
