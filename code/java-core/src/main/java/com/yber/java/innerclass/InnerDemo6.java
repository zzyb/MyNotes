package com.yber.java.innerclass;

import com.yber.java.innerclass.beans.Person3;
import com.yber.java.innerclass.beans.Person6;

public class InnerDemo6 {
    public static void main(String[] args){
        Person6 person6 = new Person6();
        person6.startNotStatic("非静态方法",100);
        Person6.startStatic("静态方法(类名调用)",200);
    }


}
