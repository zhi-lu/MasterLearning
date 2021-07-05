package com.luzhi.miniTomcat.catalina;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.log.LogFactory;
import com.luzhi.miniTomcat.http.Request;
import com.luzhi.miniTomcat.http.Response;
import com.luzhi.miniTomcat.servlets.DefaultServlet;
import com.luzhi.miniTomcat.servlets.InvokerServlet;
import com.luzhi.miniTomcat.servlets.JspServlet;
import com.luzhi.miniTomcat.util.ConstantTomcat;
import com.luzhi.miniTomcat.util.SessionManagerUtil;

import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author apple
 * @version jdk1.8
 * @since 1.8
 * // 创建一个Http处理器,分担{@link Connector} 类的任务
 */
public class HttpProcessor {

    /**
     * 返回异常信息的长度不超过20.
     *
     * @see #SIZE
     */
    private static final int SIZE = 20;

    /**
     * 检查压缩方式.
     *
     * @see #GZIP
     */
    private static final String GZIP = "gzip";

    /**
     * 处理jsp文件.
     *
     * @see #END_WITH
     */
    private static final String END_WITH = ".jsp";

    /**
     * 进行分隔。
     *
     * @see #SEPARATOR
     */
    private static final String SEPARATOR = ";";

    /**
     * 压缩器是否启动
     *
     * @see #START_ON
     */
    private static final String START_ON = "on";

    public void execute(Socket socket, Request request, Response response) {
        try {
            // 在传送的时候需要添加相关的HTTP协议和活动码.
            String uri = request.getUri();
            if (null == uri) {
                return;
            }
            System.out.println("打印Uri:" + uri);
            prepareSession(request, response);
            // 通过Context对象来获取资源地址.
            Context context = request.getContext();
            HttpServlet workingServlet;
            String servletClassName = context.useUrlToServletClassName(uri);
            if (null != servletClassName) {
                workingServlet = InvokerServlet.getInstance();
            } else if (uri.endsWith(END_WITH)) {
                workingServlet = JspServlet.getInstance();
            } else {
                workingServlet = DefaultServlet.getInstance();
            }
            List<Filter> filterList = request.getContext().getMatchedFilters(request.getRequestURI());
            ApplicationFilterChain applicationFilterChain = new ApplicationFilterChain(filterList, workingServlet);
            applicationFilterChain.doFilter(request, response);
            // 如果是内部服务器跳转,就不进行处理.
            if (request.isForwarded()) {
                return;
            }
            if (ConstantTomcat.CODE_200 == response.getStatus()) {
                handle200(socket, request, response);
                return;
            }
            if (ConstantTomcat.CODE_302 == response.getStatus()) {
                handle302(socket, response);
                return;
            }
            if (ConstantTomcat.CODE_404 == response.getStatus()) {
                handle404(socket, uri);
            }
        } catch (Exception exception) {
            // 日志打印输出异常.
            LogFactory.get().error(exception);
            handle500(socket, exception);
        } finally {
            try {
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException exception) {
                System.out.println("打印异常原因:" + exception.getMessage());
            }
        }
    }

    /**
     * 准备获取Session对象.
     *
     * @param request  获取{@link Request} 对象
     * @param response 获取{@link Response} 对象
     * @see #prepareSession(Request, Response)
     */
    public void prepareSession(Request request, Response response) {
        String jSessionId = request.getJSessionIdFormCookie();
        HttpSession session = SessionManagerUtil.getSession(jSessionId, request, response);
        request.setSession(session);
    }

    /**
     * 对相关的信息写入一个字符数组中.通过输出流{@link OutputStream} 向浏览器传送必需的Http头内容和活动码和相关的信息.
     *
     * @param socket   获取一个套接字对象
     * @param request  获取{@link Request} 对象.
     * @param response 获取{@link Response}对象.
     * @see #handle200(Socket, Request, Response)
     */
    protected static void handle200(Socket socket, Request request, Response response) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        String content = response.getContentType();
        String headText;
        String cookieHeader = response.getCookiesHeader();
        byte[] body = response.getBody();
        boolean gzip = isGzip(request, body, content);
        // 如果文件满足压缩,将页面头信息设置为可以压缩202请求,不满足则将返回普通的202请求.
        if (gzip) {
            headText = ConstantTomcat.RESPONSE_HEAD_202_GZIP;
        } else {
            headText = ConstantTomcat.RESPONSE_HEAD_202;
        }
        if (gzip) {
            body = ZipUtil.gzip(body);
            System.out.println("数据已经进行压缩:");
        }
        // 对字符串进行格式化
        headText = StrUtil.format(headText, content, cookieHeader);
        // 获取"头内容"和相关信息.
        byte[] head = headText.getBytes(StandardCharsets.UTF_8);
        // 写入到一个大的字符数据中.
        byte[] responseText = new byte[head.length + body.length];
        ArrayUtil.copy(head, 0, responseText, 0, head.length);
        ArrayUtil.copy(body, 0, responseText, head.length, body.length);

