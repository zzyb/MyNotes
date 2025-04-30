package com.yber.java.lambda;

import com.yber.java.lambda.beans.Person;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConstructorDemo2 {
  public static void main(String[] args) {
    //
    ArrayList<String> names = new ArrayList<>(Arrays.asList("tom","jack","lili"));

    Stream<Person> personStream = names.stream().map(Person::new);

    // 流的toArray方法可以转换为一个Object数组。通过传入(Person[]::new) 获取的一个Person[]数组
    Person[] people = personStream.toArray(Person[]::new);

    System.out.println(Arrays.toString(people));
  }
}
