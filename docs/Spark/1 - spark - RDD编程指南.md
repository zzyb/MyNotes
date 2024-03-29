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

#### 2.4.1 读取外部数据集

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



#### 2.4.2 对现有数据集并行化

**常用于开发原型和测试**，这种方式<u>需要提前把整个数据集放在一台机器的内存中</u>。

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

#### 2.5.1 转化操作

1. 转化操作是返回新的RDD的操作。
2. 转化操作是惰性操作，只有在行动操作中用到了这些RDD才会被计算。

##### map

接收一个函数，把函数用于RDD中的每个元素，将函数的返回结果作为结果RDD中对应元素的值。

```java
// map操作：接收的字符串数据转换为长度
JavaRDD<Integer> mapRDD = stringJavaRDD.map(new Function<String, Integer>() {
    @Override
    public Integer call(String s) throws Exception {
        return s.length();
    }
});
```



##### filter

接收一个函数，并将满足该函数的元素放入新的RDD中返回。

```java
// filter操作：接收字符串数据，如果包含flink则返回。
JavaRDD<String> filterRDD = stringJavaRDD.filter(new Function<String, Boolean>() {
    @Override
    public Boolean call(String s) throws Exception {
        return s.contains("flink");
    }
});
```



##### flatmap

接收一个函数，应用到输入RDD的每个元素上；返回的不是一个元素，而是一个返回值序列迭代器。**最终输出是**一个<u>包含各个迭代器的所有元素的RDD</u>。

```java
// flatMap操作：接收字符串数据，根据分隔符返回成单个单词数据。
JavaRDD<String> flatMapRDD = stringJavaRDD.flatMap(new FlatMapFunction<String, String>() {
    @Override
    public Iterator<String> call(String s) throws Exception {
        String[] words = s.split(" ");
        Iterator<String> iterator = Arrays.stream(words).iterator();
        return iterator;
    }
});
```



##### distinct

生成一个只包含不同元素的新RDD。

<u>distinct开销很大，需要将所有数据通过网络进行混洗（shuffle）</u>，确保每个元素都只有一份。

// to-do ： `数据混洗与如何避免数据混洗`

```java
ArrayList<String> lines = new ArrayList<>(Arrays.asList(
    "spark",
    "spark",
    "hadoop",
    "hadoop",
    "flink",
    "flink"
));
JavaRDD<String> stringJavaRDD = javaSparkContext.parallelize(lines);

// 去重
JavaRDD<String> distinct = stringJavaRDD.distinct();

// 测试输出
distinct.foreach(new VoidFunction<String>() {
    @Override
    public void call(String s) throws Exception {
        System.out.println(s);
    }
});
// 输出：
//spark
//hadoop
//flink
```



##### union(other)

返回一个包含两个RDD中所有元素的RDD。（spark的union操作<u>包含重复数据</u>！）

```java
JavaRDD<String> stringJavaRDD = javaSparkContext.parallelize(lines);
JavaRDD<String> stringJavaRDD2 = javaSparkContext.parallelize(lines);

// union操作：将两个RDD的数据聚合到一个RDD
JavaRDD<String> unionRDD = stringJavaRDD.union(stringJavaRDD2);
```



##### intersection(other)

返回两个RDD<u>都有的元素</u>，同时<u>去掉所有重复元素</u>。（注意：**单个RDD中的重复元素也会被移除**）

性能差，需要网络混洗来发现共有元素。

```java
ArrayList<String> lines = new ArrayList<>(Arrays.asList(
    "spark",
    "hadoop",
    "hadoop",
    "flink"
));
JavaRDD<String> stringJavaRDD = javaSparkContext.parallelize(lines);
ArrayList<String> lines2 = new ArrayList<>(Arrays.asList(
    "hadoop",
    "flink"
));
JavaRDD<String> stringJavaRDD2 = javaSparkContext.parallelize(lines2);

// intersection操作：返回两个RDD都有的元素，同时去掉重复元素。（单个RDD中的重复元素也会被移除）
JavaRDD<String> intersectionRDD = stringJavaRDD.intersection(stringJavaRDD2);
// 输出：flink、hadoop
```



##### subtract(other)

返回<u>只存在于第一个RDD而不存在于第二个RDD的**所有元素**</u>组成的RDD。（不去重！！！）

需要网络混洗。

```java
ArrayList<String> lines = new ArrayList<>(Arrays.asList(
    "spark",
    "spark",
    "hadoop",
    "hadoop",
    "flink",
    "flink"
));

ArrayList<String> lines2 = new ArrayList<>(Arrays.asList(
    "hadoop",
    "flink"
));

JavaRDD<String> stringJavaRDD = javaSparkContext.parallelize(lines);
JavaRDD<String> stringJavaRDD2 = javaSparkContext.parallelize(lines2);

// 返回只存在于第一个RDD而不存在于第二个RDD的所有元素组成的RDD。（不去重！！！）
JavaRDD<String> subtractRDD = stringJavaRDD.subtract(stringJavaRDD2);
// 结果：spark、spark
```



##### cartesian(other)

返回所有可能的(a,b)对，a来自第一个RDD，b来自另一个RDD。

<u>大规模RDD的笛卡尔积开销巨大</u>！！！

注意：<u>输出类型是两个RDD组成的RDD对（二元组）</u>

