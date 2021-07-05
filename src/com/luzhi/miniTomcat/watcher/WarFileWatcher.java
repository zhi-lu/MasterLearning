package com.luzhi.miniTomcat.watcher;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.watch.WatchMonitor;
import cn.hutool.core.io.watch.WatchUtil;
import cn.hutool.core.io.watch.Watcher;
import com.luzhi.miniTomcat.catalina.Host;
import com.luzhi.miniTomcat.util.ConstantTomcat;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/7/2
 * 监控webapps下的文件,如果存在新创建的.war文件.则由{@link com.luzhi.miniTomcat.catalina.Host#loadWar(File)}方法进行处理.
 */
@SuppressWarnings("unused")
public class WarFileWatcher {

    /**
     * 创建监听器
     *
     * @see #watchMonitor
     */
    private final WatchMonitor watchMonitor;

    /**
     * war文件的扩展名:
     *
     * @see #WAR_EXTENSION
     */
    private static final String WAR_EXTENSION = ".war";

    public WarFileWatcher(Host host) {
        this.watchMonitor = WatchUtil.createAll(ConstantTomcat.WEBAPPS_FOLDER, 1, new Watcher() {
            private void dealWith(WatchEvent<?> event, Path currentPath) {
                synchronized (WarFileWatcher.class) {
                    String fileName = event.context().toString();
                    if (fileName.toLowerCase().endsWith(WAR_EXTENSION) && ENTRY_CREATE.equals(event.kind())) {
                        File warFile = FileUtil.file(ConstantTomcat.WEBAPPS_FOLDER, fileName);
                        host.loadWar(warFile);
                    }
                }

            }

            @Override
            public void onCreate(WatchEvent<?> event, Path currentPath) {
                dealWith(event, currentPath);
            }

            @Override
            public void onModify(WatchEvent<?> event, Path currentPath) {
                dealWith(event, currentPath);
            }

            @Override
            public void onDelete(WatchEvent<?> event, Path currentPath) {
                dealWith(event, currentPath);
            }

            @Override
            public void onOverflow(WatchEvent<?> event, Path currentPath) {
                dealWith(event, currentPath);
            }
        });
    }

    public void start() {
        watchMonitor.start();
    }

    public void stop() {
        watchMonitor.interrupt();
    }
}
