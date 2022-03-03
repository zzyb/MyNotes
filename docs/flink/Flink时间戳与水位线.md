# Flink时间戳与水位线

## 什么是水位线

    水位线告诉系统事件时间的进度——基于时间的算子会使用这个时间来触发计算并推动进度前进。

    水位线有两个基本属性：

- 必须单调递增。

- 和记录时间戳存在联系。

    水位线存在的意义：允许应用控制结果的完整性和延迟。

## 水位线在Flink API的应用

    Flink API 期望 `WatermarkStrategy`同时包含 a `TimestampAssigner`和`WatermarkGenerator`.`WatermarkStrategy`有许多常用策略作为静态方法开箱即用，除此之外用户也可以在需要时构建自己的策略。

**静态方法**：

- `WatermarkStrategy<T> forBoundedOutOfOrderness(Duration maxOutOfOrderness)`

  周期性的。这种水印策略引入的延迟是周期性间隔长度，加上绑定的界限。

  ```java
  //
  WatermarkStrategy
       // 水印,这里设定的是任意新到元素和已到时间戳最大元素之间的时间差上限。
       // Tuple4 数据示例：(北京,购买,2022-03-03 11:15:20.373,1)
      .<Tuple4<String, String, String, Integer>>forBoundedOutOfOrderness(Duration.ofSeconds(10))
      //lambda表达式获取数据时间戳
      .withTimestampAssigner((event, timestamp) -> {
          try {
              return format.parse(event.f2).getTime();
          } catch (ParseException e) {
              return 0L;
          }
      })    // 时间戳分配器 -- lambda获取时间戳;(可选的)
  ```

- `WatermarkStrategy<T> forMonotonousTimestamps()`

  周期性的，并紧紧地遵循数据中的最新时间戳。通过该策略引入的延迟主要是生成水印的周期性间隔。

  ```java
  WatermarkStrategy
       // 水印,这里设定的是时间元素时间戳。
       // Tuple4 数据示例：(北京,购买,2022-03-03 11:15:20.373,1)
      .<Tuple4<String, String, String, Integer>>forMonotonousTimestamps()
      .withTimestampAssigner(
      (event, timestamp) -> {
          try {
              return format.parse(event.f2).getTime();
          } catch (ParseException e) {
              return 0L;
          }
      }
  )
  ```

- `WatermarkStrategy<T> noWatermarks()`

  没有生产水印。这在做基于纯处理时间的流处理的场景中可能是有用的。

  ```java
  SingleOutputStreamOperator singleOutputStreamOperator = dataSource.assignTimestampsAndWatermarks(
      WatermarkStrategy
      .<Tuple4<String, String, String, Integer>>noWatermarks()//水印追随时间戳。
  );
  
  singleOutputStreamOperator.keyBy(new KeySelector<Tuple4<String,String,String,Integer>,String>() {
      @Override
      public String getKey(Tuple4<String, String, String, Integer> value) throws Exception {
          return value.f1;
      }
  })
      // 采用处理时间，不需要水印推进
      .windowAll(TumblingProcessingTimeWindows.of(Time.milliseconds(5000)))
      .sum(3).print();
  ```

  

**WatermarkStrategy接口代码**：

TimestampAssigner接口：用于从已读入流式应用的元素中提取时间戳。（时间戳分配器）

WatermarkGenerator接口：用于定制水位线。（水位线分配器）

```java
public interface WatermarkStrategy<T> 
    extends TimestampAssignerSupplier<T>,
            WatermarkGeneratorSupplier<T>{

    /**
     * Instantiates a {@link TimestampAssigner} for assigning timestamps according to this
     * strategy.
     */
    @Override
    TimestampAssigner<T> createTimestampAssigner(TimestampAssignerSupplier.Context context);

    /**
     * Instantiates a WatermarkGenerator that generates watermarks according to this strategy.
     */
    @Override
    WatermarkGenerator<T> createWatermarkGenerator(WatermarkGeneratorSupplier.Context context);
}
```

`WatermarkStrategy`可以在Flink应用程序的两个地方使用：

1. 直接在源上（Source上）

2. 在非源操作之后（Source之后）

    直接在源上更可取，因为它允许Source利用有关水位线逻辑中的分片/分区/拆分的知识。然后，来源通常可以更精细地跟踪水位线，并且来源产生的整体水位线将更加准确。

    仅当无法直接在Source上设置策略时，才应用第二个选项。

```textile
The first option is preferable, because it allows sources to exploit knowledge about shards/partitions/splits in the watermarking logic. Sources can usually then track watermarks at a finer level and the overall watermark produced by a source will be more accurate. Specifying a WatermarkStrategy directly on the source usually means you have to use a source specific interface/ Refer to Watermark Strategies and the Kafka Connector for how this works on a Kafka Connector and for more details about how per-partition watermarking works there.
The second option (setting a WatermarkStrategy after arbitrary operations) should only be used if you cannot set a strategy directly on the source
```

## 编写 WatermarkGenerators（水位线分配器）

```java
/**
 * The {@code WatermarkGenerator} generates watermarks either based on events or
 * periodically (in a fixed interval).
 *
 * <p><b>Note:</b> This WatermarkGenerator subsumes the previous distinction between the
 * {@code AssignerWithPunctuatedWatermarks} and the {@code AssignerWithPeriodicWatermarks}.
 */
@Public
public interface WatermarkGenerator<T> {

    /**
     * Called for every event, allows the watermark generator to examine 
     * and remember the event timestamps, or to emit a watermark based on
     * the event itself.
     */
    void onEvent(T event, long eventTimestamp, WatermarkOutput output);

    /**
     * Called periodically, and might emit a new watermark, or not.
     *
     * <p>The interval in which this method is called and Watermarks 
     * are generated depends on {@link ExecutionConfig#getAutoWatermarkInterval()}.
     */
    void onPeriodicEmit(WatermarkOutput output);
}
```

