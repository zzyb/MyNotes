# Spark SQL

## 一、简介

用来操作`结构化`和`半结构化`数据的接口——Spark SQL。

### 三大功能

- 可以从各种结构化数据源读取数据。（JSON、Hive、Parquet等）
- 不仅支持在程序内使用SQL语句进行数据查询，也支持从使用商业软件通过标准数据库连接器（JDBC/ODBC）连接Spark SQL进行查询。
- 程序中使用Spark SQL时，支持SQL与常规的代码高度整合，包括连接RDD与SQL表、公开定义SQL函数接口等。

### SchemaRDD

- SchemaRDD是一种**特殊的RDD**。<u>存放了Row对象的RDD，每个Row对象表示一行记录</u>。
- SchemaRDD还<u>包含了记录的结构信息</u>（即数据字段）。
- SchemaRDD<u>支持SQL查询</u>。
- SchemaRDD还可以从<u>外部数据源</u>**创建**，也可以<u>从查询结果或普通RDD中</u>**创建**。



## 二、连接Spark SQL





## 开始

### 起点: SparkSession

Spark 中所有功能的入口点是 SparkSession 类:

```java
import org.apache.spark.sql.SparkSession;

SparkSession spark = SparkSession
  .builder()
  .appName("Java Spark SQL basic example")
  .config("spark.some.config.option", "some-value")
  .getOrCreate();
```

### 创建 DataFrames

使用 SparkSession，应用程序可以从现有的 RDD、 Hive 表或 Spark 数据源创建 DataFrames。

```java
public static void main(String[] args) {

    SparkSession spark = SparkSession
        .builder()
        .appName("Java Spark SQL data sources example")
        .master("local[*]")
        .config("spark.some.config.option", "some-value")
        .getOrCreate();

    Dataset<Row> jsonDataFrame = spark.read().json("./people.json");

    jsonDataFrame.show();
//      +---+----+
//      | id|name|
//      +---+----+
//      |100|  zs|
//      |101|  ls|
//      |102| wmz|
//      |103|  ww|
//      +---+----+
}
```

### 非类型化数据集操作(dataframe operations)

DataFrames 只是 Scala 和 Java API 中的行数据集。这些操作也被称为“非类型化转换”，与强类型 Scala/Java 数据集中的“类型化转换”形成对比。

更多参考：[API - DataSet ](https://spark.apache.org/docs/latest/api/java/org/apache/spark/sql/Dataset.htm)

```java
jsonDataFrame.printSchema();
    //root
    //  |-- id: string (nullable = true)
    //  |-- name: string (nullable = true)
jsonDataFrame.select("id").show();
    //+---+
    //| id|
    //+---+
    //|100|
    //|101|
    //|102|
    //|103|
    //+---+
jsonDataFrame.select(col("id").plus(1),col("name")).show();
    //+--------+----+
    //|(id + 1)|name|
    //+--------+----+
    //|   101.0|  zs|
    //|   102.0|  ls|
    //|   103.0| wmz|
    //|   104.0|  ww|
    //+--------+----+
jsonDataFrame.filter(col("id").gt(102)).show();
    //+---+----+
    //| id|name|
    //+---+----+
    //|103|  ww|
    //+---+----+
jsonDataFrame.groupBy("name").count().show();
    //+----+-----+
    //|name|count|
    //+----+-----+
    //| wmz|    1|
    //|  ww|    1|
    //|  zs|    1|
    //|  ls|    1|
    //+----+-----+
```

