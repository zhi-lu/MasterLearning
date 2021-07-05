package com.luzhi.miniTomcat.watcher;

import cn.hutool.core.io.watch.WatchMonitor;
import cn.hutool.core.io.watch.WatchUtil;
import cn.hutool.core.io.watch.Watcher;
import cn.hutool.log.LogFactory;
import com.luzhi.miniTomcat.catalina.Context;

import java.lang.annotation.Native;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/6/7
 * 创建上下文资源数据文件改变的监听器.
 */
public class ContextFileChangeWatcher {

    /**
     * @see #MAX_VALUE
     * 引用本机代码引用常量值.大小为2<sup>31</sup> -1 大小
     */
    @Native
    private static final int MAX_VALUE = 0x7fffffff;

    /**
     * @see #stop
     * 作为标记是否停止
     */
    private volatile boolean stop = false;

    /**
     * @see #watchMonitor
     * 是真正起作用的监听器
     */
    private final WatchMonitor watchMonitor;

    /**
     * @see #JAR_FILE
     * @see #CLASS_FILE
     * @see #XML_FILE
     * 对需要监听和处理文件的确定后缀
     */
    private static final String JAR_FILE = ".jar";
    private static final String CLASS_FILE = ".class";
    private static final String XML_FILE = ".xml";

    /**
     * 初始该自定义监听器
     *
     * @param context 获取Context对象.对docBase下的文件进行监听.
     * @see #ContextFileChangeWatcher(Context)
     */
    public ContextFileChangeWatcher(Context context) {
        // Integer.MAX_VALUE 替换成 @Native private static final int MAX_VALUE = 0x7fffffff;
        this.watchMonitor = WatchUtil.createAll(context.getDocBase(), MAX_VALUE, new Watcher() {

            /**
             * 对监听事件的处理.自然使用同步.处理文件一个一个去处理.避免出现多次context.reload()
             * @see #dealWith(WatchEvent)
             * @param event 处理的监听事务
             */
            private void dealWith(WatchEvent<?> event) {
                synchronized (ContextFileChangeWatcher.class) {
                    String fileName = event.context().toString();
                    if (stop) {
                        return;
                    }
                    if (fileName.endsWith(JAR_FILE) || fileName.endsWith(CLASS_FILE) || fileName.endsWith(XML_FILE)) {
                        // 设置该Context对象下已经设置.
                        stop = true;
                        LogFactory.get().info(ContextFileChangeWatcher.this + "该监测器检查到重要文件:{}发生改变", fileName);
                        // 重新加载
                        context.reload();
                    }
                }
            }

            @Override
            public void onCreate(WatchEvent<?> event, Path currentPath) {
                dealWith(event);
            }

            @Override
            public void onModify(WatchEvent<?> event, Path currentPath) {
                dealWith(event);
            }

            @Override
            public void onDelete(WatchEvent<?> event, Path currentPath) {
                dealWith(event);
            }

            @Override
            public void onOverflow(WatchEvent<?> event, Path currentPath) {
                dealWith(event);
            }
        });
        // 将当前的监听器设置为守护线程.
        this.watchMonitor.setDaemon(true);
    }

    /**
     * @see #start()
     * 开启监听器
     */
    public void start() {
        watchMonitor.start();
    }

    /**
     * @see #stop()
     * 关闭监听器
     */
    public void stop() {
        watchMonitor.close();
    }
}
