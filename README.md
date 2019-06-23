# playground
## **目的在于平时的学习而写的代码**
### 1.common包
* HttpAPIClient-http请求工具包
* IpUtil-IP格式的互转和IP地址的一些判断
* SysUtils-判断一个对象是否为空
### 2.jvm目录
* TestGC-->学习JVM的时候测试GC的一些代码
* zhengwei.jvm.classloader-->学习JVM中类加载过程时测试时写的实例代码，有代码的帮助，理解起来会更加的透彻
    > 类的加载过程是:加载->链接->初始化->使用->卸载<br/>
    > 其中链接分为:验证->准备->解析<br/>
    __需要特别注意的是准备和初始化是两个过程__<br/>
    1. 加载：加载是把.class文件加载进JVM之中
        1. 加载类的方式有从本地直接加载；通过网络下载.class文件；从jar中加载；从专有数据库中加载和将Java动态编译成.class文件
        2. 类加载器
            1. 类加载器是用来把类加载进JVM中的，从JDK1.2开始，类加载采用**双亲委托机制**，这种机制保证了JVM平台的安全性。在此委托机制中，除了JVM自带的根类加载器外，其余的类加载器都有且只有一个父类加载器。
               Java中所有的核心类库都将会由JVM自带的Bootstrap ClassLoader、ExtClassLoader和AppClassLoader进行加载，用户自定义的类加载器是没有机会去加载的，防止包含恶意核心类乎代码被加载。
            2. JVM自带的类加载器
                1. 根类加载器(**Bootstrap ClassLoader**)，无父类，最顶级的类，会去系统属性 `sun.boot.class.path` 所指定的路径下加载类库，由C++实现，不是ClassLoader的子类
                2. 扩展类加载器(**ExtClassLoader**),父加载器是**根类加载器**，负责加载Java平台中扩展功能的一些jar包，它从系统属性 `java.ext.dirs` 所指定的目录下加载类库，它是ClassLoader的子类
                3. 系统类加载器(**AppClassLoader**)，父加载器是**扩展类加载器**，负责加载classpath中所指定的jar包，从系统属性 `java.class.path` 所指定的目录下加载类，它是ClassLoader的子类
            3. 自定义类加载器
                1. 需要继承 `java.lang.ClassLoader` , `java.lang.ClassLoader` 是一个抽象类，但是没有抽象方法，不能够直接实例化，需要继承它，然后实例化，需要重写findClass方法。
                2. 用户自定义的类加载器的父类加载器是应用类加载器AppClassLoader
                3. 还有一种特殊的类加载器，它的存在就是为了打破双亲委托机制的局限性，为了使用SPI机制而存在的，那就是线程上下文类加载器 `Thread.currentThread().getContextClassLoader()`
            4. 类加载器并不会等到某个类被**首次主动使用**的时候再去加载它。JVM规范允许加载器在预料到某个类要被使用的时候就预先加载它，
               如果在预先加载过程中遇到了.class文件缺失或存在错误，类加载器必须在**程序首次主动**使用该类时才报告错误(Linkage Error)，
               如果这个类一直没有被**主动使用**，那么类加载器将不会报告此错误。
            5. 获取ClassLoader的几种方式
                1. 获取当前类加载器: `clazz.getClassLoader();`
                2. 获取当前线程的上下文类加载器: `Thread.currentThread().getContextClassLoader();`
                3. 获取系统的类加载器: `ClassLoader.getSystemClassLoader();`
                4. 获得调用者的类加载器: `DirverManager.getCallerClassLoader();`
            6. 值得注意的是：各个类加载器之间的关系并**不是继承关系**，而是**包含关系**，形成一种**树形结构**。除了根类加载器，其余的类加载器都有且只有一个父类加载器。
            7. 类加载器的**命名空间**：
                1. 每个类加载器都有自己的命名空间，**命名空间由该类加载器及其所有父类加载器加载的类组成**。
                2. 在同一个命名空间中，不会出现类的全限定名相同的两个类。
                3. 在不同的命名空间中，可能出现类的全限定名相同的两个类。[zhengwei.jvm.classloader.TestClassLoader3.testClassLoaderNamespace]
                4. **子类加载器的命名空间包含所有父类加载器的命名空间**。因此由子类加载器加载的类能够访问到父类加载器加载的类，但是父类加载器是访问不到子类加载器加载的类的，例如扩展类加载器能够访问到根类加载器加载的类。
                5. 如果两个加载器之间没有直接或间接的关系，那么它们各自加载的类将互不可见。[zhengwei.jvm.classloader.TestClassLoader3.testClassLoaderNamespace]
            8. 创建自定义类加载器，只需要继承 `java.lang.ClassLoader` 类，然后重写 `findClass(String name)` 方法即可，该方法根据指定的类的二进制名字，返回对应的Class对象的引用。
    2. 链接：将类与类之间的关系处理好
        1. 验证：校验.class文件的正确性；语义检查；字节码验证和二进制兼容性验证，把加载的类的二进制文件合并到JVM中去。
        2. 准备：为类的**静态变量**分配内存空间，并将其**赋初始值**，在到达初始化之前，类的静态变量知识只是jvm赋予的默认值，而不是真正的用户指定的值
        3. 解析：将类中常量池中寻找类、接口、字段和方法的符号引用替换成直接引用的过程
    3. 初始化：为类的静态变量赋予正确的默认值，就是把链接阶段中的准备阶段的类的静态变量的默认值赋予用户指定的初始值
        1. 类的初始化时机
            1. 创建的类的实例
            2. 访问某个类或接口的静态变量(字节码中使用`getstatic`标记)，或者对静态变量进行赋值(字节码中使用`putstatic`标记)，或者调用类的静态方法(字节码中使用`invokestatic`)
            3. 反射Class.forName("zhengwei.jvm.Test");调用一个参数的Class.forName("xxxxx");是会默认初始化该类的，源码中是有体现的。
                ```java
                public static Class<?> forName(String className) throws ClassNotFoundException {
                         Class<?> caller = Reflection.getCallerClass();
                         return forName0(className, true, ClassLoader.getClassLoader(caller), caller);
                }
                ```
                ```java
                private static native Class<?> forName0(String name, boolean initialize,
                                                             ClassLoader loader,
                                                             Class<?> caller)
                throws ClassNotFoundException;
                ```
            4. 初始化一个类的子类，同时也会初始化这个类的父类，如果父类还有父类，那么会继续初始化父类的父类直到最顶级的父类。这条规则不适用于接口。
            5. JVM启动时被表明启动类的类，包含main方法的类。
            6. JDK1.7支持动态语言调用。
            7. 除了上述的几种调用方式，**其余的调用都是被动调用**，都不会导致类的初始化。
        2. 在初始化阶段，JVM会执行类的初始化语句，为类的静态变量赋予初始值(即**程序员自己指定的值**)，在程序中，静态变量的初始化方法有两种：
            1. 在静态变量处声明初始值： `public static int a = 1;`
            2. 在静态代码块进行初始化： `public static int a ; static { a = 1; }`
        3. 静态变量的声明语句，以及静态代码块都被看作类的初始化语句，JVM会严格按照初始化语句在类文件的**既定顺序**去执行它们。
        4. 类的初始化步骤
            1. 加入这个类没有被加载和连接，那就先进行加载和连接。
            2. 假如类存在直接父类，并且这个类还没有被初始化，那就初始化父类。
            3. 假如类存在初始化语句，那就依次执行类的初始化语句。
        5. **接口的初始化和类的初始化是有一些区别的**。
            1. 在初始化一个接口的时候并不会初始化它的父接口。
            2. 因此，一个父接口并不会因为它的父接口或者实现类被初始化而被初始化，只有当程序首次使用了该接口的特定接口的静态变量时才会导致该接口的初始化。
        6. **调用ClassLoader的loadClass方法加载一个类时，并不是对类的主动使用，不会导致类的初始化。**
    4. 使用：
        1. 实例化对象：
            * 为类的新实例分配内存空间，通在堆上分配内存空间
            * 为实例赋予默认值
            * 为实例赋予指定的默认值
                **注意：Java编译器为它编译的每个类都至少生成一个初始化方法。在Java的.class文件中这个实例化方法被称为<init>，针对源代码中的一个构造方法，Java编译器都会产生一个<init>方法**
    5. 卸载：把类的相关信息从内存中剔除
        1. 当一个类被加载、链接和初始化之后，它的生命周期就开始了。只有当该类不再被引用时，即不可触及时，class对象就结束了它的生命周期，该类的信息将会在方法区卸载，从而结束生命周期。
        2. 一个类何时结束生命周期取决于代表它的class对象何时结束生命周期。
        3. 由Java虚拟机自带的类加载器加载的类，在虚拟机周期中始终不会被卸载。Java自带的虚拟机：Bootstrap ClassLoader,ExtClassLoader和AppClassLoader。JVM会始终保持对这些类加载器的引用，而这些类加载器也会保持它们所加载类的class对象的引用，因此这些class对象始终是可触及的。
        4. 由用户自定义的类加载器加载的类是可以被卸载的。
