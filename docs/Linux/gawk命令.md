# gawk程序

## 一、gawk初级

### gawk可以做什么？

- 定义变量保存数据。
- 使用算数和字符串操作符处理数据。
- 使用结构化编程概念（比如if-else、循环）来为数据处理增加处理逻辑。
- 通过提取数据文件中的数据元素，将其重新排列或格式化，生成格式化报告。

### gawk命令格式

```shell
gawk [options] [program] [file]
```

选项：

| 选项         | 描述                               |
| ------------ | ---------------------------------- |
| -F fs        | 指定行分割数据的分隔符             |
| -f file      | 从指定的文件中读取程序             |
| -v var=value | 定义gawk中的一个变量及其默认值     |
| -mf N        | 指定处理的数据文件中的最大字段数   |
| -mr N        | 指定处理的数据文件中的最大数据行数 |
| -w keyword   | 指定gawk兼容模式 或 警告等级       |

### 从命令行读取程序脚本

1. 脚本用花括号（{}）定义。
2. 脚本放入单引号（''）中。

```shell
gawk '{print "hello world!"}'
# 将会每一行输出一个hello world!
# 终止gawk程序，需要标明数据流已经结束了，shell通过EOF字符完成。Ctrl-D输出一个EOF符号。
```

### 使用数据字段变量

- gawk会按照`$N`的形式分配文本行中发现的数据字段。

| 符号 | 含义                 |
| ---- | -------------------- |
| $0   | 表示整行。           |
| $1   | 文本行中第一个字段。 |
| $2   | 文本行中第二个字段。 |
| $n   | 文本行中第n个字段。  |

- 每个数据字段是通过`字段分割符`划分的。默认字段分隔符是任意空白字符（例如空格或制表符）。

通过`-F`选项指定其他字段分隔符。

```shell
gawk -F= '{print $1}' properties/load.properties 
# -F=  表示指定分隔符为=号。
```

### 在程序中使用多个命令

- gawk允许将多个命令组合成一个正常程序，命令之间放置分号（;）即可。

```shell
echo "My name is lili" | gawk '{$4="TOM";print $0}'
# My name is TOM
# 此处 $4="TOM" 会给第四个字段赋值，print $0 会输出整行。
```

### 从文件中读取程序

- 可以将gawk命令存储到文件中，然后在命令行中引用。

```shell
# cat zyb.gawk 
{print $1 "'s value is " $2}

gawk -F= -f zyb.gawk load.properties 
# -f zyb.gawk 指定gawk命令文件
```

- 可以在文件中指定多条命令，<u>一条命令一行，不需要分号</u>。

```shell
# cat zyb.gawk 
{
text="'s value is : "
print $1 text $2
}
# text定义了变量，并引用
# 注意 gawk程序在引用变量的时候没有像shell一样使用美元符！
# 注意 gawk程序在引用变量的时候没有像shell一样使用美元符！
# 注意 gawk程序在引用变量的时候没有像shell一样使用美元符！


gawk -F= -f zyb.gawk load.properties 
# -f zyb.gawk 指定gawk命令文件
```

### 在处理数据前运行脚本

- BEGIN 关键字 ： 强制在读取数据前执行 BEGIN关键字后的脚本。

```shell
gawk 'BEGIN {print "##########"} {print $1 "--" $2}' load.properties 
```



### 在处理数据后运行脚本

- END关键字 ：指定一个脚本，在读完数据后执行它。

```shell
gawk 'BEGIN {print "##########"} {print $1 "--" $2} END {print "#########"}' load.properties 
```



```shell
# cat zyb.gawk 
BEGIN{
print "##########"
print "##########"
FS="="
}
{
text="'s value is : "
print $1 text $2
}
END{
print "##########"
print "##########"
}

# FS="=" 定义了FS的特殊变量，这是定义字段分隔符的另一种方法。这样就不需要在命令行中指定分隔符了！
# FS="=" 定义了FS的特殊变量，这是定义字段分隔符的另一种方法。这样就不需要在命令行中指定分隔符了！
# FS="=" 定义了FS的特殊变量，这是定义字段分隔符的另一种方法。这样就不需要在命令行中指定分隔符了！
```



## 二、gwak进阶

### 1.1使用变量

#### 内建变量

##### 字段和记录分隔符变量

| 变量        | 描述                                                         |
| ----------- | ------------------------------------------------------------ |
| FIELDWIDTHS | 【field widths】定义了每个数据字段的确切宽度（各个数字间由空格分隔） |
| FS          | 输入 字段分隔符                                              |
| RS          | 输入 记录分隔符                                              |
| OFS         | 输出 字段分隔符 （默认OFS 为 空格）【print的各个数据字段带上逗号，否则OFS设置不生效。】 |
| ORS         | 输出 记录分隔符                                              |

**FS \& OFS**

```shell
# 默认情况下，gawk将OFS设成一个空格。
# 切记print的各个数据字段带上逗号，否则OFS设置不生效。
# 切记print的各个数据字段带上逗号，否则OFS设置不生效。
# 切记print的各个数据字段带上逗号，否则OFS设置不生效。
gawk 'BEGIN {FS="," ; OFS=";"} {print $1,$2,$3}' test.txt 
```

