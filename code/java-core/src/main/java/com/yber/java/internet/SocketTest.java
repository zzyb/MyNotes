package com.yber.java.internet;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SocketTest {
  public static void main(String[] args) {
    try {
      Socket socket = new Socket();
      // Socket socket = new Socket("time-a.nist.gov", 13);
      // 设置连接超时时间
      socket.connect(new InetSocketAddress("time-a.nist.gov", 13),1000);

      InputStream inputStream = socket.getInputStream();

      String s = new String(inputStream.readAllBytes());

      System.out.println(s);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
