package com.luzhi.miniTomcat.catalina;

import cn.hutool.core.util.ArrayUtil;

import javax.servlet.*;
import java.io.IOException;
import java.util.List;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/6/27
 * 生成筛选器责任链
 */
public class ApplicationFilterChain implements FilterChain {

    private final Filter[] filters;
    private final Servlet servlet;
    int pos = 0;

    /**
     * 初始方法责任链
     *
     * @param filterList 获取筛选器列表
     * @param servlet    获取当前的servlet
     * @see #ApplicationFilterChain(List, Servlet)
     */
    public ApplicationFilterChain(List<Filter> filterList, Servlet servlet) {
        this.filters = ArrayUtil.toArray(filterList, Filter.class);
        this.servlet = servlet;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        if (pos < filters.length) {
            Filter filter = filters[pos++];
            filter.doFilter(request, response, this);
        } else {
            servlet.service(request, response);
        }
    }
}
