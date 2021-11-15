# Flink DataStream API

## 什么是DataStream？

The DataStream API gets its name from the special `DataStream` class that is used to represent a collection of data in a Flink program. You can think of them as immutable collections of data that can contain duplicates. This data can either be finite or unbounded, the API that you use to work on them is the same.

DataStream API 的名称来自特殊的 **DataStream 类**，该类<u>用于表示一个 flink 程序中的数据集合</u>。您可以将它们视为<u>可以包含重复项的不可变数据集合</u>。这些数据<u>可以是有限的，也可以是无限的</u>，您用来处理这些数据的 API 是相同的。

A `DataStream` is similar to a regular Java `Collection` in terms of usage but is quite different in some key ways. They are immutable, meaning that once they are created you cannot add or remove elements. You can also not simply inspect the elements inside but only work on them using the `DataStream` API operations, which are also called transformations.

DataStream 在用法上类似于普通的 Java 集合，但在一些关键方面有很大的不同。它们是不可变的，这意味着一旦创建了它们，就不能添加或删除元素。您也不能简单地检查内部的元素，而<u>只能使用 DataStream API 操作来处理它们，这也称为转换</u>。

You can create an initial `DataStream` by adding a source in a Flink program. Then you can derive new streams from this and combine them by using API methods such as `map`, `filter`, and so on.

您可以通过在 flink 程序中添加源来创建一个初始的 DataStream。然后，您可以从中派生出新的流，并使用诸如 map、 filter 等 API 方法将它们组合起来。



## Flink程序的执行步骤

Flick 程序看起来像是转换数据流的常规程序，每个程序都由相同的基本部分组成:

1. Obtain an `execution environment` 取得执行环境。【1】
2. Load/create the initial data, 加载/创建初始数据。【2】
3. Specify transformations on this data, 在此数据上指定转换。【3】
4. Specify where to put the results of your computations, 指定将计算结果放在哪里。【4】
5. Trigger the program execution 触发程序执行。【5】



**所有的 flink 程序都是惰性执行的**: 当程序的 main 方法执行时，数据加载和转换不会直接发生。相反，每个操作都被创建并添加到数据流图中。当执行被执行环境上的 execute ()调用显式触发时，实际上会执行这些操作。程序是在本地执行还是在集群上执行取决于执行环境的类型。

延迟计算允许您构造复杂的程序，而 flink 作为一个整体计划单元执行。



# Flink Data Source

## 内置 Data Source

**Flink Data Source 用于定义 Flink 程序的数据来源**，Flink 官方提供了多种数据获取方法，用于帮助开发者简单快速地构建输入流，具体如下：

### 1 基于文件构建

**1. readTextFile(path)**：按照 TextInputFormat 格式读取文本文件，并将其内容以字符串的形式返回。示例如下：

```java
StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

DataStreamSource<String> source = env.readTextFile("C:\\Users\\Lenovo\\Desktop\\hdfs常用命令.txt");

source.print();

env.execute("Fraud Detection");
```

**2. readFile(fileInputFormat, path)** ：按照指定格式读取文件。

**3. readFile(inputFormat, filePath, watchType, interval, typeInformation)**：按照指定格式周期性的读取文件。其中各个参数的含义如下：

- **inputFormat**：数据流的输入格式。
- **filePath**：文件路径，可以是本地文件系统上的路径，也可以是 HDFS 上的文件路径。
- **watchType**：读取方式，它有两个可选值，分别是 `FileProcessingMode.PROCESS_ONCE` 和 `FileProcessingMode.PROCESS_CONTINUOUSLY`：前者表示对指定路径上的数据只读取一次，然后退出；后者表示对路径进行定期地扫描和读取。需要注意的是如果 watchType 被设置为 `PROCESS_CONTINUOUSLY`，那么当文件被修改时，其所有的内容 (包含原有的内容和新增的内容) 都将被重新处理，因此这会打破 Flink 的 *exactly-once* 语义。
- **interval**：定期扫描的时间间隔。
- **typeInformation**：输入流中元素的类型。

```java

/**
     * Reads the given file line-by-line and creates a data stream that contains a string with the
     * contents of each such line. The {@link java.nio.charset.Charset} with the given name will be
     * used to read the files.
     *
     * <p><b>NOTES ON CHECKPOINTING: </b> The source monitors the path, creates the {@link
     * org.apache.flink.core.fs.FileInputSplit FileInputSplits} to be processed, forwards them to
     * the downstream readers to read the actual data, and exits, without waiting for the readers to
     * finish reading. This implies that no more checkpoint barriers are going to be forwarded after
     * the source exits, thus having no checkpoints after that point.
     *
     * @param filePath The path of the file, as a URI (e.g., "file:///some/local/file" or
     *     "hdfs://host:port/file/path")
     * @param charsetName The name of the character set used to read the file
     * @return The data stream that represents the data read from the given file as text lines
     */
public DataStreamSource<String> readTextFile(String filePath, String charsetName) {
    Preconditions.checkArgument(
        !StringUtils.isNullOrWhitespaceOnly(filePath),
        "The file path must not be null or blank.");

    TextInputFormat format = new TextInputFormat(new Path(filePath));
    format.setFilesFilter(FilePathFilter.createDefaultFilter());
    TypeInformation<String> typeInfo = BasicTypeInfo.STRING_TYPE_INFO;
    format.setCharsetName(charsetName);

    return readFile(format, filePath, FileProcessingMode.PROCESS_ONCE, -1, typeInfo);
}
```

