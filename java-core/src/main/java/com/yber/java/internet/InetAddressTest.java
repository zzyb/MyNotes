package com.yber.java.internet;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

public class InetAddressTest {
  public static void main(String[] args) {
    //
    try {
      InetAddress baiduHost = InetAddress.getByName("www.google.com");
      InetAddress[] baiduAllHost = InetAddress.getAllByName("www.google.com");
      InetAddress localHost = InetAddress.getLocalHost();
      InetAddress localHost2 = InetAddress.getByName("localhost");
      InetAddress realLocalHost = InetAddress.getLoopbackAddress();

      System.out.println(baiduHost);
      System.out.println(Arrays.toString(baiduAllHost));
      System.out.println(localHost);
      System.out.println(localHost2);
      System.out.println(realLocalHost);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
