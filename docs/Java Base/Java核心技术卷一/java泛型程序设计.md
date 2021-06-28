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



