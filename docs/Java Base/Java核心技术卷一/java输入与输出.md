# 输入与输出

## 输入/输出流

`输入流（I）`：可以从其中**读取**一个字节序列的对象。

`输出流（O）`：可以向其中**写入**一个字节序列的对象。



- 抽象类 InputStream和OutputStream 构成了输入输出（I/O）类层次结构的基础。
- 抽象类Reader和Writer中继承出来了一个专门用于<u>处理Unicode字符的</u>单独的类层次结构。
  - 基于两个字节的Char值的（Unicode码元），而不是byte值。



### 读写字节

- `InputStream`类有一个抽象方法：

```java
abstract int read()
//读入一个字节，并返回读入的字节，或者在遇到输入源结尾时返回-1。
```

- `OutputStream`类的一个抽象方法：

```java
abstract void write(int b)
//向某个输出位置写出一个字节。

//如果有一个字节数组，也可以一次性的输出它们。
byte[] values = ...;
out.write(values);
```

- read 和 write 方法在执行时将被阻塞，直至字节确实被读入或写出。

- **available**方法可以检查当前可读入的字节数量。

  ```java
  public int available() throws IOException
  返回可以从此输入流读取（或跳过）的字节数的估计值，而不会因下一次调用此输入流的方法而阻塞。下一次调用可能是同一个线程或另一个线程。单次读取或跳过这么多字节不会阻塞，但可能读取或跳过更少的字节。
  请注意，虽然 InputStream 的某些实现会返回流中的总字节数，但许多不会。使用此方法的返回值来分配用于保存此流中所有数据的缓冲区永远是不正确的。
  
  如果此输入流已通过调用 close() 方法关闭，则此方法的子类实现可能会选择抛出 IOException。
  
  The available method for class InputStream always returns 0.
  InputStream 类的可用方法始终返回 0。
  
  这个方法应该被子类覆盖。
  
  Returns:
  估计可以从此输入流读取（或跳过）而不会阻塞的字节数，或者在到达输入流末尾时为 0。
  Throws:
  IOException - if an I/O error occurs.
  ```

  ```java
  //不会被阻塞的读取！！！
  int bytesAvailable = in.available();
  if(bytesAvailable > 0){
    var data = new bytes[bytesAvailable];
    in.read(data);
  }
  ```

  

