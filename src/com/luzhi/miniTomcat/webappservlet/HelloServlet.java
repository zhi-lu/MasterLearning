package com.luzhi.miniTomcat.webappservlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/5/21
 * 生成一个最简单的功能。
 */
public class HelloServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getWriter().println("<h1 align=center style='color:pink'>Hello, It's really easy servlet</h1>");
    }
}