```java
ArrayList<String> lines = new ArrayList<>(Arrays.asList(
    "spark",
    "hadoop",
    "flink"
));

ArrayList<String> lines2 = new ArrayList<>(Arrays.asList(
    "123",
    "abc"
));

JavaRDD<String> stringJavaRDD = javaSparkContext.parallelize(lines);
JavaRDD<String> stringJavaRDD2 = javaSparkContext.parallelize(lines2);


//  返回所有可能的(a,b)对，a来自第一个RDD，b来自另一个RDD。
// 注意：输出类型和两个RDD类型不一样：是两个RDD组成的对！！！
JavaPairRDD<String, String> cartesianRDD = stringJavaRDD.cartesian(stringJavaRDD2);


// 测试输出
cartesianRDD.foreach(new VoidFunction<Tuple2<String, String>>() {
    @Override
    public void call(Tuple2<String, String> tuple2Value) throws Exception {
        System.out.println(tuple2Value);
    }
});
// 输出：
//(spark,123)
//(spark,abc)
//(hadoop,123)
//(hadoop,abc)
//(flink,123)
//(flink,abc)
```





#### 2.5.2 行动操作

1. 行动操作会把最终求出的结果返回到驱动器程序，或者写入外部存储系统中。会强制执行那些求值必须用到的RDD转化操作。

##### reduce(function)

接收一个函数作为参数，函数操作两个<u>RDD的元素类型的数据</u>，并<u>返回一个同样类型的**新元素**</u>。

- （返回的不是RDD！！！ 可以看做聚合结果。）
- 要求**函数返回值类型**和我们操作的<u>RDD类型相同</u>。

```java
ArrayList<Tuple2<String, Integer>> lines = new ArrayList<Tuple2<String, Integer>>(Arrays.asList(
    new Tuple2<>("spark", 1),
    new Tuple2<>("flink", 1),
    new Tuple2<>("hadoop", 1),
    new Tuple2<>("spark", 1)
));

JavaRDD<Tuple2<String, Integer>> tuple2JavaRDD = javaSparkContext.parallelize(lines);

// reduce返回了聚合结果。
Tuple2<String, Integer> reduce = tuple2JavaRDD.reduce(new Function2<Tuple2<String, Integer>, Tuple2<String, Integer>, Tuple2<String, Integer>>() {
    @Override
    public Tuple2<String, Integer> call(Tuple2<String, Integer> value1, Tuple2<String, Integer> value2) throws Exception {
        StringBuffer sb = new StringBuffer();
        // key 按照下划线“_”拼接在一起
        String str = sb.append(value1._1).append("_").append(value2._1).toString();
        // value 前后相加
        int sum = value1._2 + value2._2;
        Tuple2<String, Integer> t2 = new Tuple2<String, Integer>(str, sum);
        return t2;
    }
});

System.out.println(reduce.toString());
```



##### fold(initValue,function)

两个参数：

1. 初始值：作为每个分区第一次调用时的结果。
   - 提供的初始值是你提供操作的单位元素。
   - 注意：初始值必须多次调用时不会改变结果（例如＋对应0，*对应1，拼接操作对应空列表）。
2. 函数：函数操作两个<u>RDD的元素类型的数据</u>，并<u>返回一个同样类型的**新元素**</u>。

```java
ArrayList<Tuple2<String, Integer>> lines = new ArrayList<Tuple2<String, Integer>>(Arrays.asList(
    new Tuple2<>("spark", 1),
    new Tuple2<>("flink", 1),
    new Tuple2<>("hadoop", 1),
    new Tuple2<>("spark", 1)
));

JavaRDD<Tuple2<String, Integer>> tuple2JavaRDD = javaSparkContext.parallelize(lines);

// 创建一个初始状态
Tuple2<String, Integer> beginState = new Tuple2<>("", 0);
// fold参数：初始值，函数
Tuple2<String, Integer> fold = tuple2JavaRDD.fold(beginState, new Function2<Tuple2<String, Integer>, Tuple2<String, Integer>, Tuple2<String, Integer>>() {
    @Override
    public Tuple2<String, Integer> call(Tuple2<String, Integer> first, Tuple2<String, Integer> second) throws Exception {
        String str = new StringBuffer().append(first._1).append(second._1).toString();
        int sum = first._2 + second._2;
        Tuple2<String, Integer> t2result = new Tuple2<>(str, sum);
        return t2result;
    }
});

// 测试输出
System.out.println(fold.toString());
```



##### aggregate()

aggregate函数**<u>可以返回与操作类型RDD不同类型的数据！！！</u>**

三个参数：

1. 初始值：期待返回类型的初始值。
2. 一个函数：把RDD中的元素合并起来放入累加器。(每个节点上)
3. 第二个函数：将累加器两两合并。（节点之间的）

```java
ArrayList<Tuple2<String, Integer>> lines = new ArrayList<Tuple2<String, Integer>>(Arrays.asList(
  new Tuple2<>("spark", 1),
  new Tuple2<>("flink", 2),
  new Tuple2<>("hadoop", 3),
  new Tuple2<>("spark", 4)
));

JavaRDD<Tuple2<String, Integer>> tuple2JavaRDD = javaSparkContext.parallelize(lines);

// 创建一个初始状态（这里可以改变返回类型）
Tuple3<String, Integer,Integer> beginState = new Tuple3<>("", 0,0);

Tuple3<String, Integer, Integer> aggregate = tuple2JavaRDD.aggregate(
  // 初始值，可以与输入RDD不同。
  beginState,
  // 合并元素到累加器
  new Function2<Tuple3<String, Integer, Integer>, Tuple2<String, Integer>, Tuple3<String, Integer, Integer>>() {
    @Override
    public Tuple3<String, Integer, Integer> call(Tuple3<String, Integer, Integer> v1, Tuple2<String, Integer> v2) throws Exception {
      String str = v1._1() + v2._1;
      int sum = v1._2() + v2._2();
      int count = v1._3() + 1;
      return new Tuple3<>(str,sum,count);
    }
  },
  //  不同分区间累加器的合并
  new Function2<Tuple3<String, Integer, Integer>, Tuple3<String, Integer, Integer>, Tuple3<String, Integer, Integer>>() {
    @Override
    public Tuple3<String, Integer, Integer> call(Tuple3<String, Integer, Integer> v1, Tuple3<String, Integer, Integer> v2) throws Exception {
      return new Tuple3<>(v1._1()+v2._1(),v1._2()+ v2._2(),v1._3()+v2._3());
    }
  }
);

// 最终输出结果：计算出来各个值的平均数。
System.out.println(aggregate._1());
System.out.println(Double.valueOf(aggregate._2())/aggregate._3());
```



