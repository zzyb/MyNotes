# Spark SQL

## 一、简介

### 1.1 定义

用来操作`结构化`和`半结构化`数据的接口——Spark SQL。

### 1.2 三大功能

- 可以从各种结构化数据源读取数据。（JSON、Hive、Parquet等）
- 不仅支持在程序内使用SQL语句进行数据查询，也支持从使用商业软件通过标准数据库连接器（JDBC/ODBC）连接Spark SQL进行查询。
- 程序中使用Spark SQL时，支持SQL与常规的代码高度整合，包括连接RDD与SQL表、公开定义SQL函数接口等。

### 1.3 DataFrame

- DataFrame是一种**特殊的RDD**。<u>存放了Row对象的RDD，每个Row对象表示一行记录</u>。
- DataFrame还<u>包含了记录的结构信息</u>（即数据字段）。
- DataFrame<u>支持SQL查询</u>。
- DataFrame还可以从<u>外部数据源</u>**创建**，也可以<u>从查询结果或普通RDD中</u>**创建**。

### 1.4 DataFrame与DataSet

`DataSet`是分布式的数据集合。

**DataFrame**是一个组织成命名列的`DataSet`。

- Java中：Dataset\<row\> 表示一个DataFrame。
- Scala中：Dataset[Row]类型别名是DataFrame。

## 二、连接Spark SQL

### 2.1 起点: SparkSession

Spark 中所有功能的入口点是 SparkSession 类:

```java
import org.apache.spark.sql.SparkSession;

SparkSession spark = SparkSession
  .builder()
  .appName("Java Spark SQL basic example")
  .config("spark.some.config.option", "some-value")
  .getOrCreate();
```

### 2.2 创建 DataFrames

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

### 2.3 非类型化数据集操作(dataframe operations)

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

### 2.4 以编程的方式运行SQL查询

#### 2.4.1 sql临时视图

SparkSession 上的 SQL 函数使应用程序<u>能够以编程方式运行 SQL 查询，并将结果返回为 Dataset < row ></u> 。

```java
private static void runSimpleSQLFromJsonFile(SparkSession spark) {
  Dataset<Row> fromJsonDataFrame = spark.read().json("./data/people.json");

  //将DataFrame注册为SQL临时视图
  fromJsonDataFrame.createOrReplaceTempView("people");

  Dataset<Row> sqlDF = spark.sql("select name,1 as num from people");

  sqlDF.show();
}
//+-----+---+
//| name|num|
//+-----+---+
//|bruce|  1|
//| coco|  1|
//| lili|  1|
//|  joe|  1|
//| jack|  1|
//|  tom|  1|
//+-----+---+
```

#### 2.4.2 sql全局临时视图

Spark SQL 中的临时视图是会话范围的，如果创建它的会话终止，它将消失。**如果希望有一个在所有会话之间共享的临时视图，并希望在 Spark 应用程序终止之前保持活动状态，则可以创建一个全局临时视图**。<u>全局临时视图绑定到一个系统保存的数据库 global_temp，我们必须使用限定名称来引用它</u>，例如 SELECT * FROM global_temp.people。

```java
private static void runSQLWithGlobalTempViewFromJsonFile(SparkSession spark) {
  Dataset<Row> fromJsonDataFrame = spark.read().json("./data/people.json");

  //将DataFrame注册为SQL全局临时视图
  fromJsonDataFrame.createOrReplaceGlobalTempView("people");

  // 这里要在视图前加global_temp前缀！！！
  Dataset<Row> sqlDF = spark.sql("select name,'global' as type from global_temp.people");
  sqlDF.show();

  // 全局临时视图是跨越会话的。(如果创建为临时视图，下面代码将查不到，报错);
  // 此处不报错!!!
  // org.apache.spark.sql.AnalysisException: Table or view not found: people;
  // 这里使用了newSession来创建新的会话。
  Dataset<Row> newSqlDF = spark.newSession().sql("select name,'newSession' as type from global_temp.people");
  newSqlDF.show();
}
```

```txt
+-----+------+
| name|  type|
+-----+------+
|bruce|global|
| coco|global|
| lili|global|
|  joe|global|
| jack|global|
|  tom|global|
+-----+------+
+-----+----------+
| name|      type|
+-----+----------+
|bruce|newSession|
| coco|newSession|
| lili|newSession|
|  joe|newSession|
| jack|newSession|
|  tom|newSession|
+-----+----------+
```