* zhengwei.jvm.bytecode->学习Java字节码时敲的一些实例代码
    1. 可以使用 `javap -verbose -p` 命令来分析一个class文件，将会分析文件中的魔数、版本号、常量池、类信息、类构造方法和成员变量。
    2. 魔数：所有的.class文件前四个字节为魔数，魔数的固定值是 `0xCAFEBABE`
    3. 魔数之后的四个字节是版本信息，前两个字节是minor version，后两个字节是major version，可以使用 `java -version` 来验证这一点。
    4. 常量池(constant pool)：紧接着版本号之后的就是常量池入口，一个Java类中定义的很多信息都由常量池来维护和描述，可以将常量池看作是class文件的资源仓库，比如Java中的定义的方法和变量信息都存储在常量池中。<br/>
       常量池中主要存储两类常量：字面量和符号引用。**字面量就是文本字符串，Java中被申明成final的常量值；而符号引用是如类和接口的全限定名，字段的名称和描述符，方法的名称和描述符**。
    5. 常量池的总体结构：Java类所对应的常量池主要由常量池数量和常量池表组成。常量池的数量紧跟在版本号之后，占据两个字节；常量池表紧跟在常量池数量之后，常量数组表与一般的数组不同，<br/>
       常量数组中都是不同的元素类型、结构不同的，长度自然也会不同，但是每一种元素的第一个数据都是u1类型的，该字节是个标志位，占据一个字节。JVM会根据这个标志位来获取元素的具体元素<br/>
       值得注意的是：**常量池中元素的个数=常量池数量-1(其中0暂时不使用)**，目的是满足某些常量池索引值的数据在特定情况下需要表达“不引用任何一个常量池”的含义；根本原因在于，索引0也是一个常量(保留常量)，只不过它不位于常量池中，这个常量-> l就对应null值，**常量池的索引从1而非0开始**。
    6. 在JVM规范中，每个变量/字段都有描述信息，描述信息主要是描述字段的数据类型、方法的参数列表(包括数量、类型与顺序)与返回值。根据描述规则，基本数据类型和代表无返回值的void类型都用一个大写字母表示，对象类型使用大写的L加上对象的全限定名表示。<br/>
       >`B -> byte`<br/>
       `C -> char`<br/>
       `F -> float`<br/>
       `I -> int`<br/>
       `J -> long`<br/>
       `S -> short`<br/>
       `Z -> boolean`<br/>
       `V -> void`<br/>
       `L -> 对象，例如: Ljava/lang/Object;`
    7. 对于数组类型来说，每一个维度都使用一个前置的 `[` 来表示，如 `int[]` 表示成 `[I;`， `String[][]` 表示成 `[[java/lang/String;`
    8. 描述方法时，按照先参数列表后返回值类型的顺序来描述，参数列表被严格定义在 `()` 中，如方法 `String getRealNameByIdAndNickName(int id, String nickNamw)` 表示成 `(I, Ljava/lang/String;)Ljava/lang/String;`
