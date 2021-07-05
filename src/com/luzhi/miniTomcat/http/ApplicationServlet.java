package com.luzhi.miniTomcat.http;

import com.luzhi.miniTomcat.catalina.Context;

import java.io.File;
import java.util.*;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/6/9
 * 具体实现自定义的ServletContext.
 * @since 1.0
 */
@SuppressWarnings("unused")
public class ApplicationServlet extends BaseServletContext {

    /**
     * 创建一个存放相关的属性.
     *
     * @see #attributeMap
     */
    private final Map<String, Object> attributeMap;

    /**
     * 通过{@link Context}对象来操作.
     *
     * @see #context
     */
    private final Context context;

    public ApplicationServlet(Context context) {
        this.attributeMap = new HashMap<>(512);
        this.context = context;
    }

    @Override
    public void removeAttribute(String name) {
        attributeMap.remove(name);
    }

    public void setAttributeMap(String name, Object object) {
        attributeMap.put(name, object);
    }

    public Object getAttributeValue(String name) {
        return attributeMap.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        Set<String> setKey = attributeMap.keySet();
        return Collections.enumeration(setKey);
    }

    @Override
    public String getRealPath(String path) {
        return new File(context.getDocBase(), path).getAbsolutePath();
    }
}
