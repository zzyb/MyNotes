package com.yber.java.lambda;

import com.yber.java.lambda.beans.Person;

import java.util.Date;
import java.util.function.Supplier;

public class SupplierDemo {
  public static void main(String[] args) {
    //
    Person person1 = new Person("tom");
    Person person2 = null;

    // person1不是null，不会创建Date对象。
    String first = isNull(person1, () -> new Date());
    // person2为null,此时才会调用lambda创建Date对象
    String second = isNull(person2, () -> new Date());

    System.out.println(first);
    System.out.println(second);
  }

  // Supplier懒计算
  public static String isNull(Person p, Supplier<Date> s){
    if(null == p){
      return s.get().toString();
    } else {
      return p.toString();
    }
  }


}
