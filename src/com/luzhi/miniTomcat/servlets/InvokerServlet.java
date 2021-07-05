package com.luzhi.miniTomcat.servlets;

import cn.hutool.core.util.ReflectUtil;
import com.luzhi.miniTomcat.catalina.Context;
import com.luzhi.miniTomcat.http.Request;
import com.luzhi.miniTomcat.http.Response;
import com.luzhi.miniTomcat.util.ConstantTomcat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2921/6/1
 * 时间多乎哉,生成InvokerServlet.处理Servlet.这里不设置为私有静态访问.
 * 使用双重锁机制.实例发生改变即使把线程改变的对象刷入主线程中。
 */
public class InvokerServlet extends HttpServlet {

    private static final Long serialVersionUID = 1L;

    public static volatile InvokerServlet INSTANCE = new InvokerServlet();

    public static synchronized InvokerServlet getInstance() {
        return INSTANCE;
    }

    /**
     * @see #InvokerServlet()
     * 禁止任何人实例化该对象.
     */
    private InvokerServlet() {
    }

    /**
     * @see #service(HttpServletRequest, HttpServletResponse)
     * 该方法通过强制转化生成的{@link Request}和{@link Response}对象。
     * 获取请求uri和{@link Context}对象。解析出并通过反射生成一个实例化对象.
     * 调用实例化方法{@link HttpServlet#service(HttpServletRequest, HttpServletResponse)} 将参数传入
     * 调用{@link HttpServlet#doGet(HttpServletRequest, HttpServletResponse)}方法.
     */
    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Request request = (Request) req;
        Response response = (Response) resp;

        String uri = request.getUri();
        Context context = request.getContext();
        String servletClassName = context.getServletClassName(uri);
        try {
            Class<?> servletClass = context.getWebAppClassLoader().loadClass(servletClassName);
            System.out.println("打印servlet加载类ServletClass:" + servletClass);
            System.out.println("打印类的加载器ServletClassLoader:" + servletClass.getClassLoader());
            Object servletObject = context.getServlet(servletClass);
            ReflectUtil.invoke(servletObject, "service", request, response);
            if (null != response.getRedirectPath()) {
                // 设置活动码200,意味界面跳转成功.
                response.setStatus(ConstantTomcat.CODE_302);
            } else {
                // 设置活动码200,意味事务处理成功.
                response.setStatus(ConstantTomcat.CODE_200);
            }

        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
