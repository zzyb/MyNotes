package com.yber.java.io.Path;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ResolveSiblingTest {
  public static void main(String[] args) {
    Path path = Paths.get("/opt/kafka/log");
    Path path2 = Paths.get("conf");
    System.out.println(path);
    System.out.println(path.resolveSibling(path2));
  }

}