**FIELDWIDTHS**

```shell
cat field.txt 
111.2131415
2122-232425
31323.33435

gawk 'BEGIN {FIELDWIDTHS="3 4 3"} {print $1,$2,$3}' field.txt 
111 .213 141
212 2-23 242
313 23.3 343
```

- 一旦设置了FIELDWIDTHS变量的值，就不能再改变了，这种方法不适用于边长的字段。
- 设置了FIELDWIDTHS变量，gawk就会忽略FS变量。

**RS \& ORS**

```shell
cat test2.txt 
wang xiaohua
010-88997
beijing-beijin

li si
010-67984
henan-zhengzhou

zhao xiaoxiao
010-93786
shanghai-jingan

# 这里将\n设置为字段分隔符 , 行分隔符设置为空行。
# 这里将\n设置为字段分隔符 , 行分隔符设置为空行。
gawk 'BEGIN {FS="\n" ; RS=""} {print $1,$2,$3}' test2.txt 
wang xiaohua 010-88997 beijing-beijin
li si 010-67984 henan-zhengzhou
zhao xiaoxiao 010-93786 shanghai-jingan
```

##### 数据变量

| 变量       | 描述                                                         |
| ---------- | ------------------------------------------------------------ |
| ARGC       | 当前命令行参数个数                                           |
| ARGIND     | 当前文件在ARGV中的位置                                       |
| ARGV       | 包含命令行参数的数组 （数组从下标0开始）   **ARGV[0] 应该都是gawk** |
| CONVFMT    | 数字的转换格式（详见printf语句）                             |
| ENVIRON    | 当前shell环境变量及其值组成的关联数组  **使用：ENVIRON["FLINK_HOME"]** |
| ERRNO      |                                                              |
| FILENAME   |                                                              |
| FNR        | 当前数据文件的数据行数 （**每一个文件计数，对比NR**）        |
| IGNORECASE |                                                              |
| NF         | 数据文件中的字段总数 （相当于表示**最后一个字段的位置**）    |
| NR         | 已处理的输入记录数 **（如果gawk接受两个文件，NR将会记录两个文件的总数）** |
| OFMT       |                                                              |
| RLENGTH    |                                                              |
| RSTART     |                                                              |



**ARGC  \& ARGV**

- gawk不会把<u>程序脚本</u>当做命令行的一部分。

```shell
# 'BEGIN {print ARGC,ARGV[0],ARGV[1]}' 是程序脚本，不会当做命令行的一部分。
gawk 'BEGIN {print ARGC,ARGV[0],ARGV[1]}' data1
2 gawk data1
```



**ENVIRON**

ENVIRON["VALUE"] 提供了VALUE的环境变量值。比如，ENVIRON["PATH"] 提供了PATH的环境变量值。

```shell
# ENVIRON["FLINK_HOME"] 提供了FLINK_HOME的环境变量值。
gawk 'BEGIN {print ENVIRON["FLINK_HOME"]}'
/opt/apps/flink
```

 **FNR  、NF  \& NR**

- 追踪数据字段和记录。

1. NF ： 追踪最后一个字段。

   ```shell
   cat test.txt 
   data11 data12 data13 data14 data15
   data21 data22 data23 data24 data25
   data31 data32 data33 data34 data35
   data41 data42 data43 data44 data45
   
   # 最前面加上美元号，当做字段变量（$NF）
   gawk '{print $1,$NF}' test.txt 
   data11 data15
   data21 data25
   data31 data35
   data41 data45
   ```

2. FNR  \&  NR

   ```shell
   # 注意：该命令输入了两侧test.txt文件（为了比较）
   gawk '{print $1,"FNR="FNR,"NR="NR}' test.txt test.txt 
   data11 FNR=1 NR=1
   data21 FNR=2 NR=2
   data31 FNR=3 NR=3
   data41 FNR=4 NR=4
   data11 FNR=1 NR=5
   data21 FNR=2 NR=6
   data31 FNR=3 NR=7
   data41 FNR=4 NR=8
   
   # FNR变量值在处理第二个文件的时候被重置了。
   # FNR变量值在处理第二个文件的时候被重置了。
   # FNR变量值在处理第二个文件的时候被重置了。
   ```

   

#### 自定义变量

自定义变量的变量名规则：

1. 任意数目的字母、数字、下划线。
2. 不能以数字开头。
3. gawk变量名区分大小写。

##### 在脚本中给变量赋值



##### 在命令行上给变量赋值





### 1.2处理数组



### 1.3使用模式

#### 正则表达式



#### 匹配操作符



#### 数学表达式



### 1.4结构化命令

#### if语句



#### while语句



#### do-while语句



#### for语句



### 1.5格式化打印



### 1.6内建函数

#### 数学函数



#### 字符串函数



#### 时间函数



### 1.7自定义函数

#### 定义自定义函数



#### 使用自定义函数



#### 创建函数库