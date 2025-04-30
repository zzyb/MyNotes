package com.yber.java.generics.cls;

public class PairAlg {
  public static Pair<String> minmax(String[] arrays) {
    if (arrays == null || arrays.length == 0) {
      return null;
    }

    String min = arrays[0];
    String max = arrays[0];
    for (String array : arrays) {
      if(min.compareTo(array)<0){
        min = array;
      }
      if(max.compareTo(array)>0){
        max = array;
      }
    }

    return new Pair<>(min, max);
  }
}
