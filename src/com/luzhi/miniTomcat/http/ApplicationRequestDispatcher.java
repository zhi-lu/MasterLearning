package com.luzhi.miniTomcat.http;

import com.luzhi.miniTomcat.catalina.HttpProcessor;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.net.Socket;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/6/21
 * 生成服务端的跳转器。跳转的思路是修改了request中uri.通过{@link HttpProcessor#execute(Socket, Request, Response)}
 * 执行了一次,相关于进行一次服务器内部跳转.
 */
@SuppressWarnings("RedundantThrows")
public class ApplicationRequestDispatcher implements RequestDispatcher {

    /**
     * 默认路径"/"
     *
     * @see #DEFAULT_PATH
     */
    private static final String DEFAULT_PATH = "/";

    private final String uri;

    public ApplicationRequestDispatcher(String uri) {
        if (!uri.startsWith(DEFAULT_PATH)) {
            uri = DEFAULT_PATH + uri;
        }
        this.uri = uri;
    }

    /**
     * @param request  获取{@link Request}对象获取当前的{@link java.net.Socket}对象
     * @param response 获取{@link Response}对象
     * @see #forward(ServletRequest, ServletResponse)
     */
    @Override
    public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        Request req = (Request) request;
        Response reps = (Response) response;
        req.setUri(uri);

        HttpProcessor httpProcessor = new HttpProcessor();
        httpProcessor.execute(req.getSocket(), req, reps);
        req.setForwarded(true);
    }

    @Override
    public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {

    }
}