**值得注意的是：类在准备和初始化阶段中，在执行为静态变量赋值遵循从上到下的顺序执行具体实例参见[zhengwei.jvm.classloader.TestClassLoader2]**
* 类和接口在加载的时候有一些不同，JVM在初始化一个类时，要求它的全部父类全部初始化完毕，但是这条规则不适用于接口
    1. 初始化一个类时，并不会初始化它所有实现的接口
    2. 在初始化一个接口时，并不会先去初始化它的父接口<br/>
    因此，一个父接口并不会因为它的子接口或实现类初始化而初始化，只有当程序首次使用了特定接口的静态变量时，才会去初始化该接口。
### 3.hdfs包
* HDFSClient-hdfs的客户端
* HDFSIO-hdfs的IO操作，包含往云平台上传文件和下载文件等操作
### 4.LeetCode包
* 是一些平时的LeetCode的刷题代码
* Daily-LeetCode01TwoSum-[两数之和](https://leetcode-cn.com/problems/two-sum/)
* Daily-LeetCode02AddTwoNumbers-[两数相加](https://leetcode-cn.com/problems/add-two-numbers/)
### 5.spark包
* 学习和实验spark一些功能的时候写的一些代码
### 6.unsafe包
* 闲暇时看一些Java关于unsafe的文章时写的一些代码
### 7.algorithm包-算法
#### 自己的一些白话理解
* 1.选择排序，时间复杂度是O(n^2),空间复杂度是O(1),不稳定的一种排序算法，
主要思想是：遍历整个要排序的数组，要是前一个元素大于后一个元素，则调换元素位置，知道数组有序位置
* 2.冒泡排序，时间复杂度是O(n^2),空间复杂度是O(1)，稳定的一种排序，
主要思想：有两层循环，外循环控制着要排序的数组的长度，每完成一次比较，下次要比较的数组长度就会减小一；
内循环控制着数组中元素的比较，如果后一个数大于前一个数就交换位置，大的数就像泡泡一样慢慢的往数组的后面跑去，随着要比较的数组长度减少，数组也逐渐有序。
* 3.插入排序，时间复杂度O(n^2),空间复杂度O(1)，稳定的一种排序算法，
主要思想：插入排序就像玩扑克时往手上接牌一样，刚开始牌比较少，随着牌的增多，我们把牌插入到大小合适的地方。
插入排序同样是有两层循环，第一层循环控制着要排序数组的个数，每循环一次要比较的数组长度加一，每次加的一个元素就是要就进行比较的元素，每次新进的元素要和之前的元素进行比较，直到新进的元素大于前一个元素为止。
插入排序适合样本较小和基本有序的元素排序，理论上效率要比选择排序和冒泡排序要高。
### 8.设计模式
#### 8.1 单例模式
* 这里写了4钟单例模式的写法。
* 1.饿汉式，线程安全，也是生产环境中比较常用的一种写法，比较简单的一种方式。这种方式主要是利用JVM只会加载一个类一次的特性来保证线程安全，当类被加载时，静态变量也会被加载；
有一个缺点：就是不论我们是否用到了这个类的对象，JVM都会去加载这个类的实例。
* 2.懒加载，线程不安全和线程不安全的版本都有，主要时利用synchronized关键字去加锁代码块以达到同步效果。但是效率比较低。
* 3.静态内部类的方式，线程安全，这既达到了懒加载也时线程安全的，主要还是利用JVM只加载一次类的这个特性来保证线程安全。
* 4.枚举的方式，真正意义上的单例模式，线程安全，不会被反序列化，这种方法还要继续深入了解下...
#### 8.2 责任链模式
* 可以参阅JavaEE中的过滤器

#### 8.3 观察者模式
* 观察者模式与责任链模式有些相像，都是利用面向对象的多态的这个特性；都是把每个观察者串联起来执行，但是责任链模式中可以中断链的继续执行，而观察者模式一般不中断链的执行。
* 观察者模式有三个主要角色，1.被观察者，2.观察者，3.事件类，还有一些观察者中对于各种事件的处理逻辑的方法。
### 9.thread
* chapter01
    1. Java应用程序的main函数就是一个线程，是被JVM启动时调用的，线程名就叫main
    2. 创建一个线程必须要创建Thread，重写 `run()` 方法，并调用 `start()` 方法。
    3. 在JVM启动时，实际上会有很多的线程，但至少有一个非守护线程。
    4. 当你调用一个线程的 `start()` 方法时，此时至少有两个线程，一个是调用你线程的那个线程，还有一个是被执行 `start()` 方法的线程。
    5. 线程分为new,runnable,running,block,terminate，需要注意的是：block是不能直接到running状态的，block需要先转到runnable状态，等分配到cpu资源时才会切换到running状态。
**平时学习和工作的一些总结**
---
**zhengwei AKA Sherlock**
---
**闲暇之余会更新哦**
