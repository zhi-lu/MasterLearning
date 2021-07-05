package com.luzhi.miniTomcat.http;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author apple
 * @version jdk1.8
 * // TODO:2021/5/20
 * 生成miniTomcat的Response,同理Response需要实现{@link HttpServletResponse}
 */
public class Response extends BaseResponse {
    /**
     * @see #stringWriter
     * 字符流将其输出收集到缓存字符串中,将输出的头信息写入缓存字符串中.
     */
    private final StringWriter stringWriter;

    /**
     * @see #writer
     * 因为浏览器返回的是字符流,所以在Java中不使用{@link java.io.PrintStream},
     * 而使用 {@link PrintWriter},把数据字符流写入 stringWrite中
     */
    private final PrintWriter writer;

    /**
     * @see #contentType
     * 保存浏览器的头信息.文本类型为 "text/html".
     */
    private String contentType;

    /**
     * @see #redirectPath
     * 重定向路径.
     */
    private String redirectPath;

    /**
     * @see #bytes
     * 保存二进制文件
     */
    private byte[] bytes;

    /**
     * @see #status
     * 设置状态值.
     */
    private int status;

    /**
     * @see #cookies
     * 声明一个Cookie列表
     */
    private final List<Cookie> cookies;

    /**
     * @see #Response()
     * 初始化构造Response()方法对获取对回复的数据进行进行操作.
     */
    public Response() {
        this.stringWriter = new StringWriter();
        this.cookies = new ArrayList<>();
        this.writer = new PrintWriter(stringWriter);
        this.contentType = "text/html";
    }

    @Override
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    /**
     * @see #getBody()
     * 获取当前stringWrite对象,对缓存的字节流,转化为字节数组.
     */
    public byte[] getBody() {
        if (null == bytes) {
            String content = stringWriter.toString();
            bytes = content.getBytes(StandardCharsets.UTF_8);
        }
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getRedirectPath() {
        return redirectPath;
    }

    @Override
    public PrintWriter getWriter() {
        return writer;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public void sendRedirect(String redirect){
        this.redirectPath = redirect;
    }

    @Override
    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public String getCookiesHeader() {
        String pattern = "EEE, d MMM yyyy HH:mm:ss 'GMT'";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.ENGLISH);
        StringBuilder stringBuffer = new StringBuilder();
        for (Cookie cookie : getCookies()) {
            stringBuffer.append("\r\n");
            stringBuffer.append("Set-Cookie:");
            stringBuffer.append(cookie.getName()).append("=").append(cookie.getValue()).append(";");
            // cookie.getMaxAge()为-1意味着Cookie中Expires is forever 直到浏览器关闭.
            if (-1 != cookie.getMaxAge()) {
                stringBuffer.append("Expires=");
                Date date = new Date();
                Date expire = DateUtil.offset(date, DateField.SECOND, cookie.getMaxAge());
                stringBuffer.append(simpleDateFormat.format(expire)).append(";");
            }
            if (null != cookie.getPath()) {
                stringBuffer.append("Path=").append(cookie.getPath());
            }
        }
        return stringBuffer.toString();
    }
}
