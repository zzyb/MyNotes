# 编程进阶

## 一、共享变量

共享变量是可以在Spark任务中使用的特殊类型的变量。

例如：

- 使用共享变量对非常严重的情况进行计数。
- 或者分发一个巨大的查询表。



### 1.1 累加器（accumulator）

#### 1.1.1 简介与用例

`累加器`用来<u>对信息进行聚合</u>。

`累加器`提供了**将工作节点中的值聚合到驱动器程序中**的简单语法。

- 常见用途：在调试时对作业执行过程中的事件进行计数。

```java
ArrayList<String> lines = new ArrayList<>(Arrays.asList(
  "spark jjj",
  "spark jjj",
  "spark jjj",
  "hadoop",
  "zookeeper",
  "spark jjj",
  "flink jjj",
  "flink"
));

// 累加器：记录一行多个单词的数据
LongAccumulator notOnlyOneWord = sparkContext.longAccumulator();
// 初始化累加器
notOnlyOneWord.setValue(0L);

JavaRDD<String> flatMapRDD = stringJavaRDD.flatMap(new FlatMapFunction<String, String>() {
  @Override
  public Iterator<String> call(String s) throws Exception {
    String[] values = s.split(" ");
    // 如果一行存在多个单词，就累加器加一
    if (values.length > 1) {
      notOnlyOneWord.add(1L);
    }
    return Arrays.stream(values).iterator();
  }
});

System.out.println(Arrays.toString(flatMapRDD.collect().toArray()));
System.out.println("一行多个单词的数据有：" + notOnlyOneWord.count());

//[spark, jjj, spark, jjj, spark, jjj, hadoop, zookeeper, spark, jjj, flink, jjj, flink]
//一行多个单词的数据有：5
```



```java
// 累加器：记录包含spark的数据
CollectionAccumulator<Object> hasSparkAccumulator = sparkContext.collectionAccumulator();
// 初始化累加器
hasSparkAccumulator.setValue(new ArrayList<>());

JavaRDD<String> flatMapRDD = stringJavaRDD.flatMap(new FlatMapFunction<String, String>() {
  @Override
  public Iterator<String> call(String s) throws Exception {
    // 向集合累加器中添加元素
    if (s.contains("spark")) {
      hasSparkAccumulator.add(s);
    }
    String[] values = s.split(" ");
    return Arrays.stream(values).iterator();
  }
});

System.out.println(Arrays.toString(flatMapRDD.collect().toArray()));
System.out.println("包含spark的数据为：" + Arrays.toString(hasSparkAccumulator.value().toArray()));

// [spark, aaa, spark, bbb, spark, ccc, hadoop, zookeeper, spark, ddd, flink, jjj, flink]
// 包含spark的数据为：[spark aaa, spark bbb, spark ccc, spark ddd] 
```



#### 1.1.2 注意

- <u>只有运行行动操作之后才能看到正确的计数</u>，因为行动操作之前的转化操作是惰性的。
- 工作节点上的任务不能访问累加器的值！！！
  - 累加器只能在驱动器中访问，所以检查也是在驱动程序中完成。



#### 1.1.3 累加器用法

- 在驱动器程序中调用`SparkContext.accumulator(initialValue)`方法，创建出存有初始值的累加器。返回值是`org.apache.spark.Accumulator[T]`对象，T是初始值类型。
- Spark的执行器可以通过+=方法（Java的add）增加累加器的值。
- Spark的驱动器使用累加器的value（Java的`value()`/`setValue()`）来访问累加器的值。



#### 1.1.4 累加器与容错性

​	Spark会自动重新执行失败或者较慢的任务来应对有错误的或者比较慢的机器。对应的结果就是：“<u>同一个函数可能对同一个数据运行了多次</u>，这取决于集群发生了什么”。

##### 这种情况下如何处理累加器？

- 行动算子中使用累加器，Spark只会把每个任务对各累加器的修改应用一次。
  - 因此，如果希望一个无论失败还是重复计算都绝对可靠的累加器，必须放到`foreach()`这样的行动算子里。
- 转化操作中使用累加器，不能得到可靠保证！
  - 转化操作可能会发生不止一次的更新。（转化操作中，累加器常用于调试目的。）



#### 1.1.5 自定义累加器

自定义累加器可以**做更复杂的操作**，比如找出过程中的最大值的而不是将值加起来。

只要该操作同时满足`交换律`和`结合律`，就可以使用任意操作来代替数值上的加法。

```shell
# 交换律
任意a、b，有 a 操作 b = b 操作 a，表示满足交换律。
# 结合律
任意a、b和c，有(a 操作 b) 操作 c = a 操作 (b 操作 c)，表示满足结合律。

# sum和max同时满足交换律和结合律，是Spark的常用操作。
```



自定义累加器：

