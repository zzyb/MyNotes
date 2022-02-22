# Spark SQL

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

