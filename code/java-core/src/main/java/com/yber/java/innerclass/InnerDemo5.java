package com.yber.java.innerclass;

import com.yber.java.innerclass.beans.Person2;
import com.yber.java.innerclass.beans.Person3;

public class InnerDemo5 {
    public static void main(String[] args){
        // 匿名内部类 lambda 表达式
        Person3 person3 = new Person3();
        person3.start("jack",20);
    }


}
