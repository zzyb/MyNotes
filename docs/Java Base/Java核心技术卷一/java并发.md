# 并发

## 什么是线程

如果**一个程序**可以**同时**运行**<u>多个线程</u>**，则称这个程序是`多线程`的（multithreaded）。



`多线程` & 多进程

- 本质区别在于每个进程都拥有一套变量，而**线程则共享数据**。



**单独线程运行任务的简单过程**：

1. 将代码放入一个类的run方法中，这个类要实现Runnable接口。
   - Runnable接口只有一个方法：void run() 。
   - 由于Runnable是一个函数式接口，可以用一个lambda表达式创建一个实例。
     - `Runnable r = () -> { task code }`
2. 通过Runnable 构造一个Thread对象。
3. 启动线程。

```java

```



```java

```



```java

```



还可以通过 建立一个Thread类的一个子类来定义线程。（不推荐！！！）

1. 创建Thread的一个子类，实现run方法。
2. 构造子类对象，并调用start方法，启动。







<u>不要直接调用Thread或Runnable接口的run方法</u>。**应该调用Thread.start方法**。



## 线程状态

<u>线程有6种状态</u>。

