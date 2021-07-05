package com.luzhi.miniTomcat.http;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.luzhi.miniTomcat.catalina.Connector;
import com.luzhi.miniTomcat.catalina.Context;
import com.luzhi.miniTomcat.catalina.Engine;
import com.luzhi.miniTomcat.catalina.Service;
import com.luzhi.miniTomcat.util.MiniBrowser;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/5/11
 * 生成miniTomcat的Request,为了把Request参数传入Servlet中,需要实现 {@link HttpServletRequest}
 */
public class Request extends BaseRequest {
    /**
     * 获取对Http解析的字符串
     *
     * @see #requestString
     */
    private String requestString;

    /**
     * 创建Context对象.
     *
     * @see #context
     */
    private Context context;

    /**
     * 处理的uri请求.
     *
     * @see #uri
     */
    private String uri;

    /**
     * 返回请求的method。
     *
     * @see #method
     */
    private String method;

    /**
     * 准备查询参数.
     *
     * @see #queryString
     */
    private String queryString;

    /**
     * 当前的请求是否为转发
     *
     * @see #forwarded
     */
    private boolean forwarded;

    /**
     * 从Connector获取Service对象,再从Service标签中获取Engine对象,在从获取的Engine获取Host对象
     *
     * @see #connector
     */
    private final Connector connector;

    /**
     * 从启动器获取Socket(套接字).
     *
     * @see #socket
     */
    private final Socket socket;

    /**
     * 创建一个Session对象.
     *
     * @see #session
     */
    private HttpSession session;

    /**
     * 存放相关参数的Map
     *
     * @see #paramMaps
     */
    private final Map<String, String[]> paramMaps;

    /**
     * 存放请求的头信息
     *
     * @see #headerMaps
     */
    private final Map<String, String> headerMaps;

    /**
     * 存放信息,便于服务器传参数.
     *
     * @see #attributesMap
     */
    private final Map<String, Object> attributesMap;

    /**
     * 存放Cookie对象的数组
     *
     * @see #cookies
     */
    private Cookie[] cookies;

    /**
     * 用于解析指定'?'是否出现被截取对字符串中
     *
     * @see #SEARCH_CHAR
     */
    private static final char SEARCH_CHAR = '?';

    /**
     * 用于分割地址的字符串. ==== "/"
     *
     * @see #DIVISION_STRING
     */
    private static final String DIVISION_STRING = "/";

    /**
     * 处理 get 请求
     *
     * @see #GET
     */
    private static final String GET = "GET";

    /**
     * 处理 post 请求
     *
     * @see #POST
     */
    private static final String POST = "POST";

    /**
     * http协议默认端口为:80
     *
     * @see #HTTP_PORT
     */
    private static final int HTTP_PORT = 80;

    /**
     * https协议默认端口为:443
     *
     * @see #HTTPS_PORT
     */
    private static final int HTTPS_PORT = 443;

    /**
     * http协议
     *
     * @see #HTTP
     */
    private static final String HTTP = "http";

    /**
     * https协议.
     *
     * @see #HTTPS
     */
    private static final String HTTPS = "https";

    /**
     * JSESSIONID
     *
     * @see #JSESSIONID
     */
    private static final String JSESSIONID = "JSESSIONID";

    /**
     * 初始化构造函数,由{@link Request}直接对http请求和uri进行解析。
     *
     * @see #Request(Socket, Connector)
     */
    public Request(Socket socket, Connector connector) throws IOException {
        this.socket = socket;
        this.connector = connector;
        this.paramMaps = new HashMap<>(512);
        this.headerMaps = new HashMap<>(512);
        this.attributesMap = new HashMap<>(512);
        parseHttpRequest();
        // 判断解析的Http请求的"头"和请求内容不为空.
        if (StrUtil.isEmpty(this.requestString)) {
            return;
        }
        parseUri();
        parseMethod();
        parseContext();
        if (!DIVISION_STRING.equals(context.getPath())) {
            uri = StrUtil.removePrefix(uri, context.getPath());
            // 如果去除context.getPath前缀后返回为空字符串
            if (StrUtil.isEmpty(uri)) {
                // 将uri 设置为"/"
                uri = "/";
            }
        }
        parseParameters();
        parseHeaderMap();
        parseCookie();
    }