##### count

```java
// 对RDD进行计数并输出
System.out.println(filterRDD.count());
```



##### collect

用来获取整个RDD的数据。

注意：

- 如果你把RDD筛选到很小的规模，并且希望在本地处理这些数据时，就可以使用它。
- <u>只有当整个数据集能够在单台机器上放的下时，才能使用collect</u>。也就是说，**不能用在大规模数据集上！！！**

```java
// 获取整个RDD的数据（必须是小规模！！！）
List<String> values = stringJavaRDD.collect();
for (String value : values) {
    System.out.println(value);
}
```



##### take

返回RDD的<u>n个元素，并尝试只访问尽量少的分区</u>。（因此会得到一个不均衡的集合）

操作返回元素的顺序与你预期的可能不同。（<u>没有保证顺序</u>）

```java
// 获取第一个元素，并输出
List<String> values = stringJavaRDD.take(1);
for (String value : values) {
    System.out.println(value);
}
```



##### top

（**如果为数据定义了顺序**！！！）获取前几个元素。

1. 使用默认顺序

   ```java
   ArrayList<String> lines = new ArrayList<>(Arrays.asList(
       "1spark jjj",
       "2spark jjjj",
       "3spark jjjjj",
       "4spark jjjjjj",
       "5flink jjjjjjj",
       "6flink"
   ));
   
   JavaRDD<String> stringJavaRDD = javaSparkContext.parallelize(lines);
   
   // 获取前3个元素
   List<String> values = stringJavaRDD.top(3);
   
   for (String value : values) {
       System.out.println(value);
   }
   ```

   ```txt
   6flink
   5flink jjjjjjj
   4spark jjjjjj
   ```

2. 可以自己提供比较函数来提取前几个元素。

   ```java
   // 创建 比较函数（这里需要实现序列化接口！！！）
   public class TopComparator implements Comparator<String>, Serializable {
       @Override
       public int compare(String o1, String o2) {
           if (o1.length() > o2.length()) {
               return 1;
           } else if (o1.length() < o2.length()) {
               return -1;
           } else {
               return 0;
           }
       }
   }
   // spark 主程序中使用top并传入比较函数
   ArrayList<String> lines = new ArrayList<>(Arrays.asList(
       "1spark jjj",
       "2spark jjjj",
       "3spark jjjjj",
       "4spark jjjjjj",
       "5flink jjjjjjj",
       "6flink"
   ));
   
   JavaRDD<String> stringJavaRDD = javaSparkContext.parallelize(lines);
   
   // 获取前3个元素
   List<String> values = stringJavaRDD.top(3, new TopComparator());
   
   for (String value : values) {
       System.out.println(value);
   }
   ```

   ```txt
   5flink jjjjjjj
   4spark jjjjjj
   3spark jjjjj
   ```



##### takeSample(withReplacement, num, seed)

三个参数：

1. 是否可以重复抽样（会抽到同一个值）
2. 抽取个数
3. 随机数生成器的种子(一般都是默认不会指定)

**从数据中获取一个采样，并指定是否替换。**

仅在<u>预期结果数组很小的情况下使用</u>，因为所有数据都被加载到driver的内存中。

```java
// 不重复抽样，抽取两个，种子默认
List<String> takeSampleValues = stringJavaRDD.takeSample(false, 2);
```



##### countByValue

返回一个从<u>各个值</u>到<u>值对应的计数</u>的映射表。

```java
ArrayList<Tuple2<String, Integer>> lines = new ArrayList<Tuple2<String, Integer>>(Arrays.asList(
    new Tuple2<>("spark", 1),
    new Tuple2<>("flink", 1),
    new Tuple2<>("hadoop", 1),
    new Tuple2<>("spark", 1)
));

JavaRDD<Tuple2<String, Integer>> tuple2JavaRDD = javaSparkContext.parallelize(lines);


Map<Tuple2<String, Integer>, Long> tuple2LongMap = tuple2JavaRDD.countByValue();

Set<Map.Entry<Tuple2<String, Integer>, Long>> entries = tuple2LongMap.entrySet();

for (Map.Entry<Tuple2<String, Integer>, Long> entry : entries) {
    System.out.println(entry.getKey() + "----------" + entry.getValue());
}
```

```txt
(flink,1)----------1
(spark,1)----------2
(hadoop,1)----------1
```



##### foreach

对RDD中的每个元素进行操作，而不需要把RDD发回本地。

```java
ArrayList<Tuple2<String, Integer>> lines = new ArrayList<Tuple2<String, Integer>>(Arrays.asList(
    new Tuple2<>("spark", 1),
    new Tuple2<>("flink", 1),
    new Tuple2<>("hadoop", 1),
    new Tuple2<>("spark", 1)
));

JavaRDD<Tuple2<String, Integer>> tuple2JavaRDD = javaSparkContext.parallelize(lines);

tuple2JavaRDD.foreach(new VoidFunction<Tuple2<String, Integer>>() {
    @Override
    public void call(Tuple2<String, Integer> value) throws Exception {
        System.out.println(value);
    }
});
```

#### 2.5.3 惰性求值

