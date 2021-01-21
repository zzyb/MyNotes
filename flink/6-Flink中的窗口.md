
## 窗口

### 窗口类型
1. flink支持两种划分窗口的方式（time和count）    如果根据时间划分窗口，那么它就是一个time-window    如果根据数据划分窗口，那么它就是一个count-window

2. flink支持窗口的两个重要属性（size和interval）    

* 如果size=interval,那么就会形成tumbling-window(无重叠数据)    
* 如果size>interval,那么就会形成sliding-window(有重叠数据)    
* 如果size<interval,那么这种窗口将会丢失数据。比如每5秒钟，统计过去3秒的通过路口汽车的数据，将会漏掉2秒钟的数据。

3. 通过组合可以得出四种基本窗口：

* `time-tumbling-window` 无重叠数据的时间窗口，设置方式举例：timeWindow(Time.seconds(5)) 
  
* `time-sliding-window`  有重叠数据的时间窗口，设置方式举例：timeWindow(Time.seconds(5), Time.seconds(3)) 


* `count-tumbling-window`无重叠数据的数量窗口，设置方式举例：countWindow(5)    

* `count-sliding-window` 有重叠数据的数量窗口，设置方式举例：countWindow(5,3)

4. flink支持在stream上的通过key去区分多个窗口

      
### 窗口的实现方式 

上一张经典图：

![1ee4f402b32040ef6ed9dcc44a212078](8-Flink中的窗口.resources/CD34BCB4-E2D4-41AD-B8E7-B6EFDB0BAB82.png)


* Tumbling Time Window

假如我们需要统计每10秒中value总数，需要将用户的行为事件按每10秒进行切分，这种切分被成为翻滚时间窗口（Tumbling Time Window）。翻滚窗口能将数据流切分成不重叠的窗口，每一个事件只能属于一个窗口。

```
   	import org.apache.flink.streaming.api.windowing.time.Time
	//streaming的类型转换
	import org.apache.flink.streaming.api.scala._
   	// get the execution environment
   	
   	
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    // get input data by connecting to the socket
    val text = env.socketTextStream(hostname, port)

    // parse the data, group it, window it, and aggregate the counts
    val windowCounts = text
      .flatMap { w => w.split("\\s") }
      .filter(_.nonEmpty)
      .map { w => (w, 1) }
      //按照二元组第一项分组
      .keyBy(0)
      //每10s计算为一个窗口
      .timeWindow(Time.seconds(10))
      //按照第二项sum计算
      .sum(1)
      .print()

```

* Sliding Time Window

我们可以每5秒计算一次最近30秒用户购买的value总数。这种窗口我们称为滑动时间窗口（Sliding Time Window）。在滑窗中，一个元素可以对应多个窗口。通过使用 DataStream API，我们可以这样实现：

```
   	import org.apache.flink.streaming.api.windowing.time.Time
	//streaming的类型转换
	import org.apache.flink.streaming.api.scala._
   	// get the execution environment
   	
  	// get the execution environment
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    // get input data by connecting to the socket
    val text = env.socketTextStream(hostname, port)

    // parse the data, group it, window it, and aggregate the counts
    val windowCounts = text
      .flatMap { w => w.split("\\s") }
      .filter(_.nonEmpty)
      .map { w => (w, 1) }
      .keyBy(0)
      .timeWindow(Time.seconds(30),Time.seconds(5))
      .sum(1)
      .print()
```

* Tumbling Count Window

CountWindow ()

根据窗口中相同的key元素来触发执行，执行时只计算元素数量到达窗口大小的key的对应的结果。

​	-- window_size 参数 ： 指的是相同key的元素个数，不是输入的所有元素的总数。

例子：当某个key数目达到5的时候，计算结果。

```
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala._


	val text = env.socketTextStream(hostname, port, '\n')
	val windowCounts = text
      .flatMap { w => w.split("\\s") }
      .map { w => (w, 1) }
      //以二元组的第一位作为分组依据
      .keyBy(t => t._1)
      //当某一个key的统计数目达到5的时候，输出结果。
      .countWindow(5)
      //对 value 计数。
      .sum(1)
      .print()
输入：
abc
key
value
hello
world
abc
abc
abc
abc
输出：
(abc,5)
```

* Session Window

在这种用户交互事件流中，我们首先想到的是将事件聚合到会话窗口中（一段用户（一个key）持续活跃的周期），由非活跃的间隙分隔开。如上图所示，就是需要计算每个用户在活跃期间总共购买的商品数量，如果用户30秒没有活动则视为会话断开（假设raw data stream是单个用户的购买行为流）。Session Window 的示例代码如下：


```
   	import org.apache.flink.streaming.api.windowing.time.Time
	//streaming的类型转换
	import org.apache.flink.streaming.api.scala._
	
	
	// get the execution environment
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    // get input data by connecting to the socket
    val text = env.socketTextStream(hostname, port)

    // parse the data, group it, window it, and aggregate the counts
    val windowCounts = text
      .flatMap { w => w.split("\\s") }
      .filter(_.nonEmpty)
      .map { w => (w, 1) }
      //按照key分组（对二元组 0是key 1是value）
      .keyBy(0)
      //设置活跃周期为20s（如果某个key20秒没有新的数据，视为断开。则输出）
      .window(ProcessingTimeSessionWindows.withGap(Time.seconds(20)))
      .sum(1)
      .print()

    env.setParallelism(1)
    env.execute("SockJob-0119")
```

一般而言，window 是在无限的流上定义了一个有限的元素集合。这个集合可以是基于时间的，元素个数的，时间和个数结合的，会话间隙的，或者是自定义的。Flink 的 DataStream API 提供了简洁的算子来满足常用的窗口操作，同时提供了通用的窗口机制来允许用户自己定义窗口分配逻辑。