### 2.5 创建数据集（DataSets）

`DataSets`**类似于RDD**，但是**不是使用**Java Serialization或Kryo，他们<u>使用专用的`编码器`来序列化用于处理或通过网络传输的对象</u>。

- 虽然编码器和标准序列化都负责将对象转换为字节，但是**编码器是动态生成的代码，并允许Spark执行许多操作的格式：“例如过滤、排序、散列等，而无需将字节重新塞回对象。**”



一个易错点：[Spark 需要公共 JavaBean 类。](https://stackoverflow.com/questions/46311201/no-applicable-constructor-method-found-for-zero-actual-parameters-apache-spark)

#### 为Java Bean创建的编码器

```java
SparkSession sparkSql = SparkSession
  .builder()
  .master("local[1]")
  .appName("simple Sql")
  .config("", "")
  .getOrCreate();

// 创建一个Bean类实例。
Person person = new Person();
person.setId(200);
person.setName("goodNight");

// 为Java Bean创建的编码器
Encoder<Person> personBean = Encoders.bean(Person.class);
// SparkSession的createDataset方法传入集合、编码器
Dataset<Person> personDataset = sparkSql.createDataset(Collections.singletonList(person), personBean);

personDataset.show();
```

```txt
+---+---------+
| id|     name|
+---+---------+
|200|goodNight|
+---+---------+
```

#### 大多数常见类型的编码器在类Encoders中提供

```java
SparkSession sparkSql = SparkSession
  .builder()
  .master("local[1]")
  .appName("simple Sql")
  .config("", "")
  .getOrCreate();


// 为基本类型组成的二元组创建编码器
Encoder<Tuple2<Integer, Integer>> tuple2Encoder = Encoders.tuple(Encoders.INT(), Encoders.INT());
// SparkSession的createDataset方法传入集合、编码器
Dataset<Tuple2<Integer, Integer>> tuple2Dataset = sparkSql.createDataset(
  Arrays.asList(
    new Tuple2<Integer, Integer>(1, 1),
    new Tuple2<Integer, Integer>(2, 1),
    new Tuple2<Integer, Integer>(3, 1)
  ),
  tuple2Encoder
);

// tuple2Dataset.show();
List<Tuple2<Integer, Integer>> collect = tuple2Dataset.toJavaRDD().collect();
System.out.println(Arrays.toString(collect.toArray()));
```

```txt
[(1,1), (2,1), (3,1)]
```

#### 提供类将DataFrame转换为DataSet。(基于名称的映射)

```java
SparkSession sparkSql = SparkSession
  .builder()
  .master("local[1]")
  .appName("simple Sql")
  .config("", "")
  .getOrCreate();


// 为Java创建编码器
Encoder<Person> personBean = Encoders.bean(Person.class);
// 对DataFrame设置编码器返回DataSet
Dataset<Person> personDataset = sparkSql.read().json("./data/person.json").as(personBean);

personDataset.show();
```

```txt
+---+-----+
| id| name|
+---+-----+
|100|bruce|
|101| coco|
|102| lili|
|103|  joe|
|104| jack|
|105|  tom|
+---+-----+
```

### 2.6 与RDDs的交互操作

- Spark SQL 支持两种不同的方法，将现有 rdd 转换为`DataSet`。
  - The first method uses reflection to infer the schema of an RDD that contains specific types of objects. This reflection-based approach leads to more concise code and works well when you already know the schema while writing your Spark application.第一个方法<u>使用反射</u>来**推断**包含特定对象类型的 RDD 的架构。当您在编写 Spark 应用程序时已经知道模式时，这种基于反射的方法可以带来更简洁的代码并且工作得很好。
  - The second method for creating Datasets is through a programmatic interface that allows you to construct a schema and then apply it to an existing RDD. While this method is more verbose, it allows you to construct Datasets when the columns and their types are not known until runtime.创建数据集的第二种方法是<u>通过编程接口</u>，该接口允许您**构造**架构，然后将其应用于现有的 RDD。虽然此方法更加详细，但它允许您在直到运行时才知道列及其类型时构造数据集。

#### 2.6.1 使用反射来推断Schema

Spark SQL 支持自动将JavaBean的RDD转换为DataFrame。使用通过反射获得的BeanInfo定义了表的Schema。

目前，SparkSQL**不支持**包含Map字段的JavaBean。但是**支持**嵌套的JavaBean和List或者Array字段。

可以通过创建一个实现了Serializable并为其所有字段提供getter、setter的类来创建JavaBean。

```java
// 从text文件创建Person类型的RDD
JavaRDD<Person> personJavaRDD = sparkSql
  .read().textFile("./data/person.txt")
  .toJavaRDD()
  .map(new Function<String, Person>() {
    @Override
    public Person call(String v1) throws Exception {
      String[] values = v1.split(",");
      Person person = new Person();
      person.setId(Long.parseLong(values[0]));
      person.setName(values[1]);
      return person;
    }
  });

// 将Schema应用到JavaBean类型的RDD获得DataFrame（DataFrame = DataSet<Row>）
Dataset<Row> personDF = sparkSql.createDataFrame(personJavaRDD, Person.class);
// 将DataFrame注册为临时视图
personDF.createOrReplaceTempView("person");
// 通过使用Spark提供的SQL方法运行SQL语句
Dataset<Row> resultSql = sparkSql.sql("select name from person where id between 300 and 301");

// resultSql.show();

Encoder<String> stringEncoder = Encoders.STRING();
// 一、通过字段索引访问字段行中的列
Dataset<String> getResultDFByIndex = resultSql.map(
  new MapFunction<Row, String>() {
    @Override
    public String call(Row value) throws Exception {
      return "Name:" + value.getString(0);
    }
  },
  stringEncoder
);

getResultDFByIndex.show();

// 二、也可以通过字段名
Dataset<String> getResultDFByName = resultSql.map(
  new MapFunction<Row, String>() {
    @Override
    public String call(Row value) throws Exception {
      return "Name:" + value.getAs("name");
    }
  },
  stringEncoder
);
getResultDFByName.show();
```

```txt
+---------+
|    value|
+---------+
|Name:曹操|
|Name:孙权|
+---------+
+---------+
|    value|
+---------+
|Name:曹操|
|Name:孙权|
+---------+
```



#### 2.6.2 编程的方式制定Schema

当 **JavaBean 类不能提前定义时**(例如，记录的结构编码为字符串，或者解析文本数据集并对于不同的用户，字段将被不同地投影) ,可以通过**三个步骤以编程方式创建 Dataset < row >** 。

1. 从原始RDD创建Rows的RDD。
2. 创建符合Rows类型RDD结构的Schema。
3. 通过SparkSession的CreateDataFrame方法将Schema应用于Rows。

```java
// 从text文件创建Person类型的RDD
JavaRDD<String> stringJavaRDD = sparkSql.read().textFile("./data/person.txt").toJavaRDD();

// 使用字符串编码Schema
String schemaString = "id name";

// 基于字符串Schema生成Schema。
ArrayList<StructField> structFields = new ArrayList<>();
for (String value : schemaString.split(" ")) {
  StructField field = DataTypes.createStructField(value, DataTypes.StringType, true);
  structFields.add(field);
}
StructType schema = DataTypes.createStructType(structFields);

// 将RDD转换为Rows
JavaRDD<Row> rowJavaRDD = stringJavaRDD.map(new Function<String, Row>() {
  @Override
  public Row call(String v1) throws Exception {
    String[] values = v1.split(",");
    return RowFactory.create(values[0], values[1].trim());
  }
});

// 将schema应用到Rows的RDD，返回DataSet
Dataset<Row> personDF = sparkSql.createDataFrame(rowJavaRDD, schema);

// 将DataFrame注册为临时视图
personDF.createOrReplaceTempView("person");
// 通过使用Spark提供的SQL方法运行SQL语句
Dataset<Row> resultSql = sparkSql.sql("select name from person where id between 300 and 301");

resultSql.show();

// SQL查询的结果是Dataframe并支持所有正常的RDD操作
// 结果中的行列可以由字段索引或字段名称访问
```

### 2.7 标量函数

<u>标量函数</u>是**每行返回一个值的函数**，而聚合函数则返回一组行的值。Spark SQL 支持各种内置标量函数。它还支持用户定义的标量函数。

### 2.8 聚合函数

<u>聚合函数</u>是**在一组行上返回单个值的函数**。内置的聚合函数提供常见的聚合，如 count ()、 count _ distinct ()、 avg ()、 max ()、 min ()等。用户不受预定义聚合函数的限制，可以创建自己的聚合函数。

