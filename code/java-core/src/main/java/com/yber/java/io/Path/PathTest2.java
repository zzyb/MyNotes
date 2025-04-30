package com.yber.java.io.Path;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathTest2 {
  public static void main(String[] args) {
    Path path = Paths.get("/opt/kafka/log/server.log");
    System.out.println(path.toAbsolutePath());
    System.out.println(path.getParent());
    System.out.println(path.getFileName());
    System.out.println(path.getRoot());
  }
}
