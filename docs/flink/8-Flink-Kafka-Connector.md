
## 简介

Flink-kafka-connector用来做什么？

Kafka中的partition机制和Flink的并行度机制结合，实现数据恢复。
Kafka可以作为Flink的source和sink。
任务失败，通过设置kafka的offset来恢复应用。

### kafka简单介绍

**1.生产者（Producer）**
    顾名思义，生产者就是生产消息的组件，它的主要工作就是源源不断地生产出消息，然后发送给消息队列。生产者可以向消息队列发送各种类型的消息，如狭义的字符串消息，也可以发送二进制消息。生产者是消息队列的数据源，只有通过生产者持续不断地向消息队列发送消息，消息队列才能不断处理消息。
**2.消费者（Consumer）**
    所谓消费者，指的是不断消费（获取）消息的组件，它获取消息的来源就是消息队列（即Kafka本身）。换句话说，生产者不断向消息队列发送消息，而消费者则不断从消息队列中获取消息。
**3.主题（Topic）**
    主题是Kafka中一个极为重要的概念。首先，主题是一个逻辑上的概念，它用于从逻辑上来归类与存储消息本身。多个生产者可以向一个Topic发送消息，同时也可以有多个消费者消费一个Topic中的消息。Topic还有分区和副本的概念。Topic与消息这两个概念之间密切相关，Kafka中的每一条消息都归属于某一个Topic，而一个Topic下面可以有任意数量的消息。


## Flink消费Kafka注意事项

* setStartFromGroupOffsets()【默认消费策略】

    默认读取上次保存的offset信息
    如果是应用第一次启动，读取不到上次的offset信息，则会根据这个参数auto.offset.reset的值来进行消费数据


* setStartFromEarliest()
从最早的数据开始进行消费，忽略存储的offset信息


* setStartFromLatest()
从最新的数据进行消费，忽略存储的offset信息


* setStartFromSpecificOffsets(Map<KafkaTopicPartition, Long>) 
从指定位置进行消费

**以上几个设置，设置在  FlinkKafkaConsumer010 之后。**

```scala
val streamkafka = env.addSource(
    new FlinkKafkaConsumer010[String](
      "flink001", new SimpleStringSchema(), properties
    ) //offset的消费策略在这里set
)
```


* 当checkpoint机制开启的时候，KafkaConsumer会定期把kafka的offset信息还有其他operator的状态信息一块保存起来。当job失败重启的时候，Flink会从最近一次的checkpoint中进行恢复数据，重新消费kafka中的数据。


* 为了能够使用支持容错的kafka Consumer，需要开启checkpoint
env.enableCheckpointing(5000); // 每5s checkpoint一次

### Kafka作为Flink Sink

向kafka写入数据：

```scala
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010
import org.apache.flink.api.common.serialization.SimpleStringSchema

import java.util.Properties

object WriteIntoKafkaProducer {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.setParallelism(1)

    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "3.7.191.3:21007,3.7.191.2:21007,3.7.191.4:21007");
    //以下配置是因为开启了 加密传输
    properties.setProperty("security.protocol", "SASL_PLAINTEXT");
    properties.setProperty("sasl.kerberos.service.name", "kafka");

    //添加自定义的数据源（该数据源每3秒生成一条模拟数据）
    val message = env.addSource(new SimpleStringGenerator)

    message.addSink(new FlinkKafkaProducer010[String](
      "flink001",new SimpleStringSchema,properties
    ))

    env.execute()
  }
}
class SimpleStringGenerator extends SourceFunction[String]{
  var running = true
  var i = 0

  override def run(sourceContext: SourceFunction.SourceContext[String]): Unit = {
    while(true){
      sourceContext.collect("add element : " + i)
      i += 1
      Thread.sleep(3000)
    }
  }

  override def cancel(): Unit = {
    running = false
  }
}

```
大家这里特别注意，我们实现了一个SimpleStringGenerator来生产数据，代码如下：

```scala
class SimpleStringGenerator extends SourceFunction[String]{
  var running = true
  var i = 0
    /**
     * 主要的方法
     * 启动一个source
     * 大部分情况下，都需要在这个run方法中实现一个循环，这样就可以循环产生数据了
     */
  override def run(sourceContext: SourceFunction.SourceContext[String]): Unit = {
    while(true){
      sourceContext.collect("add element : " + i)
      i += 1
      Thread.sleep(3000)
    }
  }
  //取消一个cancel的时候会调用的方法
  override def cancel(): Unit = {
    running = false
  }
}
```


### Kafka作为Flink Source

```scala
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010
import org.apache.flink.streaming.api.scala._
import java.util.Properties

object ReadFromKafka {
  def main(args: Array[String]): Unit = {
    // 创建 kafka 配置文件对象
    val properties = new Properties()
    // 具体配置可以根据自己的集群或需要来更改
    properties.setProperty("group.id", "test")
    properties.setProperty("auto.offset.reset", "earliest");
    properties.setProperty("bootstrap.servers", "3.7.191.3:21007,3.7.191.2:21007,3.7.191.4:21007");
    //以下配置是因为开启了 加密传输 
    properties.setProperty("security.protocol", "SASL_PLAINTEXT");
    properties.setProperty("sasl.kerberos.service.name", "kafka");
    //
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //
    env.setParallelism(1)
    
    // 添加 kafka 为 flink 数据源 （参数1：topic 参数2： 消息序列化方式 参数3： 配置文件对象）
    val streamkafka = env.addSource(new FlinkKafkaConsumer010[String](
      "flink001", new SimpleStringSchema(), properties
    ))
    streamkafka.print()
   
    env.execute("kafka - test001")
  }
}
```
