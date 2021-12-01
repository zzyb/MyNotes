# 什么是Spark？

Spark是一个用于大规模数据处理的统一分析引擎。

Apache Spark is a unified analytics engine for large-scale data processing. 

## Spark SQL

​	Spark SQL 是**用于结构化数据处理的 Spark 模块**。与基本的 Spark RDD API 不同，Spark SQL 提供的接口为 Spark 提供了关于数据结构和所执行计算的更多信息。

​	在内部，Spark SQL 使用这些额外的信息来执行额外的优化。有几种方法可以与 Spark SQL 交互，包括 SQL 和 Dataset API。

​	<u>当计算结果时，使用相同的执行引擎，与使用哪种 API/语言表示计算无关</u>。这种统一意味着开发人员可以很容易地在不同的 api 之间来回切换，基于这些 api 提供了表达给定转换的最自然的方式。

## DataSet

​	**DataSet是分布式的数据集合。**Dataset 是在 Spark 1.6中添加的一个新接口，它提供了 RDDs (强类型化、使用强大 lambda 函数的能力)和 Spark SQL 优化的执行引擎的好处。可以从 JVM 对象构建 Dataset，然后使用函数转换(map、 flatMap、 filter 等)对其进行操作。

​	数据集 API 可以在 Scala 和 Java 中使用。Python 没有对 Dataset API 的支持。但是由于 Python 的动态特性，Dataset API 的许多优点已经可用(例如，您可以通过名称自然地访问行的字段 row.columnName)。R 的情况类似。

## DataFrame 

​	A DataFrame is **a *Dataset* organized into named columns**. It is conceptually equivalent to a table in a relational database or a data frame in R/Python, but with richer optimizations under the hood.

​	DataFrame 是组织成**命名列的数据集**。它在概念上**相当于关系数据库文件中的表或者 r/Python 中的数据框架**，但是在后台进行了更加丰富的优化。

​	<u>可以从一系列广泛的数据源构建 DataFrames</u>，例如: 结构化数据文件、 Hive 中的表、外部数据库或现有 rdd。可以在 Scala、 Java、 Python 和 r 中使用。

​	<u>在 Scala 和 Java 中，DataFrame 由行数据集表示</u>。在 Scala API 中，DataFrame 只是 Dataset [ Row ]的类型别名。而在 javaapi 中，用户需要使用 Dataset < row > 来表示一个 DataFrame。

​	



# Spark-Shell

```scala
[root@bigdata-4 spark-3.1.2]# spark-shell
21/11/22 09:38:33 WARN NativeCodeLoader: Unable to load native-hadoop library for your platform... using builtin-java classes where applicable
Using Spark's default log4j profile: org/apache/spark/log4j-defaults.properties
Setting default log level to "WARN".
To adjust logging level use sc.setLogLevel(newLevel). For SparkR, use setLogLevel(newLevel).
Spark context Web UI available at http://bigdata04:4040
Spark context available as 'sc' (master = local[*], app id = local-1637545141181).
Spark session available as 'spark'.
Welcome to
      ____              __
     / __/__  ___ _____/ /__
    _\ \/ _ \/ _ `/ __/  '_/
   /___/ .__/\_,_/_/ /_/\_\   version 3.1.2
      /_/
         
Using Scala version 2.12.10 (Java HotSpot(TM) 64-Bit Server VM, Java 1.8.0_311)
Type in expressions to have them evaluated.
Type :help for more information.

scala> val testFile = spark.read.textFile("/root/hosts")
testFile: org.apache.spark.sql.Dataset[String] = [value: string]

scala> testFile.count()
res0: Long = 7                                                                  

scala> testFile.first()
res1: String = 127.0.0.1   localhost localhost.localdomain localhost4 localhost4.localdomain4

scala> val linesWithBigdata = testFile.filter(line => line.contains("bigdata"))
linesWithBigdata: org.apache.spark.sql.Dataset[String] = [value: string]

scala> linesWithBigdata.count()
res2: Long = 4                                                                  

scala> linesWithBigdata.first()
res3: String = 192.168.1.41 bigdata01

scala> testFile.map(line => line.size).reduce((a,b)=> if(a>b) a else b)
res4: Int = 78

// 使用 identity (表示 x = > x 的一种“好”的方式)
scala> testFile.flatMap(line => line.split(" ")).groupByKey(identity).count().collect()
res9: Array[(String, Long)] = Array((bigdata03,1), (localhost.localdomain,2), (192.168.1.42,1), (localhost,2), (localhost6,1), (localhost4,1), (bigdata04,1), (192.168.1.44,1), (192.168.1.43,1), (::1,1), (localhost6.localdomain6,1), (bigdata02,1), (bigdata01,1), ("",11), (127.0.0.1,1), (localhost4.localdomain4,1), (192.168.1.41,1))

scala> testFile.flatMap(line => line.split(" ")).groupByKey(x => x).count().collect()
res10: Array[(String, Long)] = Array((bigdata03,1), (localhost.localdomain,2), (192.168.1.42,1), (localhost,2), (localhost6,1), (localhost4,1), (bigdata04,1), (192.168.1.44,1), (192.168.1.43,1), (::1,1), (localhost6.localdomain6,1), (bigdata02,1), (bigdata01,1), ("",11), (127.0.0.1,1), (localhost4.localdomain4,1), (192.168.1.41,1))
```



