package com.luzhi.miniTomcat.servlets;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.luzhi.miniTomcat.catalina.Context;
import com.luzhi.miniTomcat.http.Request;
import com.luzhi.miniTomcat.http.Response;
import com.luzhi.miniTomcat.util.ConstantTomcat;
import com.luzhi.miniTomcat.util.WebXmlUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/6/1
 * 创建DefaultServlet用于处理Servlet中的静态资源.
 */
public class DefaultServlet extends HttpServlet {

    private static final Long serialVersionUID = 1L;
    /**
     * @see #STATUES_500
     * <p>
     * 活动码为500服务器出现异常.使用/500.html
     */
    private static final String STATUES_500 = "/500.html";

    /**
     * @see #STATUES_INIT
     * <p>
     * /初始化界面.
     */
    private static final String STATUES_INIT = "/";

    /**
     * @see #END_WITH
     * <p>
     * 处理jsp文件.
     */
    private static final String END_WITH = ".jsp";

    /**
     * @see #STATUES_LOGIN
     * <p>
     * 访问login.html界面.
     */
    private static final String STATUES_LOGIN = "login.html";

    public static volatile DefaultServlet instance = new DefaultServlet();

    public static synchronized DefaultServlet getInstance() {
        return instance;
    }

    /**
     * @see #DefaultServlet()
     * 禁止其他人实例化该对象.
     */
    private DefaultServlet() {
    }

    /**
     * 此服务处理servlet中的静态资源.将{@link com.luzhi.miniTomcat.catalina.HttpProcessor} 中处理静态资源的任务由该方法进行处理.
     *
     * @param req  获取{@link Request} 对象从{@link com.luzhi.miniTomcat.catalina.HttpProcessor} 传入
     * @param resp 获取{@link Response} 上同
     * @see #service(HttpServletRequest, HttpServletResponse)
     */
    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Request request = (Request) req;
        Response response = (Response) resp;
        // 通过context获取资源地址.
        Context context = request.getContext();
        String uri = request.getUri();
        // 处理服务器异常
        if (STATUES_500.equals(uri)) {
            throw new RuntimeException("It's used to test. Thus, the statement returns Exception");
        }
        // 对初始化路径文本的判断.
        if (STATUES_INIT.equals(uri)) {
            uri = WebXmlUtil.getWelcomeFile(request.getContext());
        }
        // 如果是jsp文件则交由JspServlet处理.
        if (uri.endsWith(END_WITH)) {
            JspServlet.getInstance().service(request, response);
        }
        // 对特定的文件获取相关的文件对象
        String fileName = StrUtil.removePrefix(uri, "/");
        // 这种是通过自定的ApplicationServletContext封装的继承后实现ServletContext.getRealPath(String name)方法获取资源路径.
        File file = FileUtil.file(request.getRealPath(fileName));
        //多级目录进行访问.
        if (!file.isFile()) {
            uri = uri + "/" + WebXmlUtil.getWelcomeFile(request.getContext());
            fileName = StrUtil.removePrefix(uri, "/");
            // 自定义构造,获取文件对象.
            file = FileUtil.file(context.getDocBase(), fileName);
        }
        // 如果文件存在则文件将文件字符转化为UTF8形式的字符串文本.
        if (file.exists()) {
            String extensionName = FileUtil.extName(file);
            String mimeType = WebXmlUtil.getMimeName(extensionName);
            response.setContentType(mimeType);
            // 将原来的文本处理,现在直接读取为二进制文件.
            byte[] bytes = FileUtil.readBytes(file);
            response.setBytes(bytes);
            // 当访问的文件名为:login.html,则当前线程挂起1000毫秒.
            if (STATUES_LOGIN.equals(fileName)) {
                ThreadUtil.sleep(1000);
            }
            // 和{@link com.luzhi.miniTomcat.InvokerServlet}对界面的操作相同。
            if (null != response.getRedirectPath()) {
                response.setStatus(ConstantTomcat.CODE_302);
            } else {
                response.setStatus(ConstantTomcat.CODE_200);
            }
        } else {
            response.setStatus(ConstantTomcat.CODE_404);
        }
    }
}