### 2基于集合的构建

**1、fromCollection(Collection)** 

<u>集合中的所有元素必须具有相同的类型。</u>

```java
StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

//ArrayList<String> lists = new ArrayList<>();
//lists.add("aaa");
//lists.add("bbb");
//lists.add("ccc");
//DataStreamSource<String> source = env.fromCollection(lists);

TreeSet<String> sets = new TreeSet<>();
sets.add("111");
sets.add("122");
sets.add("133");
DataStreamSource<String> source = env.fromCollection(sets);

source.print();

env.execute("Collection ")
```



### 3基于Socket构建

**1、socketTextStream**

```java
StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//Server端首先执行 nc -lk 9999
//Server端首先执行 nc -lk 9999
//Server端首先执行 nc -lk 9999,然后Server发送信息即可。
DataStreamSource<String> source = env.socketTextStream("bigdata01",9999); //Server端首先执行 nc -lk 9999

source.print();

env.execute("Socket")
```



## 自定义Data Source



## Streaming Connectors

**内置连接器：**

flink内置多种连接器。满足大多数数据收集场景。

- ###### 1Kafka（source 和 sink）

- ###### 2Hadoop FileSystem（sink)

- ###### 3Elasticsearch（sink）

### 整合kafka(source)

1. 核对版本，确定依赖。

   | Maven 依赖                      | Flink 版本 | Consumer and Producer 类的名称              | Kafka 版本 |
   | ------------------------------- | ---------- | ------------------------------------------- | ---------- |
   | flink-connector-kafka-0.8_2.11  | 1.0.0 +    | FlinkKafkaConsumer08 FlinkKafkaProducer08   | 0.8.x      |
   | flink-connector-kafka-0.9_2.11  | 1.0.0 +    | FlinkKafkaConsumer09 FlinkKafkaProducer09   | 0.9.x      |
   | flink-connector-kafka-0.10_2.11 | 1.2.0 +    | FlinkKafkaConsumer010 FlinkKafkaProducer010 | 0.10.x     |
   | flink-connector-kafka-0.11_2.11 | 1.4.0 +    | FlinkKafkaConsumer011 FlinkKafkaProducer011 | 0.11.x     |
   | flink-connector-kafka_2.11      | 1.7.0 +    | FlinkKafkaConsumer FlinkKafkaProducer       | >= 1.0.0   |

   ```xml
   <dependency>
     <groupId>org.apache.flink</groupId>
     <artifactId>flink-connector-kafka-0.11_2.11</artifactId>
     <version>${flink.kafka.version}</version>
   </dependency>
   ```

2. 代码开发

```java
public class FlinkSourceKafka {
  public static void main(String[] args) throws Exception {
    final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
    Properties properties = new Properties();
    properties.setProperty("bootstrap.servers","hadoop01:9092");
    DataStreamSource<String> stream = env.addSource(
      new FlinkKafkaConsumer011<String>("flink-stream-in-topic", new SimpleStringSchema(), properties)
    );
    stream.print();
    env.execute("flink streaming");
  }
}
```



# Flink Sink

## Data Sinks



## Streaming Connectors



### 整合Kafka Sink

- Flink 提供了 addSink方法来调用自定义Sink或者第三方连接器。

```java
//定义Flink Kafka生产者---多种实现方式1-----------------------------------------
FlinkKafkaProducer011<String> kafkaProducer =
  new FlinkKafkaProducer011<String>(
  "flink-stream-out-topic",
  new KeyedSerializationSchema<String>() {
    public byte[] serializeKey(String element) {
      return element.getBytes();
    }
    public byte[] serializeValue(String element) {
      return element.getBytes();
    }
    public String getTargetTopic(String element) {
      return null;
    }
  },
  properties,
  FlinkKafkaProducer011.Semantic.EXACTLY_ONCE
);

stream.addSink(kafkaProducer);
env.execute("flink streaming");


//定义Flink Kafka生产者---多种实现方式2-----------------------------------------
FlinkKafkaProducer011 kafkaproducer11x = 
  new FlinkKafkaProducer011(
        "localhost:9092","flink-stream-out-topic",new SimpleStringSchema()
	);

stream.addSink(kafkaproducer11x);
env.execute("flink streaming");
```

