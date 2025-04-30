package com.yber.java.io.Path;

import java.nio.file.Path;
import java.nio.file.Paths;

public class NormalizeTest {
  public static void main(String[] args) {
    Path path = Paths.get("/opt/kafka/log/server.log/../../conf");
    System.out.println(path.normalize());
  }
}
