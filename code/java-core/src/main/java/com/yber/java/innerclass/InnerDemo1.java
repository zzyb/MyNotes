package com.yber.java.innerclass;

import com.yber.java.innerclass.beans.Person;

import java.util.Date;

public class InnerDemo1 {
    public static void main(String[] args){
        // 对外部类创建一个实例；
        Person tom = new Person("tom", 12);

        // 外部类实例.new 内部类构造器() 创建内部类的引用.
        // 引用内部类 OuterClass.InnerClass
        Person.Clock clock = tom.new Clock();
        clock.printAge();

        // 外部类实例.new 内部类构造器(xxx) 创建内部类的引用.
        // 引用内部类 OuterClass.InnerClass
        Person.Clock clock_yc = tom.new Clock(new Date());
        clock_yc.printAgeWithTime();


    }


}
