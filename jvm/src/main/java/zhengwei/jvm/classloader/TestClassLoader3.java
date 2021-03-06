package zhengwei.jvm.classloader;

import com.sun.crypto.provider.AESKeyGenerator;
import org.junit.jupiter.api.Test;
import sun.misc.Launcher;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author zhengwei AKA Sherlock
 * @since 2019/5/29 10:16
 */
public class TestClassLoader3 {
    static {
        System.out.println("TestClassLoader3 init...");
    }
    public static void main(String[] args) throws ClassNotFoundException {
        /*
         * 如果String是被Bootstrap Classloader加载的，那么它的类加载器将返回null
         * 由类加载器加载类时，不是对类的主动使用，所有不会触发类的初始化过程
         */
        Class<?> clazz1=Class.forName("java.lang.String");
        System.out.println(clazz1.getClassLoader());
        /*
         * C类由AppClassLoader加载器加载
         * 由反射加载类时，是对类的主动使用，所以会触发类的初始化过程
         */
        Class<?> clazz2=Class.forName("zhengwei.jvm.classloader.C");
        System.out.println(clazz2.getClassLoader());
    }

    /**
     * JVM中类加载器的层次关系：Bootstrap ClassLoader->ExtClassLoader->AppClassLoader
     * 这三个类加载器并不是父子关系。而是包含关系，除了BootstrapClassLoader是没有父类的，其余的类加载器都只包含一个父类
     * 在有些实现中BootstrapClassLoader会用null表示
     */
    @Test
    void testParentClassLoader(){
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        /*
        因为AppClassLoader和ExtClassLoader是Launcher的内部类
        Launcher再让rt.jar中，是由Bootstrap ClassLoader加载的
        每个类都会用加载自身的类加载器去加载该类依赖的其他类(前提是依赖的类还没有被加载)
        那么Bootstrap ClassLoader也会去加载AppClassLoader和ExtClassLoader
         */
        System.out.println("AppClassLoader->"+classLoader.getClass()+"--"+classLoader.getClass().getClassLoader());//应用类加载器是由根类加载器加载的,null
        System.out.println("ExtClassLoader->"+classLoader.getParent().getClass()+"--"+classLoader.getParent().getClass().getClassLoader());//扩展类加载器由根类加载器加载，null
        System.out.println(classLoader);
        while (!Objects.isNull(classLoader)){
            classLoader=classLoader.getParent();
            System.out.println(classLoader);
        }
    }

    /**
     * ClassLoader JavaDoc
     * 数组的class对象不是由ClassLoader创建的，而是由JVM在运行期间自动创建的，
     */
    @Test
    void testArrayClassLoader(){
        String[] strings=new String[2];
        System.out.println(strings.getClass().getClassLoader());//根加载器->null
        System.out.println("--------");
        C[] cs=new C[2];
        System.out.println(cs.getClass().getClassLoader());
        System.out.println("--------");
        int[] ints=new  int[2];
        System.out.println(ints.getClass().getClassLoader());//没有classloader->null
    }

    /**
     * 根类加载器会去C:\Program Files\Java\jdk1.8.0_181\jre\classes目录下去加载类
     * 扩展类加载器会去C:\Windows\Sun\Java\lib\ext目录下去加载
     * 如果我们把类放到C:\Program Files\Java\jdk1.8.0_181\jre\classes目录中的一个，将会由根类加载器去加载，那么加载类的类加载器将会是null
     * 在HotSpot虚拟机中用null来表示根类加载器
     */
    @Test
    void testBootClassLoader() throws ClassNotFoundException {
    	/*
    	在Oracle的HotSpot中，系统属性sun.boot.class.path设置错了的话，则运行会出错，提示如下错误：
    	Error occurred during initialization of VM
		java/lang/NoClassDefFoundError: java/lang/Object
    	 */
        System.out.println(System.getProperty("sun.boot.class.path"));
        System.out.println(System.getProperty("java.ext.dirs"));
        System.out.println(System.getProperty("java.class.path"));
        /*
        特别说明：
            内建于JVM中的启动类加载器(BootstrapClassLoader)会加载java.lang.ClassLoader以及其他的Java平台类
            当JVM启动时，一块特殊的机器码会运行，它会加载扩展类加载器和应用类加载器
            这块特殊的机器码叫做启动类加载器

            启动类加载器并不是Java类，而其他加载器都是Java类
            启动类加载器是特定的平台机器码，它负责开启整个加载过程

            所有类加载器(除了启动类加载器)都被实现为Java类。不过，总归要有第一个组件来加载第一个Java类加载器，从而让整个加载过程进行下去，
            加载第一个纯Java类加载器就是启动类加载器的职责。

            启动类加载器还会负责加载供JRE正常运行的基本组件，还包括java.util和java.lang包中的类
         */
        System.out.println(ClassLoader.class.getClassLoader());//null，由启动类加载器加载
        MyClassLoader loader1=new MyClassLoader("loader1");
        System.out.println(Launcher.class.getClassLoader());//null，由启动类加载器加载
        //获取应用类加载器(重要)
        System.out.println(ClassLoader.getSystemClassLoader());
        loader1.setPath("e:/temp/");
        Class<?> clazz = loader1.loadClass("zhengwei.jvm.bytecode.TestByteCode");
        System.out.println("class : "+clazz.hashCode());
        System.out.println("class loader : "+clazz.getClassLoader());

        AESKeyGenerator aesKeyGenerator=new AESKeyGenerator();
        System.out.println(aesKeyGenerator.getClass().getClassLoader());//ExtClassLoader
        System.out.println(aesKeyGenerator.getClass().getClassLoader().getParent());//ExtClassLoader由BootstrapClassLoader加载

//        Class<?> callerClass = Reflection.getCallerClass();
    }

