# 输入与输出

## 完整的流家族

![image-20210709213308621](./java输入与输出.resource/流家族1.png)

![image-20210709213447113](./java输入与输出.resource/流家族2.png)

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

- close方法会关闭输入输出流来释放掉十分有限的系统资源。

  - 同时还会冲刷该输出流的缓冲区：所有临时置于缓冲区中，以便更大的包的形式传递的字节在关闭输出流时都将被送出。

- flush方法：人为的冲刷这些输出。





**InputStream 和 OutputStream 可以读写单个字节或字节数组。**

**Reader 和 Writer 的子类可以读写Unicode文本。**



## 组合输入输出流过滤器

- Java使用了一种非常灵巧的机制来分离职责：

  **某些输入流**可以从<u>文件或者其他更外部</u>的位置上获取字节；而**其它输入流**可以<u>将字节组装到更有用的数据结构</u>中。

```java
//读取文件中的流：
FileInputStream fin = new FileInputStream("out.txt");
//传递给DataInputStream
DataInputStream dis = new DataInputStream(fin);

int i = dis.readInt();
System.out.println(i);


//out.txt文件：1234567890
//输出：825373492
//readInt() 读取 4 个字节，但 1234 读取为 49、50、51、52 ，根据 DataInput.readInt() 规范产生 825373492 int。
```

- 通过嵌套过滤器来添加更多功能

```java
//例如：输入流默认不被缓冲区缓存，每对read调用都会请求操作系统发送一个字节。相比之下，请求一个数据块并置于缓冲区中更高效。
DataInputStream dis =
  new DataInputStream(
  new BufferedInputStream(
    new FileInputStream("out.txt"))
);
```

- 当有多个输入流链接在一起时，你需要跟踪各个中介输入流。
  - 读入和回推时可回推输入流仅有的方法，如果希望可以预览并且可以读入数字，那就需要一个即可回推输入流，又是一个数据数据流的引用。

```java
//读取输入时，预览下一个字节了解是否时需要的值。
PushbackInputStream pis = new PushbackInputStream(
  new BufferedInputStream(
    new FileInputStream("out.txt"))
);


System.out.println("原始输入可用字节：" + pis.available());
int read = pis.read();
System.out.println("读取到字节[pis.read()]：" + read);
System.out.println("读取后剩余可用字节：" + pis.available());
System.out.println("回推读取到的字节[pis.unread(read)]：" + read);
pis.unread(read);
System.out.println("回推后可用字节：" + pis.available());

//out.txt文件：1234
//原始输入可用字节：5
//读取到字节[pis.read()]：49
//读取后剩余可用字节：4
//回推读取到的字节[pis.unread(read)]：49
//回推后可用字节：5
```





## 文本输入与输出

- 保存数据时，可以选择<u>二进制格式</u>或<u>文本格式</u>。
  - 存储文本字符串时，需要考虑字符编码方式。Java内部使用 UTF-16 编码方式。
- `OutputStreamWriter`类 使用选定的字符编码方式，把<u>Unicode码元的输出流转换为字节流</u>。
- `InputStreamReader`类 将包含字节（用某种字符编码方式表示的字符）的输入流转换为可以产生Unicode的码元读入器。



## 写出文本输出

- 使用PrintWriter类。

  - 拥有文本格式打印字符串和数字的方法。

  ```java
  PrintWriter printWriter = new PrintWriter("writer.txt");
  printWriter.println("hello zyb.");
  printWriter.flush();
  printWriter.close();	
  ```

  - 可以使用另一个构造器方法[ PrintWriter(Writer writer, boolean autoFlush) ]设置自动冲刷机制：

  ```java
  PrintWriter printWriter = new PrintWriter(
    new OutputStreamWriter(
      new FileOutputStream("writer.txt")
    ),
    true
  );
  printWriter.println("auto flush");
  //        printWriter.flush();
  //        printWriter.close();
  ```

  

## 读入文本输入

