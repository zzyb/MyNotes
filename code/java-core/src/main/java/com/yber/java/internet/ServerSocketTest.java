package com.yber.java.internet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ServerSocketTest {
  public static void main(String[] args) {

    /** use "telnet localhost 7788" */
    ServerSocket server = null;
    try {
      server = new ServerSocket(7788);
      int i = 1;
      while (true) {
        Socket inComing = server.accept();
        System.out.println("client " + i);
        ThreadedEchoHandler client = new ThreadedEchoHandler(inComing);
        Thread thread = new Thread(client);
        thread.start();
        i++;

      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
class ThreadedEchoHandler implements Runnable {
  private Socket incoming;

  public ThreadedEchoHandler() {}

  public ThreadedEchoHandler(Socket incoming) {
    this.incoming = incoming;
  }

  @Override
  public void run() {
    try {
      InputStream ips = incoming.getInputStream();
      OutputStream ops = incoming.getOutputStream();

      Scanner in = new Scanner(ips, StandardCharsets.UTF_8);
      PrintWriter out = new PrintWriter(ops, true);

      out.println("hello ! Entry bye to exit.");

      boolean down = false;
      while (!down && in.hasNextLine()) {
        String line = in.nextLine();
        out.println("Echo: " + line);
        if (line.trim().equalsIgnoreCase("bye")) {
          down = true;
        }
      }

      incoming.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