    /**
     * 类只会被加载一次，下次如果再去加载已加载过的类的话，那么将会直接返回之前加载好的类对象。
     * @throws Exception 异常
     */
    @Test
    void testClassLoader() throws Exception {
        MyClassLoader loader1=new MyClassLoader("loader1");
        MyClassLoader loader2=new MyClassLoader("loader2");

        Class<?> clazz1 = loader1.loadClass("zhengwei.jvm.classloader.MySample");
        Class<?> clazz2 = loader2.loadClass("zhengwei.jvm.classloader.MySample");
        //输出true，系统类加载器加载了MySample，下次加载只会返回已经加载好的类对象
        System.out.println(clazz1==clazz2);

        Object o1 = clazz1.newInstance();
        Object o2 = clazz2.newInstance();
        //方法名，方法需要传出的参数类型
        Method method = clazz1.getMethod("setMySample", Object.class);
        method.invoke(o1,o2);
    }

    /**
     * 前提：删除MySample.class文件
     * 1.每个类加载器都有自己的命名空间，命名空间由该类加载器及其所有父类加载器的类所组成
     * 2.在同一个命名空间中，不会出现类的完整名字(包括类中的包名)相同的两个类
     * 3.在不同的命名空间中，有可能出现类的完整名字相同的类
     * 4.同一个命名空间中类是相互可见的
     * 5.子类加载器的命名空间包含所有的父类加载器。因此子类加载器加载的类能够看到父类加载器加载的类，例如系统类加载器加载的类能够看到根类加载器加载的类
     * 6.父类加载器加载的类看不到子类加载器加载的类
     * 7.如果两个加载器没有直接或间接的关系，那么两个加载器各自加载的类互不可见
     * java.lang.ClassCastException: zhengwei.jvm.MySample cannot be cast to zhengwei.jvm.MySample
     * 在运行期，一个Java类是由该类的完全限定名(binary name，二进制名)和用于加载该类的定义类加载器(defining loader)所共同决定的
     * 如果同样名字(即相同的完全限定名)的类是由两个不同的类加载器加载，那么这些类就是不同的，即便.class文件时一样的，并且从相同的位置加载也是如此
     * @throws Exception 异常
     *
     * 使用双亲委托机制的好处
     *  1.可以确保Java核心类库的类型安全：所有Java应用都至少会引用java.lang.Object类，也就是说或在运行期间，java.lang.Object会被加载到JVM中，
     *    如果这个加载过程由Java应用自己的类加载器去完成，那么很有可能在JVM内存中存在多个版本的java.lang,.Object类，而且这些类是不兼容的，相互不可见的(命名空间在起作用)
     *    借助于双亲委托机制，Java核心类库中的类加载工作由启动类加载器去完成加载，从而确保Java应用所使用的都是统一版本的Java类库。
     *  2.可以确保Java核心类库不会被自定义的类所替代
     *  3.不同的类加载器可以为相同的名称(binary name)的类创建额外的命名空间，相同名称的类可以并存在JVM中，只要用不同的类加载器去加载它们即可。不同的类加载器加载器的类是不兼容的，
     *    这就相当于在JVM中创建了一个又一个相互隔离的Java类空间，这类技术在很多框架中都的得到实际应用。
     */
    @Test
    void testClassLoaderNamespace() throws Exception{
        MyClassLoader loader1=new MyClassLoader("loader1");
        MyClassLoader loader2=new MyClassLoader("loader2");
        loader1.setPath("e:/temp/");
        loader2.setPath("e:/temp/");
        Class<?> clazz1 = loader1.loadClass("zhengwei.jvm.classloader.MySample");
        Class<?> clazz2 = loader2.loadClass("zhengwei.jvm.classloader.MySample");
        //输出false，系统类加载器加载了MySample，下次加载只会返回已经加载好的类对象
        System.out.println(clazz1==clazz2);

        Object o1 = clazz1.newInstance();
        Object o2 = clazz2.newInstance();
        //方法名，方法需要传出的参数类型
        Method method = clazz1.getMethod("setMySample", Object.class);
        //第一个参数：调用对象的方法，第二个参数：方法所需参数
        method.invoke(o1,o2);
    }

    /**
     * 扩展类加载器需要把class文件打进jar包中才会去加载
     */
    @Test
    void testExtClassLoader(){
        System.out.println(TestClassLoader3.class.getClassLoader());
        System.setProperty("java.ext.paths","e:/temp");
        System.out.println(MyClassLoader.class.getClassLoader());
    }
}
class C{
    static {
        System.out.println("C static block");
    }
}