package com.yber.java.lambda;

import com.yber.java.lambda.beans.Person;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConstructorDemo {
  public static void main(String[] args) {
    //
    ArrayList<String> names = new ArrayList<>(Arrays.asList("tom","jack","lili"));

    Stream<Person> personStream = names.stream().map(Person::new);

    List<Person> collect = personStream.collect(Collectors.toList());

    System.out.println(Arrays.toString(collect.toArray()));
  }
}
