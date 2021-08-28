package com.yber.java.internet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ServerSocketThreadTest {
  public static void main(String[] args) {
    try {
      /**
       * use "telnet localhost 7788"
       */
      ServerSocket server = new ServerSocket(7788);
      Socket inComing = server.accept();
      InputStream ips = inComing.getInputStream();
      OutputStream ops = inComing.getOutputStream();

      Scanner in = new Scanner(ips, StandardCharsets.UTF_8);
      PrintWriter out = new PrintWriter(ops, true);

      out.println("hello ! Entry bye to exit.");

      boolean down = false;
      while (!down && in.hasNextLine()){
        String line = in.nextLine();
        out.println("Echo: "+ line);
        if(line.trim().equalsIgnoreCase("bye")){
          down = true;
        }
      }

     inComing.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
