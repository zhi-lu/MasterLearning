package com.luzhi.miniTomcat.catalina;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/6/22
 * 主要存放Filter的参数
 */
public class StandardFilterConfig implements FilterConfig {

    /**
     * 获取相关的servlet的Context对象
     *
     * @see #servletContext
     */
    private final ServletContext servletContext;

    /**
     * 存放筛选器的初始化信息
     *
     * @see #initParameters
     */
    private Map<String, String> initParameters;

    /**
     * 获取筛选器对象名
     *
     * @see #filterName
     */
    private final String filterName;

    public StandardFilterConfig(ServletContext servletContext, Map<String, String> initParameters, String filterName) {
        this.servletContext = servletContext;
        this.initParameters = initParameters;
        this.filterName = filterName;
        if (null == this.initParameters) {
            this.initParameters = new HashMap<>(512);
        }
    }

    @Override
    public String getFilterName() {
        return filterName;
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
