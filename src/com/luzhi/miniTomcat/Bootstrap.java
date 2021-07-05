package com.luzhi.miniTomcat;

import com.luzhi.miniTomcat.catalina.Server;
import com.luzhi.miniTomcat.classloader.CommonClassLoader;

import java.lang.reflect.Method;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/4/21
 * 生成miniTomcat的引导程序.由自定义的加载器进行加载指定{@link Server#start()} 方法.
 */
public class Bootstrap {

    public static void main(String[] args) throws Exception {
        CommonClassLoader commonClassLoader = new CommonClassLoader();
        Thread.currentThread().setContextClassLoader(commonClassLoader);
        String serverClassName = "com.luzhi.miniTomcat.catalina.Server";
        Class<?> clazz = commonClassLoader.loadClass(serverClassName);
        Object object = clazz.newInstance();
        Method method = clazz.getDeclaredMethod("start");
        method.invoke(object);
        System.out.println("获取当前类的加载为:" + clazz.getClassLoader());
    }
}
