# RDD编程

## 一、概览	

​	At a high level, every Spark application consists of a *driver program* that runs the user’s `main` function and executes various *parallel operations* on a cluster. The main abstraction Spark provides is a ***resilient distributed dataset* (RDD)**, which is a collection of elements partitioned across the nodes of the cluster that can be operated on in parallel. RDDs are created by starting with a file in the Hadoop file system (or any other Hadoop-supported file system), or an existing Scala collection in the driver program, and transforming it. Users may also ask Spark to *persist* an RDD in memory, allowing it to be reused efficiently across parallel operations. Finally, RDDs automatically recover from node failures.

​	在高层次上，每个 Spark 应用程序都由一个驱动程序组成，该驱动程序运行用户的主函数并在集群上执行各种并行操作。**Spark 提供的主要抽象是弹性分布式数据集(RDD)** ，它是<u>跨集群节点分区的元素的集合，可以并行操作</u>。

​	RDDs 的创建方法是从 Hadoop 文件系统(或任何其他 Hadoop 支持的文件系统)中的一个文件或者驱动程序中现有的 Scala 集合开始，然后转换它。用户还可能要求 Spark 在内存中持久化 RDD，以便在并行操作中有效地重用它。最后，rdd 自动从节点故障中恢复。

​	

​	Spark 中的第二个抽象是**可以在并行操作中使用的共享变量**。默认情况下，当 Spark 将一个函数作为一组任务并行运行在不同的节点上时，它将该函数中使用的每个变量的副本发送到每个任务。有时，变量需要跨任务共享，或者在任务和驱动程序之间共享。

​	<u>Spark 支持两种类型的共享变量: **广播变量**(可用于在所有节点的内存中缓存值)和**累加器**(只“添加”到其中的变量，如计数器和求和)</u>。



## 二、弹性分布式数据集(RDDs)

RDD 是一个**可以并行操作的容错元素集合**。

创建 rdd 有两种方法:

1.  在驱动程序中并行化现有的集合。
2. 在外部存储系统中引用数据集，比如共享文件系统、 HDFS、 HBase，或者任何提供 Hadoop InputFormat 的数据源。

### 2.1 并行集合 Parallelized Collections

​	并行化集合是通过调用 Spark Context 在您的驱动程序(a Scala seq)中的现有集合上的并行化方法创建的。<u>复制集合的元素以形成可并行操作的分布式数据集</u>。例如，这里是如何创建一个包含数字1到5的并行集合：

```scala
scala> val data = Array(1, 2, 3, 4, 5)
data: Array[Int] = Array(1, 2, 3, 4, 5)

scala> val distData = sc.parallelize(data)
distData: org.apache.spark.rdd.RDD[Int] = ParallelCollectionRDD[0] at parallelize at <console>:26
// data 是一个Array ; distData 是一个RDD（弹性分布式数据集）。
```

创建后，可以并行操作分布式数据集( 在这里的例子是 distData )。

​	**并行集合的一个重要参数是要将数据集切割到的分区数量**。<u>Spark 将为集群的每个分区运行一个任务</u>。通常，您需要为集群中的每个 CPU 分配2-4个分区。通常，Spark 尝试基于集群自动设置分区数。但是，您也可以通过将其作为并行化的第二个参数传递来手动设置它(例如，sc.paralize (data，10))。



### 2.2 外部数据集 External Datasets 

​	Spark 可以从 Hadoop 支持的任何存储源创建分布式数据集，包括您的本地文件系统、 HDFS、 Cassandra、 HBase、 Amazon s3等。Spark 支持文本文件、 Sequence Files 和任何其他 Hadoop InputFormat。

```scala
scala> val distFile = sc.textFile("/root/hosts")
distFile: org.apache.spark.rdd.RDD[String] = /root/hosts MapPartitionsRDD[2] at textFile at <console>:24
```

**关于使用 Spark 读取文件的一些注释:**

