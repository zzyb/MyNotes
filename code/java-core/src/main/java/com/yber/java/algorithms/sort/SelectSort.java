package com.yber.java.algorithms.sort;

import java.util.Arrays;

/**
 * @describe
 * 选择排序
 */
public class SelectSort {
    public static void main(String[] args){
        String[] unSort = {"b","a","e","h","y","k","c","w"};
        for(int i=0;i<unSort.length;i++){
            for(int j=i+1;j< unSort.length;j++){
                if(unSort[i].compareTo(unSort[j]) > 0){
                   String temp = unSort[i];
                   unSort[i] = unSort[j];
                   unSort[j] = temp;
                }
            }
        }
        System.out.println(Arrays.toString(unSort));
    }

    public static void selectSort(Comparable[] a){
        int size = a.length;

    }

}
