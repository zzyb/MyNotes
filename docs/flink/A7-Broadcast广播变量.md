## 广播变量简介

在Flink中，同一个算子可能存在若干个不同的并行实例，计算过程可能不在同一个Slot中进行，不同算子之间更是如此，因此不同算子的计算数据之间不能像Java数组之间一样互相访问，而广播变量Broadcast便是解决这种情况的。

​	1、从clinet端将一份需要反复使用的数据封装到广播变量中，分发到每个TaskManager的内存中保存

​	2、TaskManager中的所有Slot所管理的线程在执行task的时候如果需要用到该变量就从TaskManager的内存中读取数据，达到数据共享的效果。

**注意点：**

1、广播变量中封装的数据集大小要适宜，太大，容易造成OOM。[广播出去的数据，会常驻内存，除非程序执行结束]

2、广播变量中封装的数据要求能够序列化，否则不能在集群中进行传。

3、广播变量在初始化广播出去以后不支持修改，这样才能保证每个节点的数据都是一致的。

## 步骤：

1、设置广播变量

　　在某个需要用到该广播变量的算子后调用withBroadcastSet(var1, var2)进行设置，var1为需要广播变量的变量名，var2是自定义变量名，为String类型。注意，被广播的变量只能为DataSet类型，不能为List、Int、String等类型。

2、获取广播变量

​	创建该算子对应的富函数类，例如map函数的富函数类是RichMapFunction，该类有两个构造参数，第一个参数为算子输入数据类型，第二个参数为算子输出数据类型。首先创建一个  集合  用于接收广播变量并初始化为空，接收类型与算子输入数据类型相对应；然后重写open函数，通过getRuntimeContext.getBroadcastVariable[_](var)获取到广播变量，var即为设置广播变量时的自定义变量名，类型为String，open函数在算子生命周期的初始化阶段便会调用；最后在map方法中对获取到的广播变量进行访问及其它操作。

## 代码用法

```scala
1：初始化数据
    //将准备文件数据(封装为DataSet)作为共享变量
    val broad: DataSet[String] = env.readTextFile("/opt/da/broadcast.txt")
2：广播数据
    //withBroadcastSet广播Broadcast 并在内部通过getRuntimeContext获取共享变量
	.withBroadcastSet(broad,"BroadCast")
3：获取数据
	//获取共享变量 作为一个List
	val bc: java.util.List[String] = getRuntimeContext.getBroadcastVariable[String]("BroadCast")

```

## 注意事项

### 使用广播状态，task 之间不会相互通信

只有广播的一边可以修改广播状态的内容。用户必须保证所有 operator 并发实例上对广播状态的     修改行为都是一致的。或者说，如果不同的并发实例拥有不同的广播状态内容，将导致不一致的结果。
### 广播状态中事件的顺序在各个并发实例中可能不尽相同

广播流的元素保证了将所有元素（最终）都发给下游所有的并发实例，但是元素的到达的顺序可能在并发实例之间并不相同。因此，对广播状态的修改不能依赖于输入数据的顺序。

### 所有operator task都会快照下他们的广播状态
在checkpoint时，所有的 task 都会 checkpoint 下他们的广播状态，随着并发度的增加，checkpoint 的大小也会随之增加
### 广播变量存在内存中

广播出去的变量存在于每个节点的内存中，所以这个数据集不能太大，百兆左右可以接受，Gb不能接受


## 案例

```scala
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.configuration.Configuration

import scala.collection.mutable.ArrayBuffer

object FlinkBroadcast {

  /** Main program method */
  def main(args: Array[String]): Unit = {

    // get the execution environment
    val env = ExecutionEnvironment.getExecutionEnvironment
    // 类型转换
    import org.apache.flink.api.scala._
    //将准备文件数据(封装为DataSet)作为共享变量
    val broad: DataSet[String] = env.readTextFile("/opt/da/broadcast.txt")
    //在准备一个DataSet做基础数据
    val data: DataSet[String] = env.readTextFile("/opt/da/student.txt")

    //
    data.map(new RichMapFunction[String,String] {
      //这里定义了一个可变数组 接收接收到的共享变量
      private val strings = new ArrayBuffer[String]()

      override def open(parameters: Configuration): Unit = {
        super.open(parameters)
        //获取共享变量 作为一个List
        val bc: java.util.List[String] = getRuntimeContext.getBroadcastVariable[String]("BroadCast")
        val it = bc.iterator()
        while(it.hasNext){
          val next = it.next()
          strings += next
        }
      }

      //map方法中通过定义好的可变数组 来使用接收的共享变量
      override def map(in: String): String = {
        in + " 收到共享变量的信息： " +strings.mkString("{",",","}")
      }

    }).withBroadcastSet(broad,"BroadCast")
      //withBroadcastSet广播Broadcast 并在内部通过getRuntimeContext获取共享变量
      .print()
  }
//  [root@hw-bigdata1 da]# cat broadcast.txt
//  aaa
//  bbb
//  ccc
//  111
//  222
//  333
//  444
//  [root@hw-bigdata1 da]# cat student.txt
//  李云山
//  王丽丽
//  曹孟德
//  马荣臻

//  输出：
//  李云山 收到共享变量的信息： {aaa,bbb,ccc,111,222,333,444}
//  王丽丽 收到共享变量的信息： {aaa,bbb,ccc,111,222,333,444}
//  曹孟德 收到共享变量的信息： {aaa,bbb,ccc,111,222,333,444}
//  马荣臻 收到共享变量的信息： {aaa,bbb,ccc,111,222,333,444}
}
```

另一个例子：

```scala
package com.huawei.bigdata.flink.examples

import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.configuration.Configuration

import scala.collection.mutable.ListBuffer

object FlinkBroadcast2 {

  def main(args: Array[String]): Unit = {
    val env = ExecutionEnvironment.getExecutionEnvironment
    import org.apache.flink.api.scala._
    //1: 准备需要广播的数据
    val broadData = ListBuffer[Tuple2[String,String]]()
    broadData.append(("潮汐海灵","中单刺客"))
    broadData.append(("穆德凯撒","上单战士"))
    broadData.append(("流浪法师","中上法坦"))
    //1.1处理需要广播的数据
    val tupleData = env.fromCollection(broadData)
    val toBroadcastData = tupleData.map(tup=>{
      Map(tup._1->tup._2)
    })

    val text = env.fromElements("潮汐海灵","穆德凯撒","流浪法师")

    val result = text.map(new RichMapFunction[String,String] {

      var listData: java.util.List[Map[String,String]] = null
      var allMap  = Map[String,String]()

      override def open(parameters: Configuration): Unit = {
        super.open(parameters)
        this.listData = getRuntimeContext.getBroadcastVariable[Map[String,String]]("broadcastMapName")
        val it = listData.iterator()
        while (it.hasNext){
          val next = it.next()
          allMap = allMap.++(next)
        }
      }

      override def map(key: String) = {
//        val value = allMap.get(key)     // Some(中单刺客)
//        val allvalue = allMap.mkString("[", "|", "]")  //[潮汐海灵 -> 中单刺客|穆德凯撒 -> 上单战士|流浪法师 -> 中上法坦]
        val value = allMap.get(key).get   // 中单刺客
        key + "--------" + value
      }
    }).withBroadcastSet(toBroadcastData,"broadcastMapName")
      
    result.print()
  }
}

//输出：
//潮汐海灵--------中单刺客
//穆德凯撒--------上单战士
//流浪法师--------中上法坦
```

