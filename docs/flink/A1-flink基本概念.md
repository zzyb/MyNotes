# flink核心概念

## 简介

分布式流处理框架，能够对有界和无界的数据进行高效的处理。

流式 & 批式

## 核心架构

![](./flink基础.resource/flink-stack.png)

- **api & libraries 层**

  ​	编程API，顶级类库。

- Runtime**核心层**

  ​	核心实现层，包括作业转换，任务调度，资源分配，任务执行等。

- 物理**部署层**

  ​	不同平台(本地、集群【standalone/yarn】、云【GCE/EC2】)部署flink应用。

## flink集群架构

### 1 核心组件：

**Runtime层**

采用标准的 **Master - Slave** 结构。

![](./flink基础.resource/flink-application-submission.png)

#### Master：

- **Dispatcher**

  负责接收客户端提交的执行程序，并传递给JobManager。除此之外，还提供了WEB UI界面，用于监控作业执行情况。

- **ResourceManager**

  负责管理 slots 并协调集群资源。ResourceManager 接收来自 JobManager 的资源请求，并将存在空闲 slots 的 TaskManagers 分配给 JobManager 执行任务。Flink 基于不同的部署平台，如 YARN , Mesos，K8s 等提供了不同的资源管理器，当 TaskManagers 没有足够的 slots 来执行任务时，它会向第三方平台发起会话来请求额外的资源。

- **JobManager**

  称为masters。接收Dispatcher传递过来的<u>执行程序</u>（包含：作业图 (JobGraph)，逻辑数据流图 (logical dataflow graph) 及其所有的 classes 文件以及第三方类库 (libraries) 等等 ），紧接着 JobManagers 会将 JobGraph 转换为执行图 (ExecutionGraph)，然后向 ResourceManager 申请资源来执行该任务，一旦申请到资源，就将执行图分发给对应的 TaskManagers 。因此每个作业 (Job) 至少有一个 JobManager；高可用部署下可以有多个 JobManagers，其中一个作为 *leader*，其余的则处于 *standby* 状态。

#### Slave：

- **TaskManager**

  称为workers。TaskManagers 负责实际的子任务 (subtasks) 的执行，每个 TaskManagers 都拥有一定数量的 slots。Slot 是一组固定大小的资源的合集 (如计算能力，存储空间)。TaskManagers 启动后，会将其所拥有的 slots 注册到 ResourceManager 上，由 ResourceManager 进行统一管理。

### 2 Task & SubTask

TaskManagers 实际执行的是 SubTask，而不是 Task，这里解释一下两者的区别：

在执行分布式计算时，**Flink 将可以链接的操作 (operators) 链接到一起，这就是 Task**。之所以这样做， 是为了减少线程间切换和缓冲而导致的开销，在降低延迟的同时可以提高整体的吞吐量。 但不是所有的 operator 都可以被链接，如下 keyBy 等操作会导致网络 shuffle 和重分区，因此其就不能被链接，只能被单独作为一个 Task。 简单来说，一个 Task 就是一个可以链接的最小的操作链 (Operator Chains) 。如下图，source 和 map 算子被链接到一块，因此整个作业就只有三个 Task：

![](./flink基础.resource/flink-task-subtask.png)

**SubTask，其准确的翻译是： *A subtask is one parallel slice of a task*，即一个 Task 可以按照其并行度拆分为多个 SubTask。**如上图，source & map 具有两个并行度，KeyBy 具有两个并行度，Sink 具有一个并行度，因此整个虽然只有 3 个 Task，但是却有 5 个 SubTask。Jobmanager 负责定义和拆分这些 SubTask，并将其交给 Taskmanagers 来执行，每个 SubTask 都是一个单独的线程。

### 3 资源管理

理解了 SubTasks ，我们再来看看其与 **Slots** 的对应情况。一种可能的分配情况如下：

![](./flink基础.resource/flink-tasks-slots.png)

这时每个 SubTask 线程运行在一个独立的 <u>TaskSlot， 它们共享所属的 TaskManager 进程的TCP 连接（通过多路复用技术）和心跳信息 (heartbeat messages)，从而可以降低整体的性能开销。</u>此时看似是最好的情况，但是每个操作需要的资源都是不尽相同的，这里假设该作业 keyBy 操作所需资源的数量比 Sink 多很多 ，那么此时 Sink 所在 Slot 的资源就没有得到有效的利用。

基于这个原因，**Flink 允许多个 subtasks 共享 slots，即使它们是不同 tasks 的 subtasks，但只要它们来自同一个 Job 就可以。**

假设上面 souce & map 和 keyBy 的并行度调整为 6，而 Slot 的数量不变，此时情况如下：

![](./flink基础.resource/flink-subtask-slots 2.png)

可以看到一个 Task Slot 中运行了多个 SubTask 子任务，此时每个子任务仍然在一个独立的线程中执行，只不过共享一组 Sot 资源而已。

**那么 Flink 到底如何确定一个 Job 至少需要多少个 Slot 呢？Flink 对于这个问题的处理很简单，默认情况一个 Job 所需要的 Slot 的数量就等于其 Operation 操作的最高并行度。**如下， A，B，D 操作的并行度为 4，而 C，E 操作的并行度为 2，那么此时整个 Job 就需要至少四个 Slots 来完成。通过这个机制，Flink 就可以不必去关心一个 Job 到底会被拆分为多少个 Tasks 和 SubTasks。

![](./flink基础.resource/flink-task-parallelism.png)

### 4 组件通讯

**Flink 的所有组件都基于 Actor System 来进行通讯。**Actor system是多种角色的 actor 的容器，它提供调度，配置，日志记录等多种服务，并包含一个可以启动所有 actor 的线程池，<u>如果 actor 是本地的，则消息通过共享内存进行共享，但如果 actor 是远程的，则通过 RPC 的调用来传递消息。</u>

![](./flink基础.resource/flink-process.png)





## 注释：

Dispatcher 发报机