        outputStream.write(responseText, 0, responseText.length);
        outputStream.flush();
        outputStream.close();
    }

    /**
     * 处理重定向(永久定向和临时定向都按临时302处理.)
     *
     * @param socket   获取{@link Socket}套接字对象获取{@link OutputStream}对象.
     * @param response 获取重定向的路径.
     * @see #handle302(Socket, Response)
     */
    protected static void handle302(Socket socket, Response response) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        String headText = StrUtil.format(ConstantTomcat.RESPONSE_HEAD_302, response.getRedirectPath());
        outputStream.write(headText.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 对资源不存在创建404界面.
     *
     * @param socket 获取{@link Socket}套接字对象获取{@link OutputStream}对象.
     * @param uri    获取资源名(如果不存在资源).
     * @see #handle404(Socket, String)
     */
    protected static void handle404(Socket socket, String uri) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        String responseText = StrUtil.format(ConstantTomcat.FORMAT_STRING_404, uri, uri);
        responseText = ConstantTomcat.RESPONSE_HEAD_404 + responseText;
        byte[] responseByte = responseText.getBytes(StandardCharsets.UTF_8);
        outputStream.write(responseByte);
    }

    /**
     * 如果出现服务器出现异常,创建500页面.
     *
     * @param socket    通过{@link Socket}套接字对象获取{@link OutputStream}
     * @param exception 获取{@link Exception} 对象
     * @see #handle500(Socket, Exception)
     */
    protected static void handle500(Socket socket, Exception exception) {
        try {
            // 拿到出现异常的堆栈.
            OutputStream outputStream = socket.getOutputStream();
            StackTraceElement[] stackTraceElements = exception.getStackTrace();
            //noinspection MismatchedQueryAndUpdateOfStringBuilder
            StringBuilder stringBuilder = new StringBuilder();
            // 添加出现异常的类名,往字符串生成器添加相关的信息.
            stringBuilder.append(exception);
            stringBuilder.append("\r\n");
            for (StackTraceElement stackTraceElement : stackTraceElements) {
                stringBuilder.append("\t");
                stringBuilder.append(stackTraceElement.toString());
                stringBuilder.append("\r\n");
            }
            // 如果返回的异常信息不为空且长度大于20.进行截取
            String message = exception.getMessage();
            if (null != message && message.length() > SIZE) {
                message = message.substring(0, 19);
            }
            // 格式化
            String responseText = StrUtil.format(ConstantTomcat.FORMAT_STRING_500, message, exception.toString(), stringBuilder.toString());
            responseText = ConstantTomcat.RESPONSE_HEAD_500 + responseText;
            // 转化为字符数组.
            byte[] responseBytes = responseText.getBytes(StandardCharsets.UTF_8);
            outputStream.write(responseBytes);

        } catch (IOException e) {

            System.out.println("打印异常原因:" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 判断文件是否可以进行压缩.
     *
     * @param request  通过{@link Request} 对象获取 {@link Connector}对象
     * @param bytes    压缩的文件内容
     * @param mimeType 文件类型.
     * @see #isGzip(Request, byte[], String)
     */
    private static boolean isGzip(Request request, byte[] bytes, String mimeType) {
        String acceptEncoding = request.getHeader("Accept-Encoding");
        // 如果头信息中的Accept-Encoding是否包括gzip
        if (!StrUtil.containsAny(acceptEncoding, GZIP)) {
            return false;
        }
        Connector connector = request.getConnector();
        if (mimeType.contains(SEPARATOR)) {
            mimeType = StrUtil.subBefore(mimeType, ";", false);
        }
        // 解析的web.xml中compression的s是否启动"element value 为 on"
        if (!START_ON.equals(connector.getCompression())) {
            return false;
        }
        // 判断文件大小是否满足压缩条件.
        if (bytes.length < connector.getCompressionMinSize()) {
            return false;
        }
        String userAgents = connector.getOnCompressionUserAgents();
        String[] eachUserAgents = userAgents.split(",");
        // 遍历头信息User-Agent判断是否存在不需要压缩的浏览器.
        for (String eachUserAgent : eachUserAgents) {
            eachUserAgent = eachUserAgent.trim();
            String userAgent = request.getHeader("User-Agent");
            if (StrUtil.containsAny(userAgent, eachUserAgent)) {
                return false;
            }
        }
        String mimeTypes = connector.getCompressibleMimeType();
        String[] eachMimeTypes = mimeTypes.split(",");
        // 遍历判断文件是否满足压缩文件的类型.
        for (String eachMimeType : eachMimeTypes) {
            if (mimeType.equals(eachMimeType)) {
                System.out.println("可以进行压缩");
                return true;
            }
        }
        return false;
    }
}

