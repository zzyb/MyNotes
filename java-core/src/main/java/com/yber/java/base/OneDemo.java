package com.yber.java.base;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;

public class OneDemo {
  public static void main(String[] args) throws IOException {
    System.out.println("hello , world !");
    OneDemo oneDemo = new OneDemo();
    Console console = System.console(); // bug
    String name = console.readLine("User name:");
    char[] chars = console.readPassword("passWord:");

    System.out.println(name);
    System.out.println(chars.toString());
  }
  private String readLine(String format, Object... args) throws IOException {
    if (System.console() != null) {
      return System.console().readLine(format, args);
    }
    System.out.print(String.format(format, args));
    BufferedReader reader = new BufferedReader(new InputStreamReader(
            System.in));
    return reader.readLine();
  }

  private char[] readPassword(String format, Object... args)
          throws IOException {
    if (System.console() != null)
      return System.console().readPassword(format, args);
    return this.readLine(format, args).toCharArray();
  }
}
