package com.luzhi.miniTomcat.pratice;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.log.LogFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/5/21
 * 对Atomic 原子性进行探索(包括相关的锁).
 * 在Java中,原子性操作为CAS(compare and swap)预期值和当前变量进行(compare),如果相等则进行替换(swap).
 * </br>
 * 《1》现代CPU都支持CAS,如果不支持则JVM使用自旋锁和互斥锁.(待会讨论)
 * 《1.1》自旋锁(spinlock)是一种非阻塞锁.例如:假如有A和B两个线程,A线程获取该锁时,但B锁已经对该锁进行占有.那么A线程不会被挂起(非阻塞状态),而是等待消耗CPU的资源时间,不断试图获取锁.
 * 《1.2》互斥锁(exclusionLock)是一种阻塞锁.例如:假如还是A和B两个线程,A线程获取获取该锁时,B又在A前面对锁进行占有.那么A线程会被挂起(阻塞状态).直到B线程释放该锁,唤醒当前阻塞线程A.去获取锁.
 * </br>
 * 《2》使用CAS也无法保证是否出现ABA情况,即即使预期值和变量值相等有可能出现 A->B->A,所以设置一个版本号,每次修改变量即版本号自增.对版本号进行比较.变量是否修改.
 * 《Range》Atomic的原子更新范围有四种:
 * 《2.1》原子方式更新基本类型.
 * 《2.2》原子方式更新数组.
 * 《2.3》原子方式更新引用.
 * 《2.4》原子方式更新字段.
 */
public class AtomicDemo {

    public static void main(String[] args) throws InterruptedException {

        runAtomicInt();
        System.out.println("<==============================================>");
        runAtomicIntArray();
        System.out.println("<==============================================>");
        runAtomicWife();
        System.out.println("<==============================================>");
        runAtomicParent();
        System.out.println("<==============================================>");
        runTest();
        System.out.println("<==============================================>");
        runBase();
    }

    private static void runAtomicInt() {

        AtomicInt atomicInt = new AtomicInt();
        atomicInt.run();
    }

    private static void runAtomicIntArray() {
        AtomicIntArray atomicIntArray = new AtomicIntArray();
        atomicIntArray.run();
    }

    private static void runAtomicWife() {
        AtomicWife atomicWife = new AtomicWife("白上吹雪", 16);
        AtomicReference<AtomicWife> atomicWifeAtomicReference = new AtomicReference<>();
        atomicWifeAtomicReference.set(atomicWife);
        System.out.println("打印更新前,老婆名:" + atomicWifeAtomicReference.get().getName());
        System.out.println("打印更新前,老婆年龄:" + atomicWifeAtomicReference.get().getYear());
        AtomicWife atomicWifeUpdate = new AtomicWife("刻晴", 16);
        atomicWifeAtomicReference.compareAndSet(atomicWife, atomicWifeUpdate);
        System.out.println("打印更新后,老婆名:" + atomicWifeAtomicReference.get().getName());
        System.out.println("打印更新后,老婆年龄:" + atomicWifeAtomicReference.get().getYear());
    }

    /**
     * @see #runAtomicParent()
     * 对于{@link AtomicIntegerFieldUpdater#newUpdater(Class, String)}
     * 接受一个类对象,来判断反射类型和泛型类型是否匹配.要更新的字段的名称.
     */
    private static void runAtomicParent() throws RuntimeException {

        AtomicIntegerFieldUpdater<AtomicParent> atomicParentAtomicIntegerFieldUpdater = AtomicIntegerFieldUpdater.newUpdater(AtomicParent.class, "year");
        AtomicParent atomicParent = new AtomicParent("鲁滍", 19);
        System.out.println("打印增长后的年龄:" + atomicParentAtomicIntegerFieldUpdater.incrementAndGet(atomicParent));
        System.out.println("获取十年后的我年龄:" + atomicParentAtomicIntegerFieldUpdater.addAndGet(atomicParent, 10));
        System.out.println("打印现在对象的年龄:" + atomicParent.getYear());
    }

    private static void runTest() throws InterruptedException {
        RunTest runTest = new RunTest();
        runTest.runTest();
    }

    private static void runBase() throws InterruptedException{
        BaseTest.testTwo();
        System.out.println("<=====================================>");
        Thread.sleep(10000);
        BaseTest.testOne();
    }
}

/**
 * @author apple
 * @version jdk1.8
 * @since 1.0
 * // 用作探究AtomicInteger的使用(基本类型).下面皆为实例:
 * 具体方法参考,请借鉴 {@link AtomicInteger} 中方法和注释.
 */
class AtomicInt {

    static AtomicInteger atomicInteger;

    public AtomicInt() {
        atomicInteger = new AtomicInteger(1);
    }

