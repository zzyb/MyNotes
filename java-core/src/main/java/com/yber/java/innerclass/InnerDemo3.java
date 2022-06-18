package com.yber.java.innerclass;

import com.yber.java.innerclass.beans.Person_Locality;
import com.yber.java.innerclass.beans.Person_Locality2;

public class InnerDemo3 {
    public static void main(String[] args){
        // 对外部类创建一个实例；
        Person_Locality2 lili = new Person_Locality2();

        // 内部类在一个方法中，是一个局部内部类
        lili.start("vivi",17);
    }


}
