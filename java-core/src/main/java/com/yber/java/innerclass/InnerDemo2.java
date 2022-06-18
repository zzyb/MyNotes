package com.yber.java.innerclass;

import com.yber.java.innerclass.beans.Person;
import com.yber.java.innerclass.beans.Person_Locality;

import java.util.Date;

public class InnerDemo2 {
    public static void main(String[] args){
        // 对外部类创建一个实例；
        Person_Locality lili = new Person_Locality("lili", 18);

        // 内部类在一个方法中，是一个局部内部类
        lili.start();
    }


}