    public void run() {
        // 相当于i++
        System.out.println("先获取再自增:" + atomicInteger.getAndIncrement());
        System.out.println("获取:" + atomicInteger.get());

        // 先获取,再增加.
        System.out.println("先获取再定义增加:" + atomicInteger.getAndAdd(5));
        System.out.println("获取:" + atomicInteger.get());

        // 先自增再获取
        System.out.println("先自增再获取" + atomicInteger.incrementAndGet());
        System.out.println("获取:" + atomicInteger.get());

        // 先自减再获取
        System.out.println("先自减再获取:" + atomicInteger.decrementAndGet());
        System.out.println("获取:" + atomicInteger.get());
    }
}

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/5/21
 * 对原子性类型数组进行操作.
 */
class AtomicIntArray {

    /**
     * @see #ints 原来的数组.
     * @see #atomicIntegerArray 对原数组进行copy.
     */
    static volatile int[] ints;
    AtomicIntegerArray atomicIntegerArray;

    public AtomicIntArray() {
        synchronized (AtomicIntArray.class) {
            ints = new int[]{1, 2, 3};
            atomicIntegerArray = new AtomicIntegerArray(ints);
        }
    }

    public void run() {
        atomicIntegerArray.set(0, 3);
        System.out.println("打印原子性数据<0>号元素:" + atomicIntegerArray.get(0));
        // 不会改变原来的数据元素.
        System.out.println("打印原来的数组<0>号元素:" + ints[0]);
    }
}

/**
 * @author apple
 * @version jdk1.8
 * 对于原子性类型引用操作.
 */
class AtomicWife {
    private final String name;
    private final int year;

    public AtomicWife(String name, int year) {
        this.name = name;
        this.year = year;
    }

    public int getYear() {
        return year;
    }

    public String getName() {
        return name;
    }
}

/**
 * @author apple
 * @version jdk1.8
 * 对原子性字段进行更新.
 */
class AtomicParent {
    /**
     * @see #year 权限设置为public, 给{@link AtomicIntegerFieldUpdater<AtomicParent>} 进行调用.
     * 不然会出现原子场更新器问题.
     */
    private final String name;
    public volatile int year;

    public AtomicParent(String name, int year) {
        this.name = name;
        this.year = year;
    }

    public String getName() {
        return name;
    }

    public int getYear() {
        return year;
    }
}

/**
 * @author apple
 * @version jdk1.8
 * // 对自旋锁的探究,非阻塞状态.
 */
class SpinLock {
    AtomicReference<Thread> atomicReference = new AtomicReference<>();

    public void lock() {
        Thread thread = Thread.currentThread();
        // 设置当前为非阻塞状态,现在线程Thread不为null,进行循环(更新值和初始值不匹配).判断等价于!false=true.进行循环.(自旋锁进入等待状态)
        //noinspection StatementWithEmptyBody
        while (!atomicReference.compareAndSet(null, thread)) {

        }
    }

    public void unlock() {
        Thread thread = Thread.currentThread();
        // 将当前线程设置为null,使lock方法跳出循环.
        atomicReference.compareAndSet(thread, null);
    }
}

/**
 * @author apple
 * @version jdk1.8
 * // 设置统计值(num)实现对锁和开锁的统计.实现{@link Runnable}接口.
 */
class Test implements Runnable {
    static int num = 0;
    private final SpinLock spinLock;

    public Test(SpinLock spinLock) {
        this.spinLock = spinLock;
    }

    @Override
    public void run() {
        this.spinLock.lock();
        num++;
        this.spinLock.unlock();
    }
}

/**
 * @author apple
 * @version jdk1.8
 * // 进行具体的测试和调用{@link Test}.
 */
@SuppressWarnings("AlibabaAvoidManuallyCreateThread")
class RunTest {

    private static final int NUMBER = 50;

    public void runTest() throws InterruptedException {
        synchronized (Test.class) {
            SpinLock spinLock = new SpinLock();
            for (int i = 1; i <= NUMBER; i++) {
                Test test = new Test(spinLock);
                Thread thread = new Thread(test, "线程Test--" + i);
                thread.start();
                Thread.sleep(100);
                System.out.println("操作线程为:" + thread.getName() + ",数字为:" + Test.num);
            }
        }
    }
}

@SuppressWarnings({"ManualMinMaxCalculation", "ConstantConditions"})
class BaseTest {

    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final int FREQUENCY = 1000000;

    /**
     * @see #testOne()
     * 三目运行符测试耗时.(请求100次获取最小值的耗时时间)
     */
    public static void testOne() {
        TimeInterval timeInterval = DateUtil.timer();
        for (int i = 0; i < FREQUENCY; i++) {
            int result = ONE < TWO ? ONE : TWO;
            LogFactory.get().info("结果为:" + result);
        }
        System.out.println("输出耗时为:" + timeInterval.intervalMs());
    }

    /**
     * @see #testTwo()
     * 使用函数进行测试.
     */
    public static void testTwo() {
        TimeInterval timeInterval = DateUtil.timer();
        for (int i = 0; i < FREQUENCY; i++) {
            int result = Math.min(ONE, TWO);
            LogFactory.get().info("结果为:" + result);
        }
        System.out.println("输出耗时为:" + timeInterval.intervalMs());
    }
}