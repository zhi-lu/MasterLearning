package com.luzhi.miniTomcat.http;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/6/16
 * 生成自定义标准化Session实现{@link HttpSession} 接口
 */
public class StandardSession implements HttpSession {

    /**
     * @see #attributeMap
     * <p>
     * 在session存放相关的信息
     */
    private final Map<String, Object> attributeMap;

    /**
     * @see #id
     * <p>
     * 创建session唯一标识符
     */
    private final String id;

    /**
     * @see #creationTime
     * <p>
     * session会话创建的时间
     */
    private final long creationTime;

    /**
     * @see #lastAccessedTime
     * <p>
     * 用户结束session最后访问的时间
     */
    private long lastAccessedTime;

    /**
     * @see #servletContext
     * <p>
     * 接受当前需要创建session对象的ServletContext
     */
    private final ServletContext servletContext;

    /**
     * @see #maxInactiveInterval
     * <p>
     * 最大非活动session持续的时间,一般为30分钟,如果不进行登录默认session失效.
     */
    private int maxInactiveInterval;

    public StandardSession(String jSessionId, ServletContext servletContext) {
        this.attributeMap = new HashMap<>(512);
        this.id = jSessionId;
        this.servletContext = servletContext;
        this.creationTime = System.currentTimeMillis();

    }

    @Override
    public long getCreationTime() {
        return this.creationTime;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public long getLastAccessedTime() {
        return this.lastAccessedTime;
    }

    @SuppressWarnings("unused")
    public void setLastAccessedTime(long lastAccessedTime) {
        this.lastAccessedTime = lastAccessedTime;
    }

    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
    }

    @Override
    public int getMaxInactiveInterval() {
        return this.maxInactiveInterval;
    }

    @Override
    @Deprecated
    public HttpSessionContext getSessionContext() {
        return null;
    }

    @Override
    public Object getAttribute(String name) {
        return attributeMap.get(name);
    }

    @Override
    public Object getValue(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributeMap.keySet());
    }

    @Override
    public String[] getValueNames() {
        return new String[0];
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributeMap.put(name, value);
    }

    @Override
    public void putValue(String name, Object value) {

    }

    @Override
    public void removeAttribute(String name) {
        attributeMap.remove(name);
    }

    @Override
    public void removeValue(String name) {

    }

    @Override
    public void invalidate() {
        attributeMap.clear();
    }

    @Override
    public boolean isNew() {
        return creationTime == lastAccessedTime;
    }
}
