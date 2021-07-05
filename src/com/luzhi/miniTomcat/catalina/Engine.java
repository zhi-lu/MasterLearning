package com.luzhi.miniTomcat.catalina;

import com.luzhi.miniTomcat.util.ServerXmlUtil;

import java.util.List;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/5/21
 * 对miniTomcat的内置对象Engine进行处理.
 */
@SuppressWarnings("unused")
public class Engine {

    private final String defaultName;
    private final List<Host> hosts;
    private final Service service;

    public Engine(Service service) {
        this.defaultName = ServerXmlUtil.getEngineName();
        this.hosts = ServerXmlUtil.getHosts(this);
        this.service = service;
        checkDefaultHost();

    }

    /**
     * 检查{@link #getDefaultHost()}返回是否为空。
     *
     * @throws RuntimeException 如果Host为空抛出为运行异常.
     * @see #checkDefaultHost()
     */
    private void checkDefaultHost() {
        if (null == getDefaultHost()) {
            throw new RuntimeException("the defaultHost: " + this.defaultName + " not exist");
        }

    }

    /**
     * 遍历Engine的Host标签是否存在defaultHost。
     * 理论上可以处理多个Host,但事实上只处理一个Host.本地主机.
     *
     * @see #getDefaultHost()
     */
    public Host getDefaultHost() {
        for (Host host : hosts) {
            if (this.defaultName.equals(host.getName())) {
                return host;
            }
        }
        return null;
    }

    public Service getService() {
        return service;
    }
}
