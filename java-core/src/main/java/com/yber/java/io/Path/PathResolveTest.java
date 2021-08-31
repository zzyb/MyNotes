package com.yber.java.io.Path;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathResolveTest {
  public static void main(String[] args) {
    Path path = Paths.get("/opt/kafka/log");
    Path path2 = Paths.get("log-cleaner.log");
    Path path3 = Paths.get("/opt/kafka/log/controller.log");
    System.out.println(path.toString());
    System.out.println(path2.toString());
    System.out.println(path.resolve(path2));
    System.out.println(path.resolve(path3));
  }

}
