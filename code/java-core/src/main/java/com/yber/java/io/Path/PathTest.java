package com.yber.java.io.Path;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathTest {
  public static void main(String[] args) {
    Path path = Paths.get("/opt", "zookeeper.out");
    System.out.println(path.toAbsolutePath());
    boolean flag1 = path.endsWith("out");  // false
    boolean flag2 = path.endsWith("zookeeper.out"); //true
    System.out.println(flag1);
    System.out.println(flag2);
  }
}
