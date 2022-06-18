package com.yber.java.base;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

public class FileDemo {
  public static void main(String[] args) throws IOException {
    Scanner scanner = new Scanner(Path.of("/users/zhangyanbo/install.sh"));
    while (scanner.hasNext()) {
      System.out.println(scanner.next());
    }
  }
}
