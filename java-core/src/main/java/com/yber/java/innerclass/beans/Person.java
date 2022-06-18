package com.yber.java.innerclass.beans;

import java.util.Date;

/**
 * 包含一个内部类
 */
public class Person {

  private String name;
  private int age;

  public Person() {}

  public Person(String name, int age) {
    this.name = name;
    this.age = age;
  }

  @Override
  public String toString() {
    return "Person{" +
            "name='" + name + '\'' +
            ", age=" + age +
            '}';
  }

  public class Clock {
    private Date date;
    public Clock(){
      System.out.println("调用了无参构造器");
    }

    public Clock(Date date)  {
        this.date = date;
      System.out.println("调用了 {有参数}  构造器");
    }
    public void printAge() {
      // 对name 和 age 的引用类似于OuterClass.this.变量
      System.out.println("你好，" + Person.this.name +" , 你已经：" + Person.this.age +"了。");
      System.out.println("你好，" + name +" , 你已经：" + age +"了。");
    }
    public void printAgeWithTime() {
      // 对name 和 age 的引用类似于OuterClass.this.变量
      System.out.println("你好，" + Person.this.name +" , 你已经：" + Person.this.age +"了。");
      System.out.println("你好，" + name +" , 你已经：" + age +"了。");
      System.out.println("Time is : " + date.toString());
    }
  }
}
