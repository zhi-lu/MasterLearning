package com.luzhi.miniTomcat.servlets;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.luzhi.miniTomcat.catalina.Context;
import com.luzhi.miniTomcat.classloader.JspClassLoader;
import com.luzhi.miniTomcat.http.Request;
import com.luzhi.miniTomcat.http.Response;
import com.luzhi.miniTomcat.util.ConstantTomcat;
import com.luzhi.miniTomcat.util.JspUtil;
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
 * // TODO : 2021/6/11
 * 创建该列对Jsp文件解析的Servlet;
 */
public class JspServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * @see #STATUES_INIT
     * <p>
     * /初始化界面.
     */
    private static final String STATUES_INIT = "/";

    private static final JspServlet INSTANCE = new JspServlet();

    public static synchronized JspServlet getInstance() {
        return INSTANCE;
    }

    /**
     * @see #JspServlet()
     * 禁止任何人对其实例化.
     */
    private JspServlet() {
    }

    @Override
    public void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        try {
            Request request = (Request) httpServletRequest;
            Response response = (Response) httpServletResponse;

            String uri = request.getRequestURI();
            if (STATUES_INIT.equals(uri)) {
                uri = WebXmlUtil.getWelcomeFile(request.getContext());
            }
            String fileName = StrUtil.removePrefix(uri, STATUES_INIT);
            File file = FileUtil.file(request.getRealPath(fileName));
            if (file.exists()) {
                Context context = request.getContext();
                String path = context.getPath();
                String subFolder;
                if (STATUES_INIT.equals(path)) {
                    subFolder = "_";
                } else {
                    subFolder = StrUtil.subAfter(path, '/', false);
                }

                String servletClassPath = JspUtil.getServletClassPath(uri, subFolder);
                File jspServletClassFile = new File(servletClassPath);
                if (!jspServletClassFile.exists()) {
                    JspUtil.compileJsp(context, file);
                } else if (file.lastModified() > jspServletClassFile.lastModified()) {
                    JspUtil.compileJsp(context, file);
                    JspClassLoader.invalidJspClassLoader(uri, context);
                }
                String extensionName = FileUtil.extName(file);
                String miniType = WebXmlUtil.getMimeName(extensionName);
                response.setContentType(miniType);
                JspClassLoader jspClassLoader = JspClassLoader.getJspClassLoader(uri, context);
                String jspServletClassName = JspUtil.getJspServletClassName(uri, subFolder);
                Class<?> jspServletClass = jspClassLoader.loadClass(jspServletClassName);
                HttpServlet servlet = context.getServlet(jspServletClass);
                servlet.service(request, response);
                response.setStatus(ConstantTomcat.CODE_200);
            } else {
                response.setStatus(ConstantTomcat.CODE_404);
            }

        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