```java
import org.apache.spark.util.AccumulatorV2;

/**
 * 自定义累加器：这里求出获取到的最大值。
 */
public class CustomAccumulatorGetMaxInteger extends AccumulatorV2<Integer, Integer> {

    private int max = Integer.MIN_VALUE;

    @Override
    public boolean isZero() {
        return max == Integer.MIN_VALUE;
    }

    @Override
    public AccumulatorV2<Integer, Integer> copy() {
        CustomAccumulatorGetMaxInteger copyAcc = new CustomAccumulatorGetMaxInteger();
        copyAcc.max = this.max;
        return copyAcc;
    }

    @Override
    public void reset() {
        max = Integer.MIN_VALUE;
    }

    @Override
    public void add(Integer v) {
        //
        max = Math.max(this.max, v);
    }

    @Override
    public void merge(AccumulatorV2<Integer, Integer> other) {
        max = Math.max(other.value(), max);
    }

    @Override
    public Integer value() {
        return max;
    }
}

```

使用自定义累加器：

```java
// 创建自定义累加器对象
CustomAccumulatorGetMaxInteger customAccumulatorGetMaxInteger = new CustomAccumulatorGetMaxInteger();

// 注册自定义累加器
sparkContext.register(customAccumulatorGetMaxInteger, "getMax");

// 初始化自定义累加器。
customAccumulatorGetMaxInteger.reset();

JavaRDD<Integer> mapRDD = integerJavaRDD.map(new Function<Integer, Integer>() {
    @Override
    public Integer call(Integer v1) throws Exception {
        // 向累加器添加值
        customAccumulatorGetMaxInteger.add(v1);
        return v1;
    }
});


mapRDD.foreach(new VoidFunction<Integer>() {
    @Override
    public void call(Integer integer) throws Exception {
        System.out.println(integer);
    }
});

// 注意：要在行动算子之后
System.out.println(" 累加的最大值是：" + customAccumulatorGetMaxInteger.value());

// 输出：
//12
//33
//2
//98
//37
// 累加的最大值是：98
```



### 1.2 广播变量（broadcast variable）

#### 1.1.1 简介与用例

广播变量用来<u>高效分发较大对象</u>。

- 可以让程序高效的向所有工作节点发送一个较大的只读值，以供一个或多个Spark操作使用。
  - 比如：向所有节点发送一个较大的只读查询表。
  - 比如：机器学习中的一个很大的特征向量。



广播变量其实就是**类型为`spark.broadcasst.Broadcast[T]`的一个对象**，其中存放着类型为T的值。

- 可以在任务中通过对Broadcast调用value来获取值。
- 这个值<u>只会发送到各个节点一次</u>，使用的是一种高效的类似`BitTorrent`的通讯机制。

```java
HashMap<String, String> codes = new HashMap<String, String>();
codes.put("spark", "大规模数据处理的统一分析引擎。");
codes.put("flink", "有状态的分布式流式计算引擎。");
codes.put("hive", "数据仓库");
codes.put("zookeeper", "分布式监控服务");
codes.put("hadoop", "包含hdfs、yarn、mapreduce。");
codes.put("hdfs", "分布式文件系统");
codes.put("yarn", "集群资源管理系统");
codes.put("kafka", "消息中间件");

// 广播变量:将映射转化为广播变量
Broadcast<HashMap<String, String>> hashMapBroadcast = javaSparkContext.broadcast(codes);


ArrayList<String> lines = new ArrayList<>(Arrays.asList(
    "spark hdfs",
    "hadoop",
    "zookeeper",
    "flink kafka"
));

JavaRDD<String> stringJavaRDD = javaSparkContext.parallelize(lines);


JavaRDD<String> flatMapRDD = stringJavaRDD.flatMap(new FlatMapFunction<String, String>() {
    @Override
    public Iterator<String> call(String s) throws Exception {
        String[] values = s.split(" ");
        return Arrays.stream(values).iterator();
    }
});

JavaRDD<Tuple2<String, String>> mapRDD = flatMapRDD.map(new Function<String, Tuple2<String, String>>() {
    @Override
    public Tuple2<String, String> call(String v1) throws Exception {
        // 获取广播变量
        HashMap<String, String> values = hashMapBroadcast.value();
        // 使用广播变量
        String value = values.get(v1);
        if (null != value) {
            return new Tuple2<>(v1, value);
        } else {
            return new Tuple2<>(v1, "not found !");
        }
    }
});

mapRDD.foreach(new VoidFunction<Tuple2<String, String>>() {
    @Override
    public void call(Tuple2<String, String> value) throws Exception {
        System.out.println(value);
    }
});


//(spark,大规模数据处理的统一分析引擎。)
//(hdfs,分布式文件系统)
//(hadoop,包含hdfs、yarn、mapreduce。)
//(zookeeper,分布式监控服务)
//(flink,有状态的分布式流式计算引擎。)
//(kafka,消息中间件)
```

