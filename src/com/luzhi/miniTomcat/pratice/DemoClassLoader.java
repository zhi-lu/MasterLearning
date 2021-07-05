package com.luzhi.miniTomcat.pratice;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import org.jetbrains.annotations.Contract;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/6/2
 * 该Demo用作类的加载器和反射学习,时间不多了.
 * 上午时间重要的.Java的主要三种类的加载器:
 * </br>
 * 《one》
 * Java中第一个类加载器:BootStrapLoader(启动类加载器)由JVM提供.并由C++语言实现.实现rt.jar,tool.jar等java核心类包.主要路径为:
 * {@code System.getProperty("java.home"); 通过系统类获取相关的地址属性.}主要是路径下的包.
 * 《two》
 * Java中第二个类加载器:ExtensionLoader(扩展类启动器)有Java语言实现.它的父加载器是BootStrapLoader(启动类加载器)
 * 具体实现类为Java中的:{@link  sun.misc.Launcher}$ExtClassLoader.{@code System.getProperty("java.home")}/ext下的类.
 * 《three》
 * Java中第三个类加载器:AppClassLoader(应用类加载器).自然而然,由Java语言实现.它的父类加载器为ExtensionLoader.
 * 具体实现类为Java中的:{@link sun.misc.Launcher}$AppClassLoader.主要加载的是classpath下的类或者jar.是java默认的类加载器.
 */
public class DemoClassLoader {

    private int sum = 0;
    private static final int NUM = 2;

    private synchronized void add() {
        sum += 1;
    }

    public static void main(String[] args) throws Exception {
        run();
        System.out.println("<=====================================>");
        runInvoke();
        System.out.println("<=====================================>");
        LoaderTest.getLoaderTest().run();
        System.out.println("<=====================================>");
        runTestReflect();
        System.out.println("<=====================================>");
        runTestUrlReflect();

    }

    private static void run() throws InstantiationException, IllegalAccessException {
        DemoClassLoader demoClassLoader = new DemoClassLoader();
        DemoClassLoader demoClassLoader1;
        System.out.println("输出地址" + demoClassLoader);
        Class<?> clazz = demoClassLoader.getClass();
        demoClassLoader1 = (DemoClassLoader) clazz.newInstance();
        System.out.println(demoClassLoader);
        System.out.println(demoClassLoader1);
        demoClassLoader.testClassPrint();
        demoClassLoader.testClassPrint();
        demoClassLoader1.testClassPrint();
    }

    private static void runInvoke() throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Class<?> clazz = DemoClassLoader.class;
        Object object = clazz.newInstance();
        Method method = clazz.getDeclaredMethod("runReflect");
        method.invoke(object);
    }

    private void testClassPrint() {
        System.out.println("Hello,ClassReflect");
        add();
        if (sum >= NUM) {
            System.out.println("Hello,执行了两次");
        }
    }

    private void runReflect() {
        System.out.println("成功启动该方法啦~~~~");
    }

    private static void runTestReflect() throws Exception {
        TestClassLoader testClassLoader = new TestClassLoader();
        testClassLoader.run();
    }

    private static void runTestUrlReflect() throws Exception {
        TestUrlClassLoader.run();
    }
}

/**
 * @author apple
 * @version jdk1.8
 * // 该类探究Java初始的三种类的加载器.
 */
class LoaderTest {

    private static final LoaderTest LOADER_TEST = new LoaderTest();

    @Contract(pure = true)
    public static synchronized LoaderTest getLoaderTest() {
        return LOADER_TEST;
    }

    /**
     * 该类禁止任何人进行初始化.
     */
    private LoaderTest() {
    }

    public void run() {
        ClassLoader classLoader = LoaderTest.class.getClassLoader();
        System.out.println("输出LoaderTest类的加载器:" + classLoader);
        classLoader = classLoader.getParent();
        System.out.println("输出LoaderTest父类的加载器:" + classLoader);
        classLoader = classLoader.getParent();
        if (classLoader == null) {
            System.out.println("ExtensionLoader的父类从Java的逻辑观点说不存在,因为他是由C++进行实现.");
        }
    }
}

/**
 * @author apple
 * @version jdk1.8
 * // 该类用于探究自定义类的加载器此类继承{@link ClassLoader}.实现相关的加载.
 */
class TestClassLoader extends ClassLoader {
    private final File classedCatalog = new File(System.getProperty("user.dir"), "class_path");

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes = loadClassDate(name);
        return defineClass(name, bytes, 0, bytes.length);
    }

    private byte[] loadClassDate(String fileQualifiedName) throws ClassNotFoundException {
        // 将路径中的"."替换成"/"用于路径访问。
        String fileName = StrUtil.replace(fileQualifiedName, ".", "/") + ".class";
        File file = new File(classedCatalog, fileName);
        // 如果指定路径文件不存在.抛出错误 classNotFoundException.
        if (!file.exists()) {
            throw new ClassNotFoundException("Sorry, It didn't found. Please check work-path or file is exists. Thank you!");
        }
        return FileUtil.readBytes(file);
    }

    public void run() throws Exception {
        TestClassLoader testClassLoader = new TestClassLoader();
        Class<?> clazz = testClassLoader.loadClass("cn.how2j.diytomcat.test.HOW2J");
        Object object = clazz.newInstance();
        Method method = clazz.getDeclaredMethod("hello");
        // 通过反射调用类中的方法.
        method.invoke(object);
        System.out.println("规定指向的类对象加载器为:" + clazz.getClassLoader());
    }
}

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/6/1
 * 使用URL来获取相关的类对象---{@link URLClassLoader}--为多个类进行加载.
 */
class TestUrlClassLoader extends URLClassLoader {

    public TestUrlClassLoader(URL[] urls) {
        super(urls);
    }
    /**
     * 该静态方法使用{@link URL}初始时,要指定相关路径的协议作为前缀(一定要指定协议.).例如 "file:path"
     *
     * @throws Exception 抛出错误为Exception.可能是{@link java.net.MalformedURLException} 畸形URL错误.加载等异常.
     * @see #run()
     */
    public static void run() throws Exception {
        URL url = new URL("file:/Users/apple/IdeaProjects/MasterLearning/jar_4_test/test.jar");
        URL[] urls = new URL[]{url};
        TestUrlClassLoader testUrlClassLoader = new TestUrlClassLoader(urls);
        TestUrlClassLoader testUrlClassLoaderTwo = new TestUrlClassLoader(urls);
        Class<?> clazz = testUrlClassLoader.loadClass("cn.how2j.diytomcat.test.HOW2J");
        Object object = clazz.newInstance();
        Method method = clazz.getDeclaredMethod("hello");
        method.invoke(object);
        System.out.println("打印类的加载器为:" + clazz.getClassLoader());
        if (!testUrlClassLoaderTwo.equals(testUrlClassLoader)){
            System.out.println("对象不同哦～～～～.");
        }
    }
}
