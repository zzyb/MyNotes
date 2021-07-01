# 泛型程序设计

**随着泛型的引入**，Java有了一个表述能力很强的类型系统，<u>允许设计者详细的描述变量和方法的类型要如何变化</u>。



**为什么要使用泛型程序设计**？

编写代码可以对多种不同类型的对象重用。



##  类型参数 type parameter

```java
//ArrayList类有一个类型参数用来指示元素类型。
var files = new ArrayList<String>();
```

- 让代码具有更好的可读性。
- 编译器可以充分利用类型信息。
  - 在调用get的时候，不需要进行强制类型转换。
- 编译器还知道`ArrayList<String>`的add方法有一个String类型的参数，这比有一个Object类型的参数要安全得多。

**类型参数的好处：让程序更加可读，也更安全。**



## 泛型类 generic class

`泛型类`就是有一个或多个类型变量的类。

```java
public class Pair<T> {}
```

- 类型变量T，用尖括号<>括起来，放在类名后面。
- 泛型类 可以有多个类型变量。例如，`public class Pair<T, U> { }`

- 使用<u>具体的类型</u>替换<u>类型变量</u>来**实例化**泛型类型。例如，`Pair<String>`

**泛型类相当于普通类的工厂。**



## 泛型方法

```java
class A {
  //顺序：修饰符 - 尖括号 <T> - 返回类型 T - 方法名与参数
  //这里修饰符是 public static
  public static <T> T getMiddle(T... a){
    return a[a.length/2]
  }
}
```

- 类型变量放在修饰符后面 ， 并在返回类型前面。

- 可以在普通类中定义，也可以在泛型类中定义。

- 调用泛型方法时，具体的类型包围在尖括号中，放在方法名前。

  ```java
  String middle = A.<String>getMiddle("A","B","C");
  ```



## 类型变量的限定 bound

**什么是类型变量的限定？**

有时，类或者方法需要对类型变量加以约束。



- 对类型变量T 设置一个限定（bound）, 限制 T只能是实现了XXX接口的类。

  ```java
  public static <T extends XXX> T min(T[] a)
  ```

- \<T extends  BoundingType\> 表示T只能是限定类型的子类型。

- 类型变量T和限定类型可以是类，也可以是接口。

- 一个<u>类型变量或通配符</u>（T）可以有多个限定。限定类型用“&”分隔，类型变量用“,”分隔。

  ```java
  T extends XXX & YYY
  ```

- **最多有一个限定可以是类**；<u>如果有一个类作为限定，它必须是限定列表的第一个限定</u>。



# 泛型代码和虚拟机

虚拟机没有泛型类型对象，**所有对象都属于普通类**。

## 类型擦除

- 定义一个泛型类型，都会自动提供一个相应的原始类型（raw type）。

  - 原始类型的名字就是去掉类型参数后的泛型类型名。

    ```java
    T extends XXX
    //原始类型名字：XXX
    ```

- 类型变量 T 会被擦除，并替换为其限定类型。
  - 无限定类型的变量替换为Object。

- 原始类型用第一个限定来替换类型变量，或者，如果没有给定限定，就替换为Object。

  ```java
  public class Interval<T extends Comparable & Serializable> implements Serializable {
    private T lower;
    private T upper;
    ...
    public Interval(T first,T second){
      ...
    }
  }
  //类型擦除后的原始类型为：(注意 T 的变化)
  public class Interval implements Serializable {
    private Comparable lower;
    private Comparable upper;
    ...
    public Interval(Comparable first,Comparable second){
      ...
    }
  }
  ```

- 为了提高效率，应该将`标签（tagging）接口`放在限定列表的末尾。
  - 标签接口是<u>没有方法的接口</u>。

## 转换泛型表达式

- 泛型方法调用时，如果擦除了返回类型，编译器会插入强制类型转换。

  ```java
  Pair<Employee> buddies = ...;
  Employee buddy = buddies.getFirst();
  //getFirst 擦除类型后，返回类型是Object。编译器会自动插入 转换到Employee 的强制类型转换。
  //即，两个步骤：
  //1:对原始方法Pair.getFirst的调用。
  //2:将返回的Object类型强制转换为Employee。
  ```

- 当访问一个泛型字段时，也要插入强制类型转换。

  ```java
  Employee buddy = buddies.first;
  //也会在结果字节码中插入强制类型转换。
  ```

  

## 转换泛型方法

`泛型方法`的类型擦除：

```java
//一个泛型方法： （通常认为这是一组方法）
public static <T extends Comparable> T min(T[] a)
//经过类型擦除的泛型方法： （擦除后，剩下一个方法）
public static Comparable min(Comparable[] a)
```

- 类型擦除带来两个复杂问题！

```java
//注意：Pair<T> 中也有 setSecond(T)方法。
class DateInterval extends Pair<LocalData>{
  //这里覆盖了Pair中的setSecond(LocalDate)方法
  public void setSecond(LocalData second){
    if(second.compareTo(getFirst())){
      super.setSecond(second);
    }
  }
}
```

经过类型擦除后：

```java
//注意：Pair<T> 中也有 setSecond(T)方法。
class DateInterval extends Pair{
  //因为类型擦除，导致Pair中的方法setSecond(T) 变成了 setSecond(Object)
  public void setSecond(LocalData second){
		...
  }
  //因此这里还有另一个从Pair中继承的方法
  public void setSecond(Object) {}
}
```



- 一个例子：

```java
var interval = new DateInterval(...);
Pair<LocalDate> pair = interval; // 可行，分配给超类。
pair.setSecond(aDate);
```

我们<u>希望setSecond调用具有**多态性**，由于pair引用了一个DateInterval对象，所以应该调用DateInterval.second</u>。

**问题**：类型擦除与多态发生了冲突！

**解决**：编译器在DateInterval类中生成了一个`桥方法（bridge method）`

```java
public void setSecond(Object second) {
  setSecond((LocalDate) second)
}
```

于是：

```java
var interval = new DateInterval(...);
Pair<LocalDate> pair = interval; // 可行，分配给超类。
pair.setSecond(aDate);	
// pair （经过类型擦除）只有一个 setSecond方法：setSecond(Object)
// 虚拟机在pair的引用对象（interval，即DateInterval）上调用这个方法。
// DateInterval.setSecond(Object)，这个桥方法会去调用 DateInterval.setSecond(LocalDate)
```

- 桥方法看起来会很奇怪

如果DateInterval类也覆盖了getSecond方法，就会出现：

```java
LocalDate getSecond()	// 定义在DateInterval
Object getSecond()		// 覆盖方法，定义在Pair中
//不能如此写代码！！！（两个方法有相同的参数类型【这里是都没有参数】）
//但是：
//在虚拟机中，会由参数类型和返回类型共同指定一个方法！！！！！！！！
//在虚拟机中，会由参数类型和返回类型共同指定一个方法！！！！！！！！
//在虚拟机中，会由参数类型和返回类型共同指定一个方法！！！！！！！！

```

- 桥方法不仅用于泛型类型

见361p - 注释

- 调用遗留代码

见337p





# 限制与局限性

