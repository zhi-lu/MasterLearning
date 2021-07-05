package com.luzhi.miniTomcat.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.log.LogFactory;
import com.luzhi.miniTomcat.util.ServerXmlUtil;

import java.util.List;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/5/21
 * 一个Service通常只有一个Engine,不做成集合了.
 */
@SuppressWarnings("unused")
public class Service {

    private final Engine engine;
    private final Server server;
    private final List<Connector> connectorList;

    public Service(Server server) {

        this.server = server;
        this.engine = new Engine(this);
        String name = ServerXmlUtil.getServiceName();
        this.connectorList = ServerXmlUtil.connectorList(this);
    }

    public Engine getEngine() {
        return engine;
    }

    public Server getServer() {
        return server;
    }

    public void init() {
        start();
    }

    private void start() {
        TimeInterval interval = DateUtil.timer();
        for (Connector connector : connectorList) {
            connector.init();
        }
        LogFactory.get().info("初始化时进程使用{}毫秒", interval.intervalMs());
        for (Connector connector : connectorList) {
            connector.start();
        }
    }
}
