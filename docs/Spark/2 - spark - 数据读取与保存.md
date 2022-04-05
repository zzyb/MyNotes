# 数据读取与保存

## 一、动机

<u>有时候，数据量可能大到无法放在一台机器上。</u>



**Spark支持很多种输入输出源**。（以下是常见的三种）

- <u>文件格式与文件系统</u>。
  - 文件系统：
    - 本地文件系统
    - 分布式文件系统
  - 文件格式：
    - 文本文件
    - JSON
    - SequenceFile等
- <u>SparkSQL中的结构化数据源</u>。
  - 针对JSON和Hive中的结构化数据。

- <u>数据库与键值存储</u>。
  - Spark自带库和第三方库。
    - HBase
    - ElasticSearch
    - JDBC
    - Cassandra