- 如果使用本地文件系统上的路径，则还必须在辅助节点上的相同路径上访问该文件。将该文件复制到所有工作节点，或者使用网络挂载的共享文件系统。
- Spark 的所有基于文件的输入方法，包括 textFile，支持在目录、压缩文件和通配符上运行。
  - 例如，可以使用 textFile (”/my/directory”)、 textFile ("/my/directory/\*.txt")和 textFile ("/my/directory/\*.gz")。
  - 当读取多个文件时，分区的顺序取决于文件从文件系统返回的顺序。例如，它可能<u>遵循或不遵循</u>按路径排列的文件的字典顺序。
  - 在一个分区中，元素按照它们在基础文件中的顺序排序。
- textFile 方法还采用一个可选的第二个参数来控制文件的分区数。
  - 默认情况下，Spark 为文件的每个块创建一个分区(在 HDFS 中默认为128 MB) ，但是您也可以通过传递更大的值来要求更高的分区数量。请注意，分区不能少于块。

### 2.3 RDD Operations

**RDDs 支持两种类型的操作: **

- 转换(transformations)

  一个RDD生成一个新的RDD。

- 操作(actions)

  对一个RDD计算出一个结果，并把结果返回到驱动器程序中，或把结果存储到外部系统（例如HDFS）中。

<u>	前者【转换】从现有的数据集中创建新的数据集，后者【操作】在数据集上运行计算后向驱动程序返回一个值。</u>例如，map 是一个转换，它将每个数据集元素通过一个函数并返回一个表示结果的新 RDD。另一方面，reduce 是一个动作，它使用某个函数聚合 RDD 的所有元素，并将最终结果返回给驱动程序(尽管还有一个并行 reduceByKey 返回分布式数据集)。

​	**Spark 中的所有<u>转换（transformations）</u>都是懒惰的**，因为它们不会立即计算结果。相反，它们只记得应用于某些基本数据集(例如文件)的转换。**只有当<u>操作（actions）</u>要求将结果返回给驱动程序时，才计算转换**。这种设计使 Spark 能够更有效地运行。例如，我们可以认识到，通过 map 创建的数据集将被用于 reduce，并且只将 reduce 的结果返回给驱动程序，而不是较大的映射数据集。

​	默认情况下，每次对转换后的 RDD 运行操作时，都可以重新计算它。但是，**您也可以使用 persist (或 cache)方法在内存中持久化 RDD**，<u>在这种情况下，Spark 将保留集群中的元素，以便在下次查询它时更快地访问它</u>。**还支持在磁盘上持久化 rdd，或者跨多个节点复制 rdd**。



### 2.4 RDD创建

#### 读取外部数据集

##### textFile()

```java
SparkConf sparkConf = new SparkConf()
    .setMaster("local[1]") // 注意:
    .setAppName("firstSpark");
SparkContext sparkContext = new SparkContext(sparkConf);
JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);
// 读取外部数据集（这里是文件系统中）
JavaRDD<String> stringJavaRDD = javaSparkContext.textFile("C:\\Users\\Lenovo\\Desktop\\spark.txt");
```



#### 对现有数据集并行化

##### parallelize()

```java
SparkConf sparkConf = new SparkConf()
    .setMaster("local[1]") // 注意，不是webUI的地址。
    .setAppName("firstSpark");
SparkContext sparkContext = new SparkContext(sparkConf);
JavaSparkContext javaSparkContext = new JavaSparkContext(sparkContext);
// 创建List集合
ArrayList<String> lines = new ArrayList<>(Arrays.asList(
    "spark jjj",
    "spark jjj",
    "spark jjj",
    "spark jjj",
    "flink jjj",
    "flink"
));
// 将集合转化为RDD
JavaRDD<String> stringJavaRDD = javaSparkContext.parallelize(lines);
```



### 2.5 RDD操作

- 转换操作：`返回一个新的RDD`，例如：map、filter。
- 行动操作：`向驱动器程序返回结果或把结果写入外部系统，会触发实际计算`，例如：count、first。

#### 2.5.1 转换操作



#### 2.5.2 行动操作
