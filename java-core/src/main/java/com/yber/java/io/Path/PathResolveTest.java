package com.yber.java.io.Path;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathResolveTest {
  public static void main(String[] args) {
    Path path = Paths.get("/opt/kafka/log");
    Path path2 = Paths.get("server.log");
    Path path3 = Paths.get("/opt/kafka/log/controller.log");
    System.out.println(path.toAbsolutePath());
    System.out.println(path2.toAbsolutePath());
    Path resolve = path.resolve(path2);
    System.out.println(resolve.toAbsolutePath());
    Path resolve2 = path.resolve(path3);
    System.out.println(resolve2.toAbsolutePath());
  }
}
