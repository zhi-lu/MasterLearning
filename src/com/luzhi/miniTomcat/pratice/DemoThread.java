package com.luzhi.miniTomcat.pratice;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/5/21
 * 对多线程的探索,关于synchronized的四种修饰方式
 * 在Java中 synchronized:
 * 对于接口方法不能使用synchronized进行修饰,构造方法不可以使用synchronized进行修饰,但它的synchronized代码块可以进行同步.
 * 《1》对于synchronized(this) 一个线程访问一个对象的同步代码块时,会使其他访问该对象的代码块线程出现阻塞状态。
 * 《2》对方法进行修饰,一个线程访问一个对象被synchronized修饰的方法时,会使其他访问该对象synchronized的方法线程进行堵塞.
 */
@SuppressWarnings("AlibabaAvoidManuallyCreateThread")
public class DemoThread {

    public static void main(String[] args) throws InterruptedException {

        runSyncThread();
        // 睡上2秒.便于查看.
        Thread.sleep(2000);
        runSynThread();
        Thread.sleep(2000);
        Child child = new Child();
        child.methods();
        Thread.sleep(2000);
        runSynchroThread();
        Thread.sleep(2000);
        runSynchronizedThread();
    }

    private static void runSyncThread() {
        SyncThread syncThread = new SyncThread();
        // 创建 A 线程.
        Thread thread = new Thread(syncThread, "SynchronizeOne");
        // 创建 B 线程.
        Thread thread1 = new Thread(syncThread, "SynchronizedTwo");
        thread.start();
        thread1.start();
    }

    private static void runSynThread() {

        SynThread synThread = new SynThread();
        // 创建 A 线程.
        Thread thread = new Thread(synThread, "SynThread-One");
        // 创建 B 线程.
        Thread thread1 = new Thread(synThread, "SynThread-Two");
        thread.start();
        thread1.start();
    }

    private static void runSynchroThread() {
        SynchroThread synchroThread = new SynchroThread();
        // 创建 A 线程.
        Thread thread = new Thread(synchroThread, "SynchroThread-One");
        // 创建 B 线程.
        Thread thread1 = new Thread(synchroThread, "SynchroThread-Two");
        thread.start();
        thread1.start();
    }

    private static void runSynchronizedThread() {
        SynchronizedThread synchronizedThread = new SynchronizedThread();
        // 创建 A 线程.
        Thread thread = new Thread(synchronizedThread, "synchronizedThread-One");
        // 创建 B 线程.
        Thread thread1 = new Thread(synchronizedThread, "synchronizedThread-Two");
        thread.start();
        thread1.start();
    }
}

@SuppressWarnings("unused")
class SyncThread implements Runnable {

    private static volatile int count;
    private static final int NUMBER = 5;

    public SyncThread() {
        count = 0;
    }

    /**
     * @see #run()
     * 对run进行重新.使用synchronized(this)
     */
    @Override
    public void run() {
        synchronized (this) {
            for (int i = 0; i < NUMBER; i++) {
                try {
                    System.out.println("打印当前线程名:" + Thread.currentThread().getName() + ":" + (count++));
                    Thread.sleep(100);
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }

    public static int getCount() {
        return count;
    }
}

@SuppressWarnings("unused")
class SynThread implements Runnable {

    private static volatile int count;
    private static final int NUMBER = 5;

    public SynThread() {
        count = 0;
    }

    @Override
    public synchronized void run() {
        for (int i = 0; i < NUMBER; i++) {
            // 不使用 synchronized(this)
            try {
                System.out.println("打印线程名:" + Thread.currentThread().getName() + ":" + (count++));
                Thread.sleep(100);
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }
    }

    public static int getCount() {
        return count;
    }
}


/**
 * {@link Parent} 对synchronized方法进行修饰和继承.
 */
class Parent {
    public synchronized void methods() {
        System.out.println("Hello,World,类对象为:" + this.getClass().getName());
    }
}

class Child extends Parent {

    /**
     * @see #methods()
     * 第一种重新方式.
     */
    @Override
    public synchronized void methods() {
        // 打印的是子类的对象名
        super.methods();
    }
}

/**
 * {@link SynchroThread} 修饰静态方法
 */
class SynchroThread implements Runnable {

    private static final int NUMBER = 6;
    private static AtomicInteger atomicInteger;

    public SynchroThread() {
        atomicInteger = new AtomicInteger(-1);
    }

    public static synchronized void methods() {
        for (int i = 0; i < NUMBER; i++) {
            try {
                System.out.println("打印线程名(原子性操作):" + Thread.currentThread().getName() + ":" + (atomicInteger.incrementAndGet()));
                Thread.sleep(100);
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void run() {
        methods();
    }
}

/**
 * {@link SynchronizedThread} 修饰类
 */
class SynchronizedThread implements Runnable {
    private static int count;
    private static final int NUMBER = 6;

    public SynchronizedThread() {
        count = 0;
    }

    @Override
    public void run() {
        synchronized (SynchronizedThread.class) {
            for (int i = 0; i < NUMBER; i++) {
                try {
                    System.out.println("打印线程名:" + Thread.currentThread().getName() + ":" + (count++));
                    Thread.sleep(100);
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }
}