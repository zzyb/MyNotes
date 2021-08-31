package com.yber.java.io.Path;

import java.nio.file.Path;
import java.nio.file.Paths;

public class RelativizeTest {
  public static void main(String[] args) {
    Path path = Paths.get("/opt/kafka/log/server.log");
    Path path2 = Paths.get("/opt/kafka/conf");
    Path path3 = path.relativize(path2);
    System.out.println(path3);
    System.out.println(path.resolve(path3));
    /**
       ../../conf
       /opt/kafka/log/server.log/../../conf
     * */
  }
}
