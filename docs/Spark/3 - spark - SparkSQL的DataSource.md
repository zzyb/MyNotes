# Spark SQL的DataSource

## 一、通用加载/保存函数

​	在最简单的形式中，**默认数据源**(除非 spark.sql.sources.default 另行配置，否则 **parquet**)将用于所有操作。

```java
// Parquet是 Hadoop 生态系统中任何项目都可以使用的一种 列式存储格式 ，无论数据处理框架、数据模型还是编程语言的选择如何。
```

```java
public static void main(String[] args) {
  SparkSession sparkSql = SparkSession
    .builder()
    .master("local[1]")
    .appName("simple Sql")
    .config("", "")
    .getOrCreate();

  // "./data/parquet_data" 是准备的一个测试parquet文件。
  Dataset<Row> parquetData = sparkSql.read().load("./data/parquet_data");

  parquetData.show();

  // 通过select 筛选数据
  parquetData.select("two").show();

}
```

```java
+---+--------+
|one|     two|
+---+--------+
|  1|bilibili|
+---+--------+
  
+--------+
|     two|
+--------+
|bilibili|
+--------+
```

### 1.1 手动指定选项

可以**手动指定将要使用的数据源**<u>以及要传递给数据源的任何其他选项</u>。

- 数据源通过它们的完全限定名（`org.apache.spark.sql.parquet`）来指定。
- 对于内置数据源，使用它们的短名称（json、parquet、jdbc、orc、csv、text、libsvm）。
- 从任何数据源加载的DataFrame都可以使用此语法加载为其他类型。

```java
public static void main(String[] args) {
  SparkSession sparkSql = SparkSession
    .builder()
    .master("local[1]")
    .appName("simple Sql")
    .config("", "")
    .getOrCreate();

  // 指定读取json类型的数据
  Dataset<Row> jsonData = sparkSql.read().format("json").load("./data/people.json");

  jsonData.show();

  // 将json类型的数据转换为text类型的数据(save到本地的toPersonDir目录下)
  jsonData.select("name").write().format("text").save("./data/toPersonDir");
}
```

#### 读取csv

```java
public static void main(String[] args) {
  SparkSession sparkSql = SparkSession
    .builder()
    .master("local[1]")
    .appName("simple Sql")
    .config("", "")
    .getOrCreate();

  // 指定读取json类型的数据
  Dataset<Row> csvData = sparkSql.read()
    .format("csv")
    .option("sep", "\t")// 设置csv的一些属性（字段分隔符）
    .option("inferSchema", "true")// 设置csv的一些属性（是否支持从数据中自动推导出schema）
    .option("header", "true")// 设置csv的一些属性（是否包含表头）
    .load("./data/people.csv");

  csvData.show();
}
```

#### 读取parquet

```java
```

#### 读取orc

```java
// orc 列式存储结构。
```

```java
```



### 1.2 直接在文件上运行sql

```java
public static void main(String[] args) {
  SparkSession sparkSql = SparkSession
    .builder()
    .master("local[1]")
    .appName("simple Sql")
    .config("", "")
    .getOrCreate();

  // ./data/parquet_data 是准备的一个测试parquet文件。
  // sql + fileType.+`filePath`
  Dataset<Row> parquetData = sparkSql.sql("select * from parquet.`./data/parquet_data`");

  parquetData.show();
}
```



### 1.3 保存模式

保存操作可以选择接受 SaveMode，它指定如果存在如何处理现有数据。

- 保存模式不使用锁定，也不是原子模式。
- 覆盖模式时，将会在写出新数据前被删除。

| Java/Scala                   | 含义                               |
| ---------------------------- | ---------------------------------- |
| SaveMode.ErrorIfExists/error | 引发异常                           |
| SaveMode.Append              | 追加内容到现有数据中               |
| SaveMode.Overwrite           | 现有数据被覆盖                     |
| SaveMode.Ignore              | 忽略，类似于create.. if not exists |

```java
public static void main(String[] args) {
  SparkSession sparkSql = SparkSession
    .builder()
    .master("local[1]")
    .appName("simple Sql")
    .config("", "")
    .getOrCreate();

  // ./data/parquet_data 是准备的一个测试parquet文件。
  // sql + fileType.+`filePath`
  Dataset<Row> parquetData = sparkSql.sql("select one from parquet.`./data/parquet_data`");

  parquetData.write()
    .format("text")
    //                .mode(SaveMode.ErrorIfExists) // 默认行为
    //                .mode(SaveMode.Append)
    //                .mode(SaveMode.Ignore)
    .mode(SaveMode.Overwrite)
    .save("./data/toText");
}

```

### 1.4 保存到持久表

