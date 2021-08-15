
## 分布式缓存

Flink提供了一个分布式缓存，类似于hadoop，可以**使用户在并行函数中很方便的读取本地文件，并把它放在taskmanager节点中，防止task重复拉取**。
此缓存的工作机制如下：程序注册一个文件或者目录(本地或者远程文件系统，例如hdfs或者s3)，通过ExecutionEnvironment注册缓存文件并为它起一个名称。
当程序执行，Flink自动将文件或者目录复制到所有taskmanager节点的本地文件系统，仅会执行一次。用户可以通过这个指定的名称查找文件或者目录，然后从taskmanager节点的本地文件系统访问它。


## 示例

在ExecutionEnvironment中注册一个文件

在用户函数中访问缓存文件或者目录(这里是一个map函数)。这个函数必须继承RichFunction,因为它需要使用RuntimeContext读取数据

```
package com.huawei.bigdata.flink.examples

import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.configuration.Configuration
import scala.io.Source
//类型转换
import org.apache.flink.api.scala._


/**
 * 测试分布式缓存
 * 1- 注册文件 (本地/HDFS)
 *  env.registerCachedFile("/opt/da/log2.txt","log2")
 * 2- 使用文件
 *  val file = getRuntimeContext.getDistributedCache.getFile("log2")
 * 3- 说明
 * Access the cached file in a user function (here a MapFunction).
 * The function must extend a [RichFunction] class because it needs access to the RuntimeContext.
 */
object FlinkCacheDemoFindbyFile {
  def main(args: Array[String]): Unit = {
    val env = ExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(3)

    // 注册缓存的文件
    env.registerCachedFile("/opt/da/log2.txt","log2")

    val data = env.readTextFile("/opt/da/log1.txt")

    //在map方法中实现RichMapFunction
    //[String,String] 分别对应输入输出类型
    data.map(new RichMapFunction[String, String] {
      var list: List[String] = _

      //重写open方法(这里获取缓存文件数据)
      override def open(parameters: Configuration): Unit = {
        super.open(parameters)
        // 获取缓存的数据
        val file = getRuntimeContext.getDistributedCache.getFile("log2")
        val strings = Source.fromFile(file.getAbsoluteFile).getLines()
        list = strings.toList
      }

      //这里重写map方法 可以在这里使用拿到的分布式缓存数据 编写对应的业务逻辑
      // 此处逻辑：如果缓存文件中存在就返回,否则返回空
      override def map(in: String): String = {
        var middle: String = ""
        if (list.contains(in)) {
          middle = in
        }
        middle
      }
    })
      .map((_,1L))
      .filter(_._1.nonEmpty)
      .groupBy(0)
      .sum(1)
      .print()
  }

}

// 数据文件：
//[root@hw-bigdata1 flink]# cat /opt/da/log1.txt
//GuoYijun,male,50
//CaiXuyu,female,50
//FangBo,female,50
//FangBo,female,50
//FangBo,female,50
//FangBo,female,50
//[root@hw-bigdata1 flink]# cat /opt/da/log2.txt
//FangBo,female,50
//FangBo,female,60
// 输出：
//(FangBo, female, 50, 4)

```


