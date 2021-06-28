# 反射 reflective

## 反射

能够分析类能力的程序称为<u>反射</u>。

Java  运行时系统始终为所有对象维护一个<u>运行时类型标识</u>。可以使用一个特殊的Java类访问这些信息，保存这些信息的类名为**Class**。



获得Class类对象的三种方法：

- Object类的getClass方法返回一个Class类型实例。
  - Class对象会表述一个特定类的属性。
  - 常用的Class方法是：getName 。（返回类的名字）。
- 使用静态方法forName获得类名对应的Class对象。
- 如果T是任意Java类型，T.class 将代表匹配的类对象。



可以使用 == 运算符实现两个 类对象 的比较。（因为虚拟机为每个类型管理唯一一个Class对象）。

- 与instanceof不同。（子类 instanceof 父类 会成功， == 比较 Class会失败。）。



如果有一个Class类型的对象，可以用它构造类的实例。

```java
Class cl = Class.forName("java.util.Random");
Object obj = cl.getConstructor().newInstance();
```

1. getConstructor方法得到Constructor类型对象
2. 然后使用newInstance()方法构造一个实例。

- 如果该类没有无参构造方法，getConstructor会抛出异常。



## 资源

与 **类** 关联的数据文件，称为<u>资源</u>。



Class类提供了一个很有用的服务可以`查找资源文件`。

1. 获得拥有资源类的对象。例如T.class。
2. 有些方法，接受描述资源位置的URL，需要调用URL url = cl.getResource("a.gif");
3. 否则，使用getResourceAsStream方法，得到一个输入流来读取文件中的数据。

```java
//资源加载 5-13
```



## 利用反射分析类

java.lang.reflect包中有三个类Field、Method和Constructor分别用来描述类的字段、方法和构造器。

- 都有一个getName方法，返回字段、方法或构造器的名称。
- 都有一个getModifiers的方法，返回一个整数，用不同的0/1位描述所使用的修饰符，例如public 和 static。
  - 使用Modifier类的静态方法 分析getModifiers返回的整数。例如，isPublic、isPrivate或isFinal等。
  - 使用Modifier.toString方法将修饰符打印出来。

### Field

- getType方法，返回描述字段类型的一个对象，对象类型同样是Class。

### Method

- 有一个报告返回类型的方法。
- 有报告参数类型的方法。

### Constructor

- 有报告参数类型的方法。



Class类的<u>getFields、getMethods和getConstructors方法</u>，分别返回这个类支持的**公共**字段、方法、构造器的数组，包括超类的公共成员。



Class类的<u>getDeclareFields、getDeclareMethods和getDeclareConstructors方法</u>，分别返回类中声明的**全部**字段、方法、构造器的数组，包括私有成员、包成员、受保护成员，<u>但不包括超类成员</u>。



## 使用反射在运行时分析对象

利用反射可以<u>查看在编译时还不知道的对象字段</u>。

- 关键方法Field类中的get方法。

  ```java
  // obj 是一个包含f字段的 类的对象 。
  // f   是一个Field类型的对象。
  // f.get(obj) 将返回一个对象，其值为obj的当前字段值。
  var tom = new Employee("Tom",50000,10,1,1999);
  Class cl = tom.getClass();
  Field f = cl.getDeclareField("name");
  Object v = f.get(tom);
  ```

- 也可以设置值，调用f.set(obj,value)。

- 只能对可以访问的字段调用get和set方法。（如果name是一个私有字段，get、set方法会抛出IllegalAccessException异常。）
  - Java安全机制允许**查看**一个对象有哪些字段，但是除非有访问权限，否则不允许**读取**这些字段的值。



<u>反射的默认行为收到Java的访问控制</u>。**不过**，可以调用Field、method 或 Constructor对象的<u>`setAccessible`方法（AccessibleObject类的一个方法）覆盖Java的访问控制</u>。



`AccessibleObject 类` 是Field、Method 和 Constructor类的公共超类。

- 为了调试、持久存储和类似机制提供的。



## 使用反射编写泛型数组代码

可以把一个 Employee[] 数组 临时转化为 Object[] 数组，然后在转换回来。**但是**，<u>一开始就是Object[]数组却永远不能转换成Employee[]数组</u>。



java.lang.reflect包中的Array类：

- Array类的newInstance，可以构造一个新数组，提供两个参数，一个是数组元素类型、一个是数组长度。

  ```java
  Object newArray = Array.newInstance(componentType, newLength);
  ```

- 获得数组长度，Array类的getLength方法会返回一个数组长度。

- 获得数组的元素类型：

  - 首先获得a数组的类型对象。
  - 确认它确实是一个数组。
  - 使用Class类的`getComponentType方法`(<u>只针对表示数组的类定义了这个方法</u>)确定数组的正确类型。

```java
//这里参数声明为Object，而不是Object[]。因为 int[] 可以转化为 Object，但是不能转化为对象数组！！！
public static Object goodCopyOf(Object a, int newLength){ 
  Class cl = a.getClass();
	if(!cl.isArray()) 
  	return null
	int length = Array.getLength(a);
	Class componentType = cl.getComponentType();
	Object newArray = Array.newInstance(componentType,length);
  System.arraycopy(a, 0, newArray, 0, Math.min(length,newLength));
  return newArray;
}
```



### 调用任意方法和构造器

- 使用Field类的get方法查看一个对象的字段。

- Method类有一个invoke方法，允许你调用包装在当前Method对象中的方法。

  - `Object invoke(Object obj,Object... args)`第一个参数是隐式参数，其余的对象提供了显示参数。
  - 对于静态方法，第一个参数可以忽略/设置为null。
  - 如果返回的是基本类型，invoke会返回其类型的包装器类型。

- 如何获得Method对象？

  1. 可以调用getDeclareMethods方法，然后搜索返回的method对象数组，找到想要的。
  2. 调用Class类的getMethod方法，返回想要的方法。

  ```java
  Method getMethod(String name, Class... parameterTypes)
    //示例1 获取Employee的getName方法
    Method m1 = Employee.class.getMethod("getName");
    //示例2 获取Employee的raiseSalary方法方法
    Method m2 = Employee.class.getMethod("raiseSalary",double.class);//第一个参数，是方法名；后面的参数，参数类型。
  ```

- 可以使用类似的方法调用任意的构造器。

  - 参数类型提供给Class.getConstructor方法。
  - 数值提供给Constructor.newInstance方法。

  ```java
  Class cl = Random.class;
  Constructor cons = cl.getConstructor(long.class);
  Object obj = cons.newInstance(42L);
  ```

- 如果在调用方法时，参数提供错误invoke会抛出一个异常。

- invoke的参数和返回值类型必须是Object类型。

  - 这意味着需要多次调用强制类型转换。比直接调用代码慢得多。