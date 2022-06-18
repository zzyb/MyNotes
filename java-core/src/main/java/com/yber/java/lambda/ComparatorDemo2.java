package com.yber.java.lambda;

import com.yber.java.lambda.beans.Person;

import java.util.Arrays;
import java.util.Comparator;

public class ComparatorDemo2 {
  public static void main(String[] args) {
    //
    Person person1 = new Person("tom");
    Person person2 = new Person("zhangsan");
    Person person3 = new Person("lisi");
    Person person4 = new Person("wangwu");
    Person person5 = new Person("ai-t100");

    Person[] peoples = {person1,person2,person3,person4,person5};

    /**
     * Comparable是排序接口，若一个类实现了Comparable接口，就意味着“该类支持排序”。而Comparator是比较器，我们若需要控制某个类的次序，可以建立一个“该类的比较器”来进行排序。
     *
     * <p>　　Comparable相当于“内部比较器”，而Comparator相当于“外部比较器”。
     */
    Arrays.sort(
        peoples,
        Comparator.comparing(
            Person::getName, (t1, t2) -> Integer.compare(t1.length(), t2.length())));

    System.out.println(Arrays.toString(peoples));
  }



}
