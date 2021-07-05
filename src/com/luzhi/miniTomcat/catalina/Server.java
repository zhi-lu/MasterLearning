package com.luzhi.miniTomcat.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.log.LogFactory;
import cn.hutool.system.SystemUtil;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/5/21
 * Server是最外层的元素,代表服务器本身.
 */
public class Server {

    private final Service service;

    public Server() {
        this.service = new Service(this);
    }

    public void start() {
        TimeInterval timeInterval = DateUtil.timer();
        logJvm();
        init();
        LogFactory.get().info("服务器启动使用{}毫秒", timeInterval.intervalMs());
    }

    /**
     * 由Server(服务器)进行处理.
     *
     * @see #init() 对请求和访问进行初始化.
     */
    private void init() {
        service.init();
    }

    /**
     * 此方法用作设置相关的输出日志.
     * 在该方法中使用 {@link LinkedHashMap} 而不用 {@link java.util.HashMap} 是因为前者存放是由序的Hash键值
     * 后者为无序存放Hash的键值.
     *
     * @see #logJvm()
     */
    private static void logJvm() {
        // 定义存放相关信息的Map.(Server 版本, System 相关信息, Jvm的一些相关信息.)
        Map<String, String> mapInfo = new LinkedHashMap<>(512);
        mapInfo.put("Server version", "Luzhi miniTomcat/1.0.1");
        mapInfo.put("Server built", "2021-5-21, 21:31:22");
        mapInfo.put("Server name", "1.0.1");
        mapInfo.put("OS Name \t", SystemUtil.get("os.name"));
        mapInfo.put("OS Version", SystemUtil.get("os.version"));
        mapInfo.put("System Architecture", SystemUtil.get("os.arch"));
        mapInfo.put("Java Home", SystemUtil.get("java.home"));
        mapInfo.put("Jvm Version", SystemUtil.get("java.runtime.version"));
        mapInfo.put("Jvm Vendor", SystemUtil.get("java.vm.specification.vendor"));
        Set<String> keySet = mapInfo.keySet();
        for (String key : keySet) {
            // 创建日志工厂设置info类型日志.
            LogFactory.get().info(key + ":\t\t" + mapInfo.get(key));
        }
    }
}
