# 逻辑数据模型 LDM

​	**逻辑数据模型（LDM）**可以帮助你分析信息系统的结构，独立于任何特定 的物理数据库实现。LDM 已确定实体标识符，没有概念数据模型（CDM）抽 象，但不允许你建视图模型，索引等具体的物理数据模型（PDM）元素。

- 逻辑模型是对概念数据模型的进一步细化与分解
- 既要面向业务用户，又要面向系统
- 影响数据库设计方案选择

## 一、组成元素

​	与概念数据模型（CDM）一致，只是**每个实体增加标识符**，在逻辑模型中**没有关联 （Association）及关联链接**，而概念模型中有。

## 二、创建逻辑数据模型

- 从概念数据模型转换到逻辑数据模型

  ![image-20220427095802250](D:\zyb\project\MyNotes\docs\数据仓库\PowerDesigner\PowerDesigner逻辑数据模型.resource\image-20220427095802250.png)

  ![image-20220427095903988](D:\zyb\project\MyNotes\docs\数据仓库\PowerDesigner\PowerDesigner逻辑数据模型.resource\image-20220427095903988.png)

  ![image-20220427095948794](D:\zyb\project\MyNotes\docs\数据仓库\PowerDesigner\PowerDesigner逻辑数据模型.resource\image-20220427095948794.png)

- 直接创建逻辑数据模型。

  依次创建实体、编辑属性...等。
