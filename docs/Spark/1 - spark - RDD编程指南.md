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

要访问这些附加功能，必须确保获得了正确的专用RDD类。
