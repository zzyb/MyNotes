# 分布式文件系统 HDFS



## HDFS设计



## HDFS基本概念



## HDFS命令行接口



## Java接口



## 数据流

### 剖析文件读取

![](./HDFS.resource/剖析文件读取.png)

1. 步骤 1：客户端通过调用**FileSyste对象**的open()方法打开希望读取的文件。
   1. 在HDFS中这个对象是<u>DistributedFileSystem的一个实例</u>。
2. 步骤 2：DistributedFileSystem通过使用远程调用(RPC)来调用namenode，以确定文件的**起始块**位置。
   1. 对于每一个块，namenode 返回存有该块复本的datanode地址。
   2. 这些datanode根据它们与客户端的距离来排序(根据几群的网络拓扑)。
   3. 如果该客户端本身就是一个datanode（比如，在一个MapReduce任务中），那么客户端将会从保存有相应数据块复本的本地datanode读取数据。
3. DistributedFileSystem 返回 一个 `FSDataInpuStream` 对象（一个支持文件定位的输入流）给客户端以便读取数据。
   1. `FSDataInputStream`类转而封装DFSInputStream对象，该对象管理着datanode和namenode的I/O。
4. 步骤 3：客户端对输入流调用read()方法。
   1. 存储着**文件起始几个块**的datanode地址的DFSInputStream 随即连接<u>距离最近的文件中的第一个块</u>所在的datanode。
   2. 步骤 4：通过对数据流反复调用read()方法，可以将数据从datanode传输到客户端。
   3. 步骤 5：到达块末端时，DFSInputStream关闭与该datanode的连接，然后寻找下一个块的最佳datanode。
   4. 所有这些操作对客户端都是透明的，在客户端看来它一直在读取一个而连续的流。
5. 步骤 6 ：客户端从流中读取数据的时候，块是按照打开DFSInputStream 与 datanode 新建连接的顺序读取的。它也会根据需要询问namenode来检索下一批数据块的datanode的位置。<u>一旦客户端完成读取，就对`FSDataInputStream`调用close()方法</u>。
6. 在读取数据的时候，如果DFSInputStream在与datanode通信时遇到错误：
   1. 会尝试从这个块的另外一个最邻近datanode读取数据。
   2. 记住故障的datanode，以保证以后不会反复读取该节点上后续的块。
7. DFSInputStream会通过校验和确认datanode发来的数据是否完整。如果发现有损坏：
   1. DFSInputStream试图从其他datanode读取其复本。
   2. 将被损坏的块通知给namenode。



**设计重点**：



### 剖析文件写入

![](./HDFS.resource/剖析文件写入.png)

## 通过distcp并行复制