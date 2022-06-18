package com.yber.java.lambda;

import com.yber.java.lambda.beans.Person;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.function.Supplier;

public class ComparatorDemo {
  public static void main(String[] args) {
    //
    Person person1 = new Person("tom");
    Person person2 = new Person("zhangsan");
    Person person3 = new Person("lisi");
    Person person4 = new Person("wangwu");

    Person[] peoples = {person1,person2,person3,person4};

    Arrays.sort(peoples, Comparator.comparing(Person::getName));

    System.out.println(Arrays.toString(peoples));
  }



}