    /**
     * 解析请求中method(Get,Post),获取请求值,并且不从最后开始索引.
     *
     * @see #parseMethod()
     */
    private void parseMethod() {
        method = StrUtil.subBefore(requestString, " ", false);
    }

    /**
     * 该方法对Http请求进行解析.使用{@link MiniBrowser#readBytes(InputStream, boolean)}
     * 返回一个解析完成的byte[]数组
     *
     * @see #parseHttpRequest()
     */
    private void parseHttpRequest() throws IOException {
        InputStream inputStream = this.socket.getInputStream();
        byte[] bytes = MiniBrowser.readBytes(inputStream, false);
        // 推荐使用StandardCharsets.UTF_8,因为会自动抛出 "字符不支持异常"
        this.requestString = new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * 该方法对Http解析返回的"头"和请求内容进行解析.
     *
     * @see #parseUri()
     */
    private void parseUri() {
        String temp;
        // 对字符进行截取,获取被两个字符空格截取对部分。
        temp = StrUtil.subBetween(this.requestString, " ", " ");
        // 判断'?' 是否在已经截取对字符串中.
        if (!StrUtil.contains(temp, SEARCH_CHAR)) {
            // '？' 不在截取的字符串中.
            this.uri = temp;
            return;
        }
        // 如果'？' 在截取的字符串中,返回前面的部分.不查找最后的一个分割字符串.
        temp = StrUtil.subBefore(temp, '?', false);
        this.uri = temp;
    }

    /**
     * 在Request 中创建解析Context.
     *
     * @see #parseContext()
     */
    private void parseContext() {
        Service service = connector.getService();
        Engine engine = service.getEngine();
        context = engine.getDefaultHost().getContext(uri);
        if (null != context) {
            return;
        }
        // 截取字符串
        String path = StrUtil.subBetween(this.uri, "/", "/");
        if (path == null) {
            path = "/";
        } else {
            path = "/" + path;
        }
        // 通过截取的字符串获取Context对象.
        context = engine.getDefaultHost().getContext(path);
        if (context == null) {
            context = engine.getDefaultHost().getContext("/");
        }
    }

    private void parseParameters() {
        System.out.println("打印queryString为:" + this.queryString);
        if (GET.equals(this.getMethod())) {
            String url = StrUtil.subBetween(requestString, " ", " ");
            if (StrUtil.contains(url, SEARCH_CHAR)) {
                queryString = StrUtil.subAfter(url, SEARCH_CHAR, false);
            }
        }
        if (POST.equals(this.getMethod())) {
            queryString = StrUtil.subAfter(requestString, "\r\n\r\n", false);
        }
        if (null == queryString) {
            return;
        }
        queryString = URLUtil.decode(queryString);
        String[] parametersValues = queryString.split("&");
        for (String parameterValue : parametersValues) {
            String[] nameValues = parameterValue.split("=");
            String name = nameValues[0];
            String value = nameValues[1];
            String[] values = paramMaps.get(name);
            if (null == values) {
                values = new String[]{value};
            } else {
                values = ArrayUtil.append(values, value);
            }
            paramMaps.put(name, values);
        }
    }

    public void parseHeaderMap() {
        StringReader stringReader = new StringReader(requestString);
        List<String> list = new ArrayList<>();
        // 把数据从Reader读取Collection集合中
        IoUtil.readLines(stringReader, list);
        for (int i = 1; i < list.size(); i++) {
            String line = list.get(i);
            if (line.length() == 0) {
                break;
            }
            String[] speList = line.split(":");
            String headerName = speList[0].toLowerCase();
            String headerValue = speList[1];
            headerMaps.put(headerName, headerValue);
        }
    }

    private void parseCookie() {
        List<Cookie> cookieList = new ArrayList<>();
        String cookieStringList = headerMaps.get("cookie");
        if (null != cookieStringList) {
            // 根据";"分隔获取数组
            String[] pairs = StrUtil.split(cookieStringList, ";");
            for (String pair : pairs) {
                if (null == pair) {
                    continue;
                }
                // 将获得的每个Cookie(key-value)根据"="进行分隔.
                String[] speCookie = StrUtil.split(pair, "=");
                Cookie cookie = new Cookie(speCookie[0].trim(), speCookie[1].trim());
                cookieList.add(cookie);
            }
        }
        this.cookies = ArrayUtil.toArray(cookieList, Cookie.class);
    }

    /**
     * 通过Cookie对象获取JSEESIONID
     *
     * @see #getJSessionIdFormCookie()
     */
    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    public String getJSessionIdFormCookie() {
        if (null == cookies) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (JSESSIONID.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public void setSession(HttpSession session) {
        this.session = session;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setForwarded(boolean forwarded) {
        this.forwarded = forwarded;
    }

    public Context getContext() {
        return context;
    }

    public String getUri() {
        return uri;
    }

    public String getRequestString() {
        return requestString;
    }

    public Connector getConnector() {
        return connector;
    }

    public boolean isForwarded() {
        return forwarded;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String uri) {
        return new ApplicationRequestDispatcher(uri);
    }

    @Override
    public HttpSession getSession() {
        return session;
    }

    @Override
    public Cookie[] getCookies() {
        return this.cookies;
    }

    @Override
    public String getParameter(String name) {
        String[] values = paramMaps.get(name);
        if (null != values && 0 != values.length) {
            return values[0];
        }
        return null;
    }

    @Override
    public void setAttribute(String name, Object object) {
        attributesMap.put(name, object);
    }

    @Override
    public void removeAttribute(String name) {
        attributesMap.remove(name);
    }

    @Override
    public Object getAttribute(String name) {
        return attributesMap.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributesMap.keySet());
    }

    @Override
    public HttpSession getSession(boolean b) {
        return super.getSession(b);
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public ServletContext getServletContext() {
        return context.getServletContext();
    }

    @Override
    public String getRealPath(String name) {
        return context.getServletContext().getRealPath(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return this.paramMaps;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(paramMaps.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return paramMaps.get(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(headerMaps.keySet());
    }

    @Override
    public String getHeader(String name) {
        if (null == name) {
            return null;
        }
        return headerMaps.get(name.toLowerCase());
    }

    @Override
    public int getIntHeader(String name) {
        // 将值转换为int值,转换失败默认为0
        return Convert.toInt(headerMaps.get(name), 0);
    }

    @Override
    public String getLocalAddr() {
        return socket.getLocalAddress().getHostAddress();
    }

    @Override
    public String getLocalName() {
        return socket.getLocalAddress().getHostName();
    }

    @Override
    public int getLocalPort() {
        return socket.getLocalPort();
    }

    @Override
    public String getProtocol() {
        return "HTTP:/1.1";
    }

    @Override
    public String getRemoteAddr() {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
        String temp = inetSocketAddress.getAddress().toString();
        return StrUtil.subAfter(temp, "/", false);
    }

    @Override
    public String getRemoteHost() {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
        return inetSocketAddress.getHostName();
    }

    @Override
    public String getScheme() {
        return "http";
    }

    @Override
    public String getServerName() {
        return getHeader("host").trim();
    }

    @Override
    public int getServerPort() {
        return getLocalPort();
    }

    @Override
    public String getContextPath() {
        String result = this.context.getPath();
        if (DIVISION_STRING.equals(result)) {
            return "";
        }
        return result;
    }

    @Override
    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    public String getRequestURI() {
        return uri;
    }

    @Override
    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    public StringBuffer getRequestURL() {
        StringBuffer url = new StringBuffer();
        String scheme = getScheme();
        int port = getServerPort();
        if (port < 0) {
            port = 80;
        }
        url.append(scheme);
        url.append("://");
        url.append(getServerName());
        boolean http = (scheme.equals(HTTP) && port != HTTP_PORT);
        boolean https = (scheme.equals(HTTPS) && port != HTTPS_PORT);
        if (http || https) {
            url.append(":");
            url.append(port);
        }
        url.append(getRequestURI());
        return url;
    }

    @Override
    public String getServletPath() {
        return uri;
    }

}

