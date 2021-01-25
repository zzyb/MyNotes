## 简介

Apache Flink具有两个关系API - 表API和SQL - 用于统一流和批处理。Table API是Scala和Java的语言集成查询API，允许以非常直观的方式组合来自关系运算符的查询，Table API和SQL接口彼此紧密集成，以及Flink的DataStream和DataSet API。您可以轻松地在基于API构建的所有API和库之间切换。例如，您可以使用CEP库从DataStream中提取模式，然后使用Table API分析模式，或者可以在预处理上运行Gelly图算法之前使用SQL查询扫描，过滤和聚合批处理表数据。

## Flink SQL的编程模型


### 创建一个TableEnvironment
TableEnvironment是Table API和SQL集成的核心概念，它主要负责:
　　1、在内部目录中注册一个Table
　　2、注册一个外部目录
　　3、执行SQL查询
　　4、注册一个用户自定义函数(标量、表及聚合)
　　5、将DataStream或者DataSet转换成Table
　　6、持有ExecutionEnvironment或者StreamExecutionEnvironment的引用
一个Table总是会绑定到一个指定的TableEnvironment中，相同的查询不同的TableEnvironment是无法通过join、union合并在一起。
TableEnvironment有一个在内部通过表名组织起来的表目录，Table API或者SQL查询可以访问注册在目录中的表，并通过名称来引用它们。

### 在目录中注册表
TableEnvironment允许通过各种源来注册一个表:

　　1、一个已存在的Table对象，通常是Table API或者SQL查询的结果
         Table projTable = tableEnv.scan("X").select(...);

　　2、TableSource，可以访问外部数据如文件、数据库或者消息系统
         TableSource csvSource = new CsvTableSource("/path/to/file", ...);

　　3、DataStream或者DataSet程序中的DataStream或者DataSet
         //将DataSet转换为Table
         Table table= tableEnv.fromDataSet(tableset);

### 注册TableSink	

注册TableSink可用于将 Table API或SQL查询的结果发送到外部存储系统，例如数据库，键值存储，消息队列或文件系统（在不同的编码中，例如，CSV，Apache [Parquet] ，Avro，ORC]，......）:
```scala
val sink = new CsvTableSink("/tmp/SQLTEST3.txt", "\t")
```
```scala
    val fieldnames = Array("clazz", "total")
    // val fieldtypes = Array[Types](Types.STRING,Types.DOUBLE)
    tenv.registerTableSink(
     "SQLTEST",
      fieldnames,  // 可以以直接写成Array形式
      //fieldtypes
      Array(Types.STRING,Types.DOUBLE), 
      sink
    )
```

## 实战案例一

基于Flink SQL的WordCount:

```scala
package com.huawei.bigdata.flink.examples.sql

import org.apache.flink.table.api.TableEnvironment
import org.apache.flink.api.scala.{DataSet, ExecutionEnvironment}
import org.apache.flink.api.scala._
import org.apache.flink.table.api.scala._
import scala.collection.mutable.ArrayBuffer


object WordCountSQL {
  def main(args: Array[String]): Unit = {

    val env = ExecutionEnvironment.getExecutionEnvironment
    val tenv = TableEnvironment.getTableEnvironment(env)

    val list = ArrayBuffer[Word]()

    val value = "hello flink hello yber"
    val strings: Array[String] = value.split(" ")
    for(v <- strings){
      list += Word(v,1)
    }

    val input: DataSet[Word] = env.fromCollection(list)

    tenv.registerDataSet("namewordcount",input)

//    val table = tenv.sqlQuery("SELECT word,SUM(frequency) FROM namewordcount GROUP BY word")
//    val result: DataSet[Word] = tenv.toDataSet[Word](table)
//    输出：
//    Word(flink, 1)
//    Word(hello, 2)
//    Word(yber, 1)


    val table = tenv.sqlQuery("SELECT word,'自定义' FROM namewordcount")
    val result: DataSet[(String,String)] = tenv.toDataSet[(String,String)](table)
//    输出:
//    (hello,自定义)
//    (flink,自定义)
//    (hello,自定义)
//    (yber,自定义)

    result.print()

  }
  case class Word(word:String,frequency:Int)
}

```
## 实战案例二

本例稍微复杂，首先读取一个文件中的内容进行统计，并写入到另外一个文件中：

```scala
import org.apache.flink.api.common.functions.MapFunction
import org.apache.flink.api.common.typeinfo.Types
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.api.scala._
import org.apache.flink.table.api.TableEnvironment
import org.apache.flink.table.sinks.CsvTableSink


object SQLTest {
  def main(args: Array[String]): Unit = {
    val env = ExecutionEnvironment.getExecutionEnvironment
    val tenv = TableEnvironment.getTableEnvironment(env)
    env.setParallelism(1)

    val input = env.readTextFile("/opt/da/hello.txt")
    val topInput = input.map(new MapFunction[String, Orders] {
      override def map(t: String): Orders = {
        val strings = t.split(",")
        Orders(strings(0), strings(1), strings(2).toDouble)
      }
    })
        
    val order = tenv.fromDataSet(topInput)
    tenv.registerTable("Orders",order)
    val tresult = tenv.scan("Orders").select("name")
    tresult.printSchema()
    val sql = tenv.sqlQuery("select clazz,avg(score) as avgscore from Orders group by clazz order by avgscore")

    val sink = new CsvTableSink("/tmp/SQLTEST3.txt", "\t")

    val fieldnames = Array("clazz", "total")
    tenv.registerTableSink(
     "SQLTEST",
      fieldnames,  // 可以以直接写成Array形式
      Array(Types.STRING,Types.DOUBLE), //val fieldtypes = Array[Types](Types.STRING,Types.DOUBLE)
      sink
    )
        
    sql.insertInto("SQLTEST")
    env.execute("**sql***")
//    val output = tenv.toDataSet[(String,Double)](sql)
//    output.print()        //输出测试
  }
    
  //数据源映射类
  case class Orders(clazz:String,name:String,score:Double)
  //统计结果映射类 //这里没有使用类，采用二元组声明代替了 toDataSet[(String,Double)](sql)
  case class Result(clazz:String,score:Double)
}
```

数据与输出：

```shell
//文本数据
[root@hw-bigdata1 tmp]# cat /opt/da/hello.txt
a,acc,80
a,tom,88
a,mim,90
a,jack,100
b,hk,80
b,yu,90
b,jj,77
c,tt,99
c,rili,100
c,popo,100

//生成的输出文件
[root@hw-bigdata1 tmp]# cat SQLTEST3.txt 
b	82.33333333333333
a	89.5
c	99.66666666666667
```