**周期性生成器**通常通过观察传入事件`onEvent()` ，然后在框架调用`onPeriodicEmit()`时发出水印。

**定点生成器**将查看事件`onEvent()`并等待在流中携带水位线信息的<u>特殊标记事件</u>或<u>标点符号</u>。当它看到其中一个事件时，它会立即发出一个水印。通常，定点生成器不会从`onPeriodicEmit()`发送水位线.

**生成水位线的时间间隔**（每*n*毫秒）通过`ExecutionConfig.setAutoWatermarkInterval(...)`方法. 每次都会调用生成器的`onPeriodicEmit()`方法，如果返回的水印为非空且大于前一个水印，则会发出新的水印。

### 周期性水位线分配器 (*periodic*)

周期性生成器**观察流事件并周期性地生成水印**（可能取决于流元素，或纯粹基于处理时间）。

```java
/**
 * This generator generates watermarks assuming that elements arrive out of order,
 * but only to a certain degree. The latest elements for a certain timestamp t will arrive
 * at most n milliseconds after the earliest elements for timestamp t.
 */
public class BoundedOutOfOrdernessGenerator implements WatermarkGenerator<MyEvent> {

    private final long maxOutOfOrderness = 3500; // 3.5 seconds

    private long currentMaxTimestamp;

    @Override 
    // 获取数据流遇到的最大事件时间的时间戳。
    public void onEvent(MyEvent event, long eventTimestamp, WatermarkOutput output) {
        currentMaxTimestamp = Math.max(currentMaxTimestamp, eventTimestamp);
    }

    @Override
    //当周期性的调用此方法时，发出最大遇到的最大时间戳-3.5s 作为水位线。
    public void onPeriodicEmit(WatermarkOutput output) {
        // emit the watermark as current highest timestamp minus the out-of-orderness bound
        output.emitWatermark(new Watermark(currentMaxTimestamp - maxOutOfOrderness - 1));
    }

}

/**
 * This generator generates watermarks that are lagging behind processing time 
 * by a fixed amount. It assumes that elements arrive in Flink after a bounded delay.
 */
public class TimeLagWatermarkGenerator implements WatermarkGenerator<MyEvent> {

    private final long maxTimeLag = 5000; // 5 seconds

    @Override
    // 什么都不做
    public void onEvent(MyEvent event, long eventTimestamp, WatermarkOutput output) {
        // don't need to do anything because we work on processing time
    }

    @Override
    // 周期性的调用此方法时，发出当前机器处理时间-5s 作为水位线。
    public void onPeriodicEmit(WatermarkOutput output) {
        output.emitWatermark(new Watermark(System.currentTimeMillis() - maxTimeLag));
    }
}
```

### 定点水位线分配器 (*punctuated*)

定点水位线生成器将观察事件流，并在看到带有水印信息的特殊元素时发出水印。

```java
public class PunctuatedAssigner implements WatermarkGenerator<MyEvent> {

    @Override
    public void onEvent(MyEvent event, long eventTimestamp, WatermarkOutput output) {
        // 判断每个事件流，检测到包含特殊元素时，发送水位线。
        if (event.hasWatermarkMarker()) {
            output.emitWatermark(new Watermark(event.getWatermarkTimestamp()));
        }
    }

    @Override
    // 定点水位线，该方法什么都不做。
    public void onPeriodicEmit(WatermarkOutput output) {
        // don't need to do anything because we emit in reaction to events above
    }
}
```

**注意**：可以在每个事件上生成水印。但是，由于每个水印都会导致一些下游计算，因此过多的水印会降低性能。

## 水位线与分区

Flink 会将<u>数据流划分为不同的分区</u>，并将它们交由不同的算子任务来<u>并行执行</u>。**每个分区**作为一个数据流，**都会包含带有时间戳的记录以及水位线**。

根据任务连接情况，其任务可能需要同时接受多个输入分区的记录和水位线，也可能需要将他们发送到多个输出分区。

于是有两个问题需要我们探究：

1. 如何将水位线发送到多个输出任务？
2. 从多个输出任务获取水位线后如何推动事件时间时钟前进？

回答分割线：

​	一个任务会为它的每个输入分区都维护一个分区水位线。（partition watermark）。

①.当收到某个分区传来的水位线后，任务会以接收值和当前值中较大的那个去更新对应分区的水位线的值。

②.随后，任务会把事件时间时钟调整为所有分区水位线中最小的那个值。（如果事件时间时钟向前推进，任务会首先处理因此而触发的所有计时器，之后才会把对应的水位线发往所有相连的输出分区，从而实现事件时间到全部下游的广播。）

![水位线与分区](.\Flink时间戳与水位线.resource\水位线与分区.bmp)

## 水位线的取舍

    水位线可以平衡延迟和结果的完整性。他们控制着在执行某些计算前（例如完成窗口计算并发出结果）需要等待数据到达的时间。基于事件时间的算子使用水位线来判断输入记录的完整度以及自身的操作进度。根据接收的水位线，算子会计算一个所有相关输入记录都已经接收完毕的预期时间点。

    但是现实中没有完美的水位线，你需要尽可能的了解数据源、网络以及分区等一系列信息，以此来估计进度和输入记录的延迟上限。

    **宽松的水位线**，将导致产生结果的延迟增大、状态大小也会增加，但是更能够保证相关数据收集的完整。

    **紧迫的水位线**，将导致结果不完整或不准确，但是可以做到较低的延迟和及时生成结果。