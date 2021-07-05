package com.luzhi.miniTomcat.http;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author apple
 * @version jdk1.8
 * // TODO: 2021/6/10
 * 生成自定义的StandardServletConfig实现{@link ServletContext}接口.
 * 该接口在Servlet初始化的过程中作为参数传入.
 */
public class StandardServletConfig implements ServletConfig {
    /**
     * @see #servletContext
     * 创建{@link ServletContext}对象,进行在构造方法进行初始化.
     */
    private final ServletContext servletContext;

    /**
     * @see #initParameters
     * 创建一个{@link Map}对象,在构造方法进行初始化,利用双重锁机制,如果构造引用的对象为空,在{@code synchronized(this){}}语句块中
     * 进行初始化定义.
     */
    private volatile Map<String, String> initParameters;
    private final String servletName;

    public StandardServletConfig(ServletContext servletContext, Map<String, String> initParameters, String servletName) {
        this.servletContext = servletContext;
        this.initParameters = initParameters;
        this.servletName = servletName;
        synchronized (StandardServletConfig.class) {
            if (null == this.initParameters) {
                this.initParameters = new HashMap<>(512);
            }
        }
    }

    @Override
    public String getServletName() {
        return servletName;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public String getInitParameter(String name) {
        return initParameters.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(initParameters.keySet());
    }
}