1. 意味着当我们对RDD进行转化操作时，操作不会立即执行。Spark会在内部记录下要执行操作的相关信息。
2. 数据读取到RDD的操作也是惰性的。
   - 当我们调用sc.textFile时，数据并没有读取进来，而是在必要时才会读取。
   - 和转化操作一样，读取操作也可能会多次执行。
3. 最好把每个RDD当做我们通过转化操作构建出来的、记录如何计算数据的指令列表。



### 2.6 不同RDD类型间的转换

有些函数只能用于特定的RDD类型：

- mean、variance只能用在数值RDD上。
- join只能用在键值对RDD上。

要访问这些附加功能，<u>必须确保获得了正确的专用RDD类</u>。

#### scala

```scala
// to-do
```

#### Java

Java中有两个专门的类：

- JavaDoubleRDD
- JavaPairRDD

来处理特殊类型的RDD。这两个类还针对这些类型提供了额外的函数。

##### DoubleFunction\<T\>

不能使用map接收这个函数，而要使用`mapToDouble`。

```java
// 使用DoubleFunction<T>，等价于Function<T,Double>
// 对于这个案例：使用DoubleFunction<String>，等价于Function<String,Double>
JavaDoubleRDD javaDoubleRDD = stringJavaRDD.mapToDouble(new DoubleFunction<String>() {
    @Override
    public double call(String s) throws Exception {
        return s.length();
    }
});

// Function<T,Double> 这里是：Function<String,Double>
JavaRDD<Double> mapRDD = stringJavaRDD.map(new Function<String, Double>() {
    @Override
    public Double call(String v1) throws Exception {
        return Double.valueOf(v1.length());
    }
});
```



##### DoubleFlatMapFunction\<String\>

不能使用flatMap接收这个函数，而要使用`flatMapToDouble`。

```java
// 使用DoubleFlatMapFunction<T>，等价于Function<T,Iterator<Double>>
// 对于这个案例：使用DoubleFlatMapFunction<String>，等价于Function<String,Iterator<Double>>
JavaDoubleRDD javaDoubleRDD = stringJavaRDD.flatMapToDouble(new DoubleFlatMapFunction<String>() {
    @Override
    public Iterator<Double> call(String value) throws Exception {
        String[] values = value.split(" ");
        double[] doubles = Arrays.stream(values).mapToDouble(t -> t.length()).toArray();
        return Arrays.stream(doubles).iterator();
    }
});

// Function<T,Iterator<Double>> 这里是：Function<String,Iterator<Double>>
JavaRDD<Double> flatMapRDD = stringJavaRDD.flatMap(new FlatMapFunction<String, Double>() {
    @Override
    public Iterator<Double> call(String value) throws Exception {
        String[] values = value.split(" ");
        double[] doubles = Arrays.stream(values).mapToDouble(t -> t.length()).toArray();
        return Arrays.stream(doubles).iterator();
    }
});
```

##### PairFunction\<T,K,V\>

```java
// to-du
```

##### PairFlatMapFunction\<T,K,V\>

```java
// to-du
```



### 2.7 持久化（缓存）

```java
// RDD是惰性求值的，有时我们希望能够多次使用同一个RDD。
// 如果简单的调用RDD行动操作，Spark每次都会重新计算RDD以及它的所有依赖。（这样消耗极大！！！）
// 示例： 下面的代码会对map的操作进行两次计算。
JavaRDD<Integer> mapRDD = stringJavaRDD.map(new Function<Integer, Integer>() {
    @Override
    public Integer call(Integer v1) throws Exception {
        return v1 * v1;
    }
});
// 第一次计算-1-count时。
System.out.println(mapRDD.count());
// 第二次计算-2-collect时。
System.out.println(
    Arrays.toString(
        mapRDD.collect().toArray()
    )
);
```

为了<u>避免多次计算同一个RDD</u>，可以让Spark**对数据进行持久化**。



Spark持久化存储一个RDD时，计算出RDD的节点会分别保存他们求出的分区数据。

1. 如果一个有持久化数据的节点发生故障，Spark会在需要用到缓存数据时重算丢失的数据分区。
2. 如果希望节点故障的情况不会拖累我们的执行速度，也可以把数据备份到多个节点上。



出于不同的目的，RDD有**不同的持久化级别**：

如果有必要，可以在**存储级别的末尾加上“_2”来把持久化数据存为两份**。

