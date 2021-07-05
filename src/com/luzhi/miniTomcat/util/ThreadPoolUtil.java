package com.luzhi.miniTomcat.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/5/21
 * 创建一个线程池工具的抽象类
 */

public abstract class ThreadPoolUtil {
    /**
     * @see #THREAD_POOL_EXECUTOR 创建一个线程工厂
     * 具体请看 {@link ThreadPoolExecutor#ThreadPoolExecutor(int, int, long, TimeUnit, BlockingQueue)} 该类的初始化构造方法.
     */
    @SuppressWarnings("AlibabaThreadShouldSetName")
    private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(20, 100, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10));

    /**
     * @see #run(Runnable)
     * 方法执行相关的线程任务.
     */
    public static void run(Runnable runnable) {
        THREAD_POOL_EXECUTOR.execute(runnable);
    }
}