- Scanner类：从任何<u>输入流构建Scanner对象</u>。

  ```java
  /**  源码
      /**
       * Constructs a new <code>Scanner</code> that produces values scanned
       * from the specified input stream. Bytes from the stream are converted
       * into characters using the underlying platform's
       * {@linkplain java.nio.charset.Charset#defaultCharset() default charset}.
       *
       * @param  source An input stream to be scanned
       */
      public Scanner(InputStream source) {
          this(new InputStreamReader(source), WHITESPACE_PATTERN);
      }
  */
  
    
  Scanner in = new Scanner(new FileInputStream("writer.txt"));
  
  while(in.hasNext()){
    System.out.println(in.next());
  }
  //writer.txt 内容：auto flush
  //输出：
  //auto
  //flush
  ```

- Files.readAllBytes方法：<u>短小文本文件读入</u>。

  ```java
  /**
      Reads all the bytes from a file. The method ensures that the file is closed when all bytes have been read or an I/O error, or other runtime exception, is thrown.
      Note that this method is intended for simple cases where it is convenient to read all bytes into a byte array. It is not intended for reading in large files.
      Params:
      path – the path to the file
      Returns:
      a byte array containing the bytes read from the file
      Throws:
      IOException – if an I/O error occurs reading from the stream
      OutOfMemoryError – if an array of the required size cannot be allocated, for example the file is larger that 2GB
      SecurityException – In the case of the default provider, and a security manager is installed, the checkRead method is invoked to check read access to the file.
    */
  
  String str = new String(
    Files.readAllBytes(Paths.get("/Users/zhangyanbo/writer.txt"))
  );
  System.out.println(str);
  ```

- Files.readAllLines(path,charset)方法：将<u>文件一行行的读入</u>。

  ```java
  List<String> allLines = Files.readAllLines(Paths.get("/Users/zhangyanbo/allLines.txt"),StandardCharsets.UTF_8);
  for(String value:allLines){
    System.out.println(value);
  }
  ```

- Files.lines(path,charset)：<u>处理大文件</u>，将行惰性处理为一个Stream\<String\>对象。

  ```java 
  Stream<String> lines = Files.lines(Paths.get("/Users/zhangyanbo/allLines.txt"), StandardCharsets.UTF_8);
  String collect = lines.collect(Collectors.joining("|"));
  System.out.println(collect);
  
  //按行读取
  Stream<String> lines = Files.lines(Paths.get("C:\\Users\\Lenovo\\Desktop\\hdfs常用命令.txt"), StandardCharsets.UTF_8);
  lines.forEach(
      line -> {
          System.out.println(line);
          System.out.println("------------");
      }
  );
  ```

- 使用扫描器Scanner读入符号token：由分隔符分隔的字符串，默认分隔符是空白字符。可以将分隔符修改为正则表达式。

  ```java
  Scanner in = new Scanner(new FileInputStream("/Users/zhangyanbo/allLines.txt"));
  //这里表示接受任何非Unicode字母作为分隔符。
  in.useDelimiter("\\PL+");
  while (in.hasNext()){
    System.out.println(in.next());
  }
  ```

- 早期处理文本输入的方法：BufferedReader类。

  ```java
  //早期方法。
  InputStream inputStream = new FileInputStream("/Users/zhangyanbo/allLines.txt");
  InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
  BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
  String line;
  while ((line = bufferedReader.readLine()) != null){
    System.out.println(line);
  }
  
  
  //现在又了lines方法获取Stream<String>对象。
  
  Stream<String> lines = bufferedReader.lines();
  String collect = lines.collect(Collectors.joining("\n"));
  System.out.println(collect);
  ```

  

## 字符编码方式

**字符如何编码成字节？**

- Java 针对字符使用的是Unicode标准。
  - 每个字符或编码点都具有一个21位的整数，有多种不同的字符**编码方式**。即，将21位数字包装成字节的方法有很多。

#### 编码方式

- UTF-8：

  将每个Unicode编码点编码为1到4个字节的序列。

- UTF-16：

  将每个Unicode编码点编码为1个或2个16位值。

- ISO-8859-1:

  单字节编码。

- Shift-JIS：

  日文字符可变长编码。



#### StandardCharsets类

- 具有类型为`Charset`的静态变量，用于表示每种Java虚拟机都必须支持的字符编码方式：

```java
public final class StandardCharsets {
	public static final Charset US_ASCII = Charset.forName("US-ASCII");
  public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
  public static final Charset UTF_8 = Charset.forName("UTF-8");
  public static final Charset UTF_16BE = Charset.forName("UTF-16BE");
  public static final Charset UTF_16LE = Charset.forName("UTF-16LE");
  public static final Charset UTF_16 = Charset.forName("UTF-16");
}
```

- 为了获得另一种编码方式的Charset，使用静态的forName方法：

```java
Charset shiftJIS = Charset.forName("Shift-JIS");
```



## 读写二进制数据

**文本格式并不像以<u>二进制格式</u>传递数据那样<u>高效</u>。**

- DataOutput接口

  定义了以二进制格式写数组、字符、boolean值和字符串的方法。

  ```java
  writeBoolean
  writeByte
  writeShort
  writeChar
  //总是将整数写出为4字节的二进制数量值。
  writeInt
  writeLong
  writeFloat
  //总是将一个double值写出为8字节的二进制数据值。
  writeDouble
  writeBytes
  writeChars
  writeUTF
  ```

  - 产生的结果并非人可阅读的，但是<u>对于给定类型的每个值，使用的空间是相同的，而且将其读回也比解析文本要快</u>。

- DataInput接口

  读回数据。

  ```java
  //读入一个给定类型的值。
  readBoolean
  readByte
  readUnsignedByte
  readShort
  readUnsignedShort
  readChar
  readInt
  readLong
  readFloat
  readDouble
  readLine
  readUTF
  ```

- 从文本读取二进制数据：将DataInputStream类与某个字节流组合。

  ```java
  DataInputStream dataInputStream = new DataInputStream(new FileInputStream("/Users/zhangyanbo/allLines.txt"));
  //读入四个字节：例如0000
  int i = dataInputStream.readInt();
  //输出：808464432
  System.out.println(i);
  //808464432是10进制数，转换为16进制是0x30303030，0x30是字符'0'的ASCII码的16进制表示，也就是说，808464432这个数字，对应字符串“0000”
  ```

- 写出二进制数据：使用DataOutputStream类与FileOutputStream组合。

  ```java
  //先写出
  DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream("/Users/zhangyanbo/allLines.txt"));
  dataOutputStream.writeInt(1234);
  //在读入
  DataInputStream dataInputStream = new DataInputStream(new FileInputStream("/Users/zhangyanbo/allLines.txt"));
  int i = dataInputStream.readInt();
  System.out.println(i);
  
  //输出1234
  ```



## 随机访问文件

RandomAccessFile类可以在任何位置查找或写入数据。

- 使用字符串“r“ 或者 ”rw“作为构造器的参数指定只读/读写。

  ```java
  RandomAccessFile r = new RandomAccessFile("/Users/zhangyanbo/allLines.txt", "r");
  int i = r.readInt();
  System.out.println(i);
  //1234
  ```

- 文件作为RandomAccessFile打开时，文件不会被删除。

- 有一个`seek方法`：将**文件指针**设置到文件中的任意字节位置。seek参数是一个long类型整数，值位于0到文件按字节度量的长度。

  - **文件指针**：表示下一个将被读入或写出的字节所处位置的字节指针。

  ```java
  RandomAccessFile r = new RandomAccessFile("/Users/zhangyanbo/allLines.txt", "r");
  System.out.println(r.readInt());
  System.out.println(r.getFilePointer());
  //1234
  //4
  
  r.seek(0);
  System.out.println(r.getFilePointer());
  //0
  
  System.out.println(r.readInt());
  System.out.println(r.getFilePointer());
  //1234
  //4
  ```

- getFilePointer方法：返回文件指针当前位置。

  ```java
  RandomAccessFile r = new RandomAccessFile("/Users/zhangyanbo/allLines.txt", "r");
  int i = r.readInt();
  System.out.println(i);
  //1234
  System.out.println(r.getFilePointer());
  //4
  ```



## 对象输入输出流与序列化

### 保存和加载序列化对象



