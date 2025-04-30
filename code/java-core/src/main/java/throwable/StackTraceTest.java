package throwable;

import java.util.Scanner;

public class StackTraceTest {
  public static int factorial(int n) {
    System.out.println("factorial(" + n + ")");
    final StackWalker instance = StackWalker.getInstance();
    instance.forEach(System.out::println);
    int r;
    if (n <= 1) {
      r = 1;
    } else {
      r = n * factorial(n - 1);
    }
    System.out.println("return : " + r);
    return r;
  }

  public static void main(String[] args) {
    //
    try (Scanner scanner = new Scanner(System.in)) {
      System.out.println("请输入一个数字");
      final int n = scanner.nextInt();
      factorial(n);
    }
  }
}
