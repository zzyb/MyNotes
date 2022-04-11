# 集群模式

<img src=".\spark集群模式.resource\spark集群模式1.bmp"  />

在集群中运行：

1. Spark Context可以连接到几种`Cluster Manager`（集群管理器），集群管理器在应用程序中分配资源。
2. 一旦连接，Spark就会在集群中获取对应节点上的`Executor`（执行器）。（这是运行计算和存储应用程序的数据的进程）。
3. Spark Context将应用程序的代码发送到执行器。之后，将`Tasks`发送给Executors。
   - Driver Program 将计算程序划分为不同执行阶段和多个Task，将Task发送给Executor。
   - Executor负责执行Task，并将执行状态汇报给Driver；同时，也会将节点资源使用情况汇报给资源管理器。



| Term                           | Meaning                                                      |
| :----------------------------- | :----------------------------------------------------------- |
| Application 【应用】           | User program built on Spark. Consists of a *driver program* and *executors* on the cluster. 【在spark上构建的应用程序。由集群中的驱动程序和执行者组成。】 |
| Application jar 【应用 Jar】   | A jar containing the user's Spark application. In some cases users will want to create an "uber jar" containing their application along with its dependencies. The user's jar should never include Hadoop or Spark libraries, however, these will be added at runtime. 【包含用户Spark应用程序的Jar。在某些情况下，用户希望创建一个“超级 Jar”以及其依赖项。用户的Jar永远不应该包括Hadoop或Spark库，因为，这些将在运行时添加。】 |
| Driver program 【驱动程序】    | The process running the main() function of the application and creating the SparkContext 【运行应用程序的main()函数并创建SparkContext的进程】 |
| Cluster manager 【集群管理器】 | An external service for acquiring resources on the cluster (e.g. standalone manager, Mesos, YARN, Kubernetes) 【获取集群资源的外部服务（例如独立管理器，Mesos，Yarn，Kubernetes）】 |
| Deploy mode 【部署模式】       | Distinguishes where the driver process runs. In "cluster" mode, the framework launches the driver inside of the cluster. In "client" mode, the submitter launches the driver outside of the cluster. 【区分执行程序运行的位置。在“群集”模式下，该框架在群集中启动驱动程序。在“客户端”模式下，提交者在群集之外启动驱动程序。】 |
| Worker node 【工作节点】       | Any node that can run application code in the cluster 【可以在群集中运行应用程序代码的任何节点】 |
| Executor 【执行器】            | A process launched for an application on a worker node, that runs tasks and keeps data in memory or disk storage across them. Each application has its own executors. 【在工作节点上为应用程序启动的进程，运行任务并将数据保存在内存或磁盘存储中。每个应用程序都有自己的执行者。】 |
| Task 【任务】                  | A unit of work that will be sent to one executor 【将被发送到一个执行者的工作单元】 |
| Job 【作业】                   | A parallel computation consisting of multiple tasks that gets spawned in response to a Spark action (e.g. `save`, `collect`); you'll see this term used in the driver's logs. 【由响应于 Spark行动 而产生的多个任务组成的并行计算（例如，保存，收集）;您将在驱动日志看到此术语。】 |
| Stage 【阶段】                 | Each job gets divided into smaller sets of tasks called *stages* that depend on each other (similar to the map and reduce stages in MapReduce); you'll see this term used in the driver's logs. 【每个作业都分为较小的任务组，称为阶段，依赖于彼此（类似于MapReduce中的map和reduce阶段）;您将在驱动日志中看到此术语。】 |

