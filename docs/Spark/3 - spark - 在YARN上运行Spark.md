# Running Spark on YARN

## Launching Spark on YARN 

确保 `HADOOP_CONF_DIR` 或者 `YARN_CONF_DIR` 指向包含 HADOOP 集群(客户端)配置文件的目录。这些配置用于写入 HDFS 并连接到`YARN ResourceManager（资源管理器）`。此目录中包含的配置将被分发到YARN集群，以便应用程序使用的所有容器使用相同的配置。如果配置引用了 Java 系统属性或者YARN没有管理的环境变量，那么它们也应该在 Spark 应用程序的配置中设置（驱动程序、执行程序和在客户机模式下运行时的 AM）。

```
YARN是一个资源管理、任务调度的框架，主要包含三大模块：ResourceManager（RM）、NodeManager（NM）、ApplicationMaster（AM）。
```



**有两种部署模式可用于在YARN上启动Spark应用程序。**

1. 在集群模式下，Spark 驱动程序**<u>运行在应用程序主进程内部</u>**，该进程**<u>由集群上的 YARN 管理</u>**，*客户机可以在启动应用程序后离开*。

   In `cluster` mode, the Spark driver runs inside an application master process which is managed by YARN on the cluster, and the client can go away after initiating the application.

2. 在客户端模式下，驱动程序在客户机进程中运行，应用程序主机仅用于从纱线请求资源。

   In `client` mode, the driver runs in the client process, and the application master is only used for requesting resources from YARN.

![集群模式下](.\spark集群模式.resource\yarn-clustar模式.bmp)

![客户端模式下](.\spark集群模式.resource\yarn-client模式.bmp)

### 两种模式的比较

|                    | Yarn Cluster                           | Yarn Client                            |
| ------------------ | -------------------------------------- | -------------------------------------- |
| Driver在哪里运行   | Application Master                     | Client                                 |
| 谁请求资源         | Application Master                     | Application Master                     |
| 谁启动executor进程 | Yarn NodeManager                       | Yarn NodeManager                       |
| 驻内存进程         | 1. Yarn ResourceManager 2. NodeManager | 1. Yarn ResourceManager 2. NodeManager |

**yarn-cluster和yarn-client模式的区别其实就是Application Master进程的区别**

- yarn-cluster模式下，driver运行在AM(Application Master)中，它负责向YARN申请资源，并监督作业的运行状况。当用户提交了作业之后，就可以关掉Client，作业会继续在YARN上运行。然而yarn-cluster模式不适合运行交互类型的作业。
- yarn-client模式下，Application Master仅仅向YARN请求executor，client会和请求的container通信来调度他们工作，也就是说Client不能离开。



与 Spark 支持的其他集群管理器不同的是，在其中主控地址是在 -- master 参数中指定的，而在 YARN 模式下，ResourceManager 的地址是从 Hadoop 配置中获取的。因此，-- master是yarn。

```shell
# 命令格式（cluster）
./bin/spark-submit --class path.to.your.Class --master yarn --deploy-mode cluster [options] <app jar> [app options]

# 例如：
./bin/spark-submit --class org.apache.spark.examples.SparkPi \
    --master yarn \
    --deploy-mode cluster \
    --driver-memory 4g \
    --executor-memory 2g \
    --executor-cores 1 \
    --queue thequeue \
    examples/jars/spark-examples*.jar \
    10
```