| 持久化级别                             | 含义                                                         | 补充     |
| :------------------------------------- | :----------------------------------------------------------- | -------- |
| MEMORY_ONLY                            | 将RDD作为反序列化的Java对象存储在**JVM中**。 如果RDD不能装入内存，那么一些分区将不会被缓存，并将在每次需要它们时动态地重新计算。 | 默认级别 |
| MEMORY_AND_DISK                        | 将RDD作为反序列化的Java对象存储在**JVM中**。 如果RDD不能装入内存，则一些分区会被持久化到**磁盘**，并在需要时从那里读取它们。 |          |
| MEMORY_ONLY_SER (Java and Scala)       | 将RDD存储为<u>序列化的</u> Java对象(每个分区一个字节数组)。 这通常比反序列化的对象更节省空间，特别是使用[快速序列化器](https://spark.apache.org/docs/latest/tuning.html)时，但读取时更消耗CPU。 |          |
| MEMORY_AND_DISK_SER (Java and Scala)   | 类似于MEMORY_ONLY_SER，但是将不能装入内存的分区溢出到磁盘中。 |          |
| DISK_ONLY                              | 只存储在**磁盘上**。                                         |          |
| MEMORY_ONLY_2, MEMORY_AND_DISK_2, etc. | 与上面的级别相同，但是在<u>两个集群节点上复制每个分区</u>。  |          |
| OFF_HEAP (experimental)                | 类似于MEMORY_ONLY_SER，但是将数据存储在[堆外内存](https://spark.apache.org/docs/latest/configuration.html#memory-management)。 这需要启用堆外内存。 |          |



#### cache

底层调用的`persist`。

#### persist

可以设置各种持久化级别。默认级别是JVM堆空间。

#### unpersist

手动把持久化的RDD从缓存中移除。



## 三、键值对操作

键值对类型的RDD被称为pairRDD。

他们提供了<u>并行操作各个键</u>或<u>跨界点重新进行数据分组的</u>操作接口。

<u>pairRDD也是RDD，因此同样支持RDD所支持的函数。</u>

### 3.1 创建pairRDD

#### map

```java
JavaRDD<String> stringJavaRDD = javaSparkContext.parallelize(lines);
// 返回的JavaRDD对象
JavaRDD<Tuple2<String, Integer>> pairByMapRDD = stringJavaRDD.map(new Function<String, Tuple2<String, Integer>>() {
  @Override
  public Tuple2<String, Integer> call(String v1) throws Exception {
    return new Tuple2<>(v1,1);
  }
});
```



#### mapToPair

```java
JavaRDD<String> stringJavaRDD = javaSparkContext.parallelize(lines);

// 返回的JavaPairRDD对象
JavaPairRDD<String, Integer> pairByMapToPairRDD = stringJavaRDD.mapToPair(new PairFunction<String, String, Integer>() {
  @Override
  public Tuple2<String, Integer> call(String s) throws Exception {
    return new Tuple2<>(s,1);
  }
});
```



#### 内存中创建pairRDD

scala和python只需要对二元组集合调用`SparkContext.parallelize()`即可。

Java需要使用`SparkContext.parallelizePairs()`。



### 3.2 转化pairRDD

#### 3.2.1 单个pairRDD

##### reduceByKey

合并具有相同键的值。（返回各个键和对应键归约出来的结果值组成的新RDD）

```java
// (spark,1)
// (spark,1)
// (hadoop,1)
// (flink,1)

JavaPairRDD<String, Integer> reduceByKeyRDD = pairByMapToPairRDD.reduceByKey(new Function2<Integer, Integer, Integer>() {
  @Override
  public Integer call(Integer v1, Integer v2) throws Exception {
    return v1 + v2;
  }
});

// (spark,2)
// (hadoop,1)
// (flink,1)
```



##### groupByKey

对相同键的值进行分组。

```java
// (spark,1)
// (spark,1)
// (hadoop,1)
// (flink,1)

JavaPairRDD<String, Iterable<Integer>> groupByKey = pairByMapToPairRDD.groupByKey();

// (spark,[1, 1])
// (hadoop,[1])
// (flink,[1])
```



##### combineByKey

使用<u>不同的返回类型</u>**合并具有相同键的值**。

- combineByKey会遍历分区中的所有元素（每个元素的键要么没遇见过、要么与之前某一个相同）
  - 如果是一个新元素，将使用`createCombiner()`函数创建那个键对应累加器的初始值。（发生在每个分区第一次出现各个键时（不会RDD中第一次））
  - 如果是该分区已经遇到的键，将使用`mergeValue()`方法将该键的累加器对应的当前值与这个新值合并。
- 由于分区都是独立的，因此同一个键可以有多个累加器。
  - 两个或者多个分区都有对应同一个键的累加器，就需要用户使用`mergeCombiners()`将各个分区结果进行合并。

```java
// (spark,1)
// (spark,3)
// (hadoop,1)
// (flink,1)


// 这里计算每个key的value的平均值
JavaPairRDD<String, Tuple2<Double, Integer>> combineByKey = pairRDD.combineByKey(
  // value,value累加器 (初次遇到)
  new Function<Integer, Tuple2<Double, Integer>>() {
    @Override
    public Tuple2<Double, Integer> call(Integer v1) throws Exception {
      return new Tuple2<Double, Integer>((double) v1, 1);
    }
  },
  // value,value累加器 (非第一次遇到)
  new Function2<Tuple2<Double, Integer>, Integer, Tuple2<Double, Integer>>() {
    @Override
    public Tuple2<Double, Integer> call(Tuple2<Double, Integer> v1, Integer v2) throws Exception {
      return new Tuple2<>(v1._1 + (double) v2, v1._2 + 1);
    }
  },
  // 不同分区累加器合并
  new Function2<Tuple2<Double, Integer>, Tuple2<Double, Integer>, Tuple2<Double, Integer>>() {
    @Override
    public Tuple2<Double, Integer> call(Tuple2<Double, Integer> v1, Tuple2<Double, Integer> v2) throws Exception {
      return new Tuple2<>(v1._1 + v2._1, v1._2 + v2._2);
    }
  }
);

//spark----2.0
//hadoop----1.0
//flink----1.0
```



##### mapValues

对键值对的每个值应用一个函数，不改变键。

```java
// (spark,1)
// (spark,1)
// (hadoop,1)
// (flink,1)

JavaPairRDD<String, Integer> mapValues = pairByMapToPairRDD.mapValues(new Function<Integer, Integer>() {
  @Override
  public Integer call(Integer v1) throws Exception {
    return v1 * 2;
  }
});

//(spark,2)
//(flink,2)
//(hadoop,2)
//(spark,2)
```



##### flatMapValues

对键值对的每个值应用一个函数，不改变键。

```java
// (spark,1)
// (spark,1)
// (hadoop,1)
// (flink,1)

JavaPairRDD<String, Integer> flatMapValues = pairByMapToPairRDD.flatMapValues(new FlatMapFunction<Integer, Integer>() {
  @Override
  public Iterator<Integer> call(Integer integer) throws Exception {
    int[] result = {integer, integer + 1};
    return Arrays.stream(result).iterator();
  }
});

//(spark,1)
//(spark,2)
//(flink,1)
//(flink,2)
//(hadoop,1)
//(hadoop,2)
//(spark,1)
//(spark,2)
```



##### keys

返回仅包含key的RDD。

```java
// (spark,1)
// (spark,3)
// (hadoop,1)
// (flink,1)

JavaRDD<String> keys = pairRDD.keys();

String keyArrays = Arrays.toString(keys.collect().toArray());
System.out.println(keyArrays);

// [spark, flink, hadoop, spark]
```



##### values

返回仅包含value的RDD。

```java
// (spark,1)
// (spark,3)
// (hadoop,1)
// (flink,1)

JavaRDD<Integer> values = pairRDD.values();

String valueArrays = Arrays.toString(values.collect().toArray());
System.out.println(valueArrays);

// [1, 1, 1, 3]
```



##### sortByKey

返回根据键排序的RDD。

默认（true）生序，提供参数false表示降序；也可以提供自定义排序：`Comparator`

```java
//(spark,1)
//(zookeeper,1)
//(flink,1)
//(apache,1)
//(hadoop,1)

JavaPairRDD<String, Integer> sortByKey = pairRDD.sortByKey();

//(apache,1)
//(flink,1)
//(hadoop,1)
//(spark,1)
//(zookeeper,1)



// 默认（true）生序，可以不传递参数；
// desc，表示采用降序
JavaPairRDD<String, Integer> sortByKey = pairRDD.sortByKey(false);

sortByKey.foreach(new VoidFunction<Tuple2<String, Integer>>() {
  @Override
  public void call(Tuple2<String, Integer> value) throws Exception {
    System.out.println(value);
  }
});

//(zookeeper,1)
//(spark,1)
//(hadoop,1)
//(flink,1)
//(apache,1)
```

```java
// 自定义排序：这里按照key字符串长度排序(需要实现Serializable)
JavaPairRDD<String, Integer> sortByKey = pairRDD.sortByKey(new TopComparator());

//(spark,1)
//(flink,1)
//(apache,1)
//(hadoop,1)
//(zookeeper,1)
```



#### 3.2.2 两个pairRDD

##### subtractByKey

删掉RDD中与otherRDD键相同的元素。

```java
//firstRDD:
  //(spark,1)
  //(zookeeper,1)
  //(flink,1)
  //(apache,1)
  //(hadoop,1)
//otherRDD:
  //(spark,1)
  //(flink,1)

JavaPairRDD<String, Integer> subtractByKey = firstRDD.subtractByKey(otherRDD);

//(apache,1)
//(zookeeper,1)
//(hadoop,1)
```



##### join

对两个RDD进行内连接。

```java
//firstRDD:
  //(spark,1)
  //(zookeeper,1)
  //(flink,1)
  //(apache,1)
  //(hadoop,1)
//otherRDD:
  //(spark,2)
  //(flink,2)

JavaPairRDD<String, Tuple2<Integer, Integer>> join = firstRDD.join(otherRDD);

//(spark,(1,2))
//(flink,(1,2))
```



##### rightOuterJoin

右外连接。

对两个RDD进行连接操作，确保<u>右侧RDD的键必须存在</u>。

```java
//firstRDD:
  //(spark,1)
  //(zookeeper,1)
  //(flink,1)
  //(apache,1)
  //(hadoop,1)
//otherRDD:
  //(spark,2)
  //(flink,2)

JavaPairRDD<String, Tuple2<Optional<Integer>, Integer>> rightOuterJoin = firstRDD.rightOuterJoin(otherRDD);

//(spark,(Optional[1],2))
//(flink,(Optional[1],2))
```



##### leftOuterJoin

左外连接。

对两个RDD进行连接操作，确保<u>左侧RDD的键必须存在</u>。

```java
//firstRDD:
  //(spark,1)
  //(zookeeper,1)
  //(flink,1)
  //(apache,1)
  //(hadoop,1)
//otherRDD:
  //(spark,2)
  //(flink,2)

JavaPairRDD<String, Tuple2<Integer, Optional<Integer>>> leftOuterJoin = firstRDD.leftOuterJoin(otherRDD);

//(spark,(1,Optional[2]))
//(hadoop,(1,Optional.empty))
//(flink,(1,Optional[2]))
//(zookeeper,(1,Optional.empty))
//(apache,(1,Optional.empty))
```



##### cogroup

将两个RDD中有相同键的组合到一起。（注意与Join的区别！！！）【没有对应的也会输出】

- 可以用来连接。
- 可以求键的交集。
- 还能同时应用于三个以上的RDD。

```java
//firstRDD:
  //(spark,1)
  //(zookeeper,1)
  //(flink,1)
  //(apache,1)
  //(hadoop,1)
//otherRDD:
  //(spark,2)
  //(flink,2)

JavaPairRDD<String, Tuple2<Iterable<Integer>, Iterable<Integer>>> coGroup = firstRDD.cogroup(otherRDD);

//(spark,([1],[2]))
//(hadoop,([1],[]))
//(flink,([1],[2]))
//(zookeeper,([1],[]))
//(apache,([1],[]))
```

### 3.3 并行度调优

Spark始终尝试根据集群大小推出的一个有意义的默认值，但是有时候你可能需要自定义并行度来获取更好的性能表现。

- <u>大多数算子都可以接收第二个参数，用来指定分组结果或聚合结果的RDD分区数目。</u>
- 分组操作和聚合操作之外的操作也可以改变RDD分区。

#### repartition()

重新分区代价很大。会通过网络进行混洗，并创建出新的网络分区。

```java
// 获取当前分区
int partitionSize = pairRDD.partitions().size();

// 通过网络混洗，并创建新的分区集合。
JavaPairRDD<String, Integer> repartitionRDD = pairRDD.repartition(2);

// 混洗后的分区集合
int repartitionSize = repartitionRDD.partitions().size();
```



#### coalesce()

优化的repartition，将RDD合并比现有分区更少的分区。

```java
// 通过网络混洗，并创建新的分区集合。
JavaPairRDD<String, Integer> repartitionRDD = pairRDD.repartition(4);
// 混洗后的分区数目
int repartitionSize = repartitionRDD.partitions().size();

// 通过coalesce缩减分区
JavaPairRDD<String, Integer> coalesceRDD = repartitionRDD.coalesce(4);
// 指定更多的分区不会起作用，最多和之前的一样 此处指定为5，返回的还是4
//        JavaPairRDD<String, Integer> coalesceRDD = repartitionRDD.coalesce(5);
// 缩减后的分区数目
int coalesceSize = coalesceRDD.partitions().size();
```



通过rdd.partitions.size()查看rdd分区数目。



### 3.4 行动pairRDD

#### countByKey()

对每个键对应的元素分别计数。<u>返回Map</u>。

```java
ArrayList<Tuple2<String, Integer>> lines = new ArrayList<Tuple2<String, Integer>>(Arrays.asList(
  new Tuple2<>("spark", 1),
  new Tuple2<>("flink", 1),
  new Tuple2<>("hadoop", 1),
  new Tuple2<>("spark", 3)
));

JavaPairRDD<String, Integer> pairRDD = javaSparkContext.parallelizePairs(lines);

Map<String, Long> stringLongMap = pairRDD.countByKey();

Set<Map.Entry<String, Long>> entries = stringLongMap.entrySet();
for (Map.Entry<String, Long> entry : entries) {
  System.out.println(entry.getKey() + "----" + entry.getValue());
}

//spark----2
//hadoop----1
//flink----1
```



#### collectAsMap()

将此RDD中的键值对作为Map返回给master。
仅当预期结果数据较小时才应使用此方法，因为<u>所有数据都加载到驱动程序的内存中</u>。

map的key是不重复的，所以<u>后续同一个key对应的value会覆盖前面的</u>！！！

```java
ArrayList<Tuple2<String, Integer>> lines = new ArrayList<Tuple2<String, Integer>>(Arrays.asList(
  new Tuple2<>("spark", 1),
  new Tuple2<>("flink", 1),
  new Tuple2<>("hadoop", 1),
  new Tuple2<>("spark", 3)
));

JavaPairRDD<String, Integer> pairRDD = javaSparkContext.parallelizePairs(lines);

Map<String, Integer> collectAsMap = pairRDD.collectAsMap();

Set<Map.Entry<String, Integer>> entries = collectAsMap.entrySet();
for (Map.Entry<String, Integer> entry : entries) {
  System.out.println(entry.getKey() + "----" + entry.getValue());
}

//hadoop----1
//spark----3
//flink----1
```



#### lookup(key)

**返回键 key 的 RDD 中的值列表**。如果 RDD 具有已知的分区器，则此操作通过仅搜索键映射到的分区来有效地完成。

```java
// 这里返回key是spark的所有结果.
List<Integer> keySpark = pairRDD.lookup("spark");

System.out.println(Arrays.toString(keySpark.toArray()));
```



#### other



### 3.5 数据分区

#### 3.5.1 数据分区为何重要？

在分布式程序中，**分区的代价是很大的**，因此<u>控制数据分布以获得最少的网络传输</u>可以极大的提升整体性能。



Spark的所有`键值对RDD`都可以进行分区。

<u>Spark可以确保同一组的键出现在同一个节点上</u>。



#### 3.5.2 数据分区如何优化连接操作？

​	假设有两个表进行连接操作（用户数据表userData、事件日志表events），默认情况下，会将两个数据集中所有键的Hash值求出来，并将相同Hash值的记录通过网络发送到同一个机器上，然后在那台机器上进行连接操作（如图）。

​	userData表可能很大，而且表中的数据可能也没有变化，但是每次调用都会对userData进行哈希计算和跨界点混洗。这样非常浪费时间！！！

<img src="./1 - spark - RDD编程指南.resource/image-20220404172509724.png" alt="image-20220404172509724" style="zoom:67%;" />

通过对userData使用`partitionBy`转化操作：将userData转化为哈希分区。

这样，通过构建时调用partitionBy()，Spark就知道该RDD是通过哈希值分区的，这样调用join时，Spark就会利用这一点：“具体来说，就是只会对另一个表events进行数据混洗操作，将特定key的值发送到userDate对应分区的那台机器上（如下图）。” 这样，需要通过网络传输的数据就大大减少了！

<img src="1 - spark - RDD编程指南.resource/image-20220404172622629.png" alt="image-20220404172622629" style="zoom:67%;" />

提示：

partitionBy是一个转化操作，它会返回一个新的RDD，原来的RDD不会改变（RDD一旦创建也不会改变）。

因此，**应该对parititonBy的结果进行持久化，并保存在新的RDD，而不是做一些输出操作！！！**

（传递给partitionBy的分区数目，会控制之后对这个RDD的进一步操作（比如连接时）有多少任务会并行执行。总的来说，这个值至少应该和集群总核数一样。）



#### 3.5.3 获取RDD分区的方式

##### partitioner()

返回一个scala.Option对象（这是scala存放可能存在的对象的容器类）：

1. 使用`isPresent()`判断是否有值，并使用`get()`获取其中的值。
   - 如果值存在的话，会是一个spark.Partitioner对象。这本质上是一个告诉我们RDD中各个键分别属于那个分区的函数。

```java
JavaPairRDD<String, Integer> pairRDD = javaSparkContext.parallelizePairs(lines);

// 获取RDD的partitioner属性。
Optional<Partitioner> partitioner = pairRDD.partitioner();

// 判断是否有值。
boolean present = partitioner.isPresent();

if (present) {
  System.out.println(partitioner);
} else {
  System.out.println("partitioner is Null");
}


// partitioner is Null
```



**partitioner属性**：不仅是<u>检验各种Spark操作如何影响分区方式的一种好办法</u>，还可以用来<u>在程序中检查想要的操作是否会生成正确的结果</u>。

```java
// 对RDD进行PartitionBy分区操作！！！
// 通常与持久化一起使用，否则后续RDD会一遍又一遍的对pairs进行哈希分区操作。
JavaPairRDD<String, Integer> hashPartitionPairRDD = pairRDD.partitionBy(new HashPartitioner(3));

// 获取RDD的partitioner属性。
Optional<Partitioner> partitioner = hashPartitionPairRDD.partitioner();

// 判断是否有值。
boolean present = partitioner.isPresent();

if (present) {
  System.out.println(partitioner.get());
} else {
  System.out.println("partitioner is Null");
}

// org.apache.spark.HashPartitioner@3
```



#### 3.5.4 从分区中获益的操作

数据<u>根据键进行跨界点的混洗</u>是很常见的。**合理的数据分区**将使<u>这些操作</u>受益！

##### reduceByKey()

​	这种只针对单个RDD的操作，运行在未分区的RDD上会导致每个键所有对应的值都在每台机器上进行本地计算，只需要把本地归约的结果从各个工作节点传回主节点，所以网络开销不大。

##### cogroup() / join()

这一类二元操作，预先进行数据分区会导致至少一个RDD（已知分区的RDD）不发生混洗。

- 如果<u>两个RDD使用同样的分区方式，并且还缓存在同样的机器上</u>；或者其中一个RDD还没有被计算出来**那么跨节点的数据混洗就不会发生**。



#### 3.5.5 影响分区方式的操作

Spark内部知道各操作会如何影响分区方式，并将会<u>对数据进行分区的操作 的 `结果RDD`自动设置为对应的分区器</u>。

- 例如join连接两个RDD，由于键相同的元素会被哈希到同一台机器上，Spark知道结果也是哈希分区的，这样对结果进行诸如reduceByKey这样的操作就会快很多。



不过，转化操作的结果并<u>不一定会按照已知的分区方式分区</u>，这时输出的RDD可能就会没有设置分区器。

- 当哈希分区的键值RDD调用map时，传入map的函数理论上可能改变元素的键，因此不会有固定的分区方式；（Spark不会分析你的函数来判断键是否被保留下来。）
- 但是，Spark提供了另外两个`mapValues()`和`flatMapValues()`作为替代方法，他们可以保证每个<u>二元组</u>的键不变。
  - 为了最大分区相关优化的潜在作用，应该在无需改变元素的键时尽量使用mapValues、flatMapValues。



所有会为生成的结果RDD**设好分区方式的操作**：

```
cogroup

groupWith

join

leftOuterJoin

rightOuterJoin

groupByKey

reduceByKey

combineByKey

partitionBy

sort

mapValues（如果父RDD有分区方式的话）

flatMapValues（如果父RDD有分区方式的话）

filter（如果父RDD有分区方式的话）
```



对于**二元操作**，输出数据的分区方式取决于父RDD的分区方式：

- 默认采用哈希分区，分区的数量和操作的并行度一样。
  - 如果其中一个父RDD已经设置过分区方式，结果就采用那种方式。
  - 如果两个父RDD都设置好了分区方式，结果RDD采用第一个父RDD的分区方式。





### 3.6 自定义分区方式

`HashPartitioner` 和 `RangePartitioner`已经能够满足大多数用例。

同时，Spark也提供了`自定义Parritioner对象`来控制RDD分区的方式。（这可以让你利用领域知识进一步减少通信开销）



要实现自定义分区器，需要继承`org.apache.spark.Partitioner`类并实现三个方法：

1. numPartitions：Int：返回创建出来的分区数目。
2. getPartition(key)：Int：返回指定键的分区编号（0到numPartitions-1）
   - 注意：Java的hashCode方法可能会返回负数，但是getPartition永远返回一个非负数。
3. equals()：Java判断相等性的标准方法。（非常重要），Spark需要使用这个方法来<u>检查你的分区器对象是否和其他分区器实例相同</u>，这样Spark才可以<u>判断两个RDD的分区方式是否相同</u>。

```java
JavaPairRDD<String, Integer> pairRDDWithCustomPartition = pairRDD.partitionBy(new Partitioner() {
  @Override
  public int numPartitions() {
    return 3;
  }

  @Override
  public int getPartition(Object key) {
    String keyString = key.toString();
    if (keyString.length() == 4) {
      return 0;
    } else if (keyString.length() == 5) {
      return 1;
    } else {
      return 2;
    }
  }
});

System.out.println(pairRDDWithCustomPartition.getNumPartitions());
Optional<Partitioner> customPartitioner = pairRDDWithCustomPartition.partitioner();
if (customPartitioner.isPresent()) {
  System.out.println(customPartitioner.get());
} else {
  System.out.println("partitioner is Null !");
}


//3
//example.run.rdd.pair.CreateCustomPartitionWithJavaPairRDDDemo$1@58f4b31a
```





如果你想对多个RDD使用相同的分区方式，应该使用**同一个函数的对象**（比如一个全局函数），而不是为每一个RDD创建一个新的函数对象。



