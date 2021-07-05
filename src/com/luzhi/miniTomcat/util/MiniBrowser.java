package com.luzhi.miniTomcat.util;


import cn.hutool.http.HttpUtil;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author apple
 * @version jdk1.8
 * // TODO :2021/5/14
 * 创建一个mini浏览器,在该类中getContentBytes()是非常关键的
 */
@SuppressWarnings("unused")
public abstract class MiniBrowser {

    public static void main(String[] args) {

        //noinspection HttpUrlsUsage
        String url = "http://static.how2j.cn/diytomcat.html";
        String contentString = getContentString(url, false);
        System.out.println(contentString);
        System.out.println("<==================================================>");
        String httpString = getHttpString(url, false);
        System.out.println(httpString);
    }
    //<=========================================================================================>//
    //<=========================================================================================>//

    /**
     * 解析请求详情请看{@link #getContentBytes(String, boolean, Map, boolean)}操作
     *
     * @param url 该重构方法通过url解析请求
     * @see #getContentBytes(String)
     */
    public static byte[] getContentBytes(String url) {
        return getContentBytes(url, false);
    }

    /**
     * 解析请求详情请看{@link #getContentBytes(String, boolean, Map, boolean)}操作
     *
     * @param url  请求解析的url
     * @param gzip 是否进行压缩
     * @see #getContentBytes(String, boolean)
     */
    public static byte[] getContentBytes(String url, boolean gzip) {
        return getContentBytes(url, gzip, null, true);
    }

    /**
     * 解析请求详情请看{@link #getContentBytes(String, boolean, Map, boolean)}操作<p>(默认不进行压缩)</p>
     *
     * @param url    需要请求解析的url
     * @param params 解析体
     * @param isGet  是否是GET还POST请求.
     * @see #getContentBytes(String, Map, boolean)
     */
    public static byte[] getContentBytes(String url, Map<String, Object> params, boolean isGet) {
        return getContentBytes(url, false, params, isGet);
    }
    //<=========================================================================================>//
    //<=========================================================================================>//

    /**
     * 详情操作请看{@link #getContentString(String, boolean, Map, boolean)} 方法
     *
     * @param url 需要请求解析的url
     * @see #getContentString(String)
     */
    public static String getContentString(String url) {
        return getContentString(url, false);
    }

    /**
     * 详情操作请看{@link #getContentString(String, boolean, Map, boolean)} 方法
     *
     * @param url  请求解析的url
     * @param gzip 是否进行压缩
     * @see #getContentString(String, boolean)
     */
    public static String getContentString(String url, boolean gzip) {
        return getContentString(url, gzip, null, true);
    }

    /**
     * 详情操作请看{@link #getContentString(String, boolean, Map, boolean)} 方法<p>(默认不进行压缩)</p>
     *
     * @param url    需要请求解析的url
     * @param params 解析体
     * @param isGet  是否是GET还POST请求.
     * @see #getContentString(String, Map, boolean)
     */
    public static String getContentString(String url, Map<String, Object> params, boolean isGet) {
        return getContentString(url, false, params, isGet);
    }

    /**
     * 通过调用方法 {@link MiniBrowser#getContentBytes(String, boolean, Map, boolean)} 生成byte型数组
     * 如果返回的方法{{@link #getContentBytes(String, boolean, Map, boolean)}}数据为空 则在调用的主方法为获取的值也为null;
     *
     * @param url    需要请求解析的url
     * @param gzip   是否要进行压缩
     * @param params 解析体
     * @param isGet  是否是GET还POST请求.
     * @see #getContentString(String, boolean, Map, boolean)
     */
    public static @Nullable String getContentString(String url, boolean gzip, Map<String, Object> params, boolean isGet) {
        byte[] result = getContentBytes(url, gzip, params, isGet);
        if (null == result) {
            return null;
        }
        // 自动抛出错误在调用编码格式为StandardCharsets.UTF_8 在实现的抽象类中已经抛出该异常
        // 为 UnsupportedCharsetException(不支持该编码格式)
        return new String(result, StandardCharsets.UTF_8).trim();
    }
    //<=========================================================================================>//
    //<=========================================================================================>//

    /**
     * 详细操作请看该方法结构体的注解.
     *
     * @param url    需要请求解析的url
     * @param gzip   是否需要进行压缩
     * @param params 解析体
     * @param isGet  是否是GET还POST请求.
     * @see #getContentBytes(String, boolean, Map, boolean)
     */
    public static byte[] getContentBytes(String url, boolean gzip, Map<String, Object> params, boolean isGet) {
        // 创建一个通过调用getHttpBytes(url,gzip,params,isGet)返回的数组
        byte[] response = getHttpBytes(url, gzip, params, isGet);
        // 将 "\r\n\r\n" 生成一个字符数组.
        byte[] doubleReturn = "\r\n\r\n".getBytes(StandardCharsets.UTF_8);
        // 作为一个记录值
        int pos = -1;
        for (int i = 0; i < response.length - doubleReturn.length; i++) {
            byte[] temp = Arrays.copyOfRange(response, i, i + doubleReturn.length);
            if (Arrays.equals(temp, doubleReturn)) {
                pos = i;
                break;
            }
        }
        // 如果记录值pos不变则方法 getHttpBytes()返回的数据为空,则返回null
        if (-1 == pos) {
            return null;
        }
        // 加上了doubleReturn的长度,获取内容的真实长度
        pos += doubleReturn.length;
        // 返回已经复制过后的byte[]数组.从长度为to - from
        return Arrays.copyOfRange(response, pos, response.length);
    }

    public static String getHttpString(String url) {
        return getHttpString(url, false);
    }

    public static String getHttpString(String url, boolean gzip) {
        return getHttpString(url, gzip, null, true);
    }

    public static String getHttpString(String url, Map<String, Object> params, boolean isGet) {
        return getHttpString(url, false, params, isGet);
    }

    public static String getHttpString(String url, boolean gzip, Map<String, Object> params, boolean isGet) {
        byte[] result = getHttpBytes(url, gzip, params, isGet);
        // 返回删除左右空格的bytes内容转化为String()类对象
        return new String(result).trim();
    }

    public static byte[] getHttpBytes(String url, boolean gzip, Map<String, Object> params, boolean isGet) {
        // 获取当前请求的方法(GET/POST)
        String method = isGet ? "GET" : "POST";
        // 定义一个null的byte数组
        byte[] result;
        try {
            // 通过url创建一个 URL对象,直接获取它的地址和相关的端口.
            URL u = new URL(url);
            // 创建一个socket连接此套接字对象指向本地计算机
            Socket client = new Socket();
            // 获取端口该URL端口
            int port = u.getPort();
            // 如果端口没有设置,则端口号默认为-1,则重新设置端口号为80,web的端口一般为80.
            if (-1 == port) {
                port = 80;
            }
            // 通过主机和端口设置一个IP套接字连接,和URL进行连接
            InetSocketAddress inetSocketAddress = new InetSocketAddress(u.getHost(), port);
            // 本地主机和通过Ip套接字和URL进行连接,连接超时1s.
            client.connect(inetSocketAddress, 1000);
            // 使用Map<String,String> 创建一个HashMap<>对象,用来保存请求"头"和请求内容(进行初始化容积为128)
            Map<String, String> requestHeaders = new HashMap<>(128);
            // 存放内容
            requestHeaders.put("Host", u.getHost() + ":" + port);
            requestHeaders.put("Accept", "text/html");
            requestHeaders.put("Connection", "close");
            requestHeaders.put("User-Agent", " mini browser / java1.8");
            // 是否支持压缩
            if (gzip) {
                requestHeaders.put("Accept_Encoding", "gzip");
            }
            // 获取当前url的路径部分
            String path = u.getPath();
            // 如果url的路径部分不存在则设置 path="/"
            if (0 == path.length()) {
                path = "/";
            }
            // 对请求体存在而请求方法为"GET"的处理
            if (null != params && isGet) {
                String paramsString = HttpUtil.toParams(params);
                path = path + "?" + paramsString;
            }
            // 创建一个请求头加上HTTP协议,请求方法(GET/POST)和HTTP/1.1字符串的空格
            String firstLine = method + " " + path + " HTTP/1.1\r\n";
            // 创建Sting缓存区 用来存放拼接过后的请求头和相关的内容
            //noinspection MismatchedQueryAndUpdateOfStringBuilder
            StringBuffer httpRequestString = new StringBuffer();
            httpRequestString.append(firstLine);
            // 创建一个集合,存放相关的浏览器的请求"头"和相关内容
            Set<String> headers = requestHeaders.keySet();
            for (String header : headers) {
                // 进行请求头和相关的内容拼接
                String headMessage = header + ":" + requestHeaders.get(header) + "\r\n";
                httpRequestString.append(headMessage);
            }
            // 对POST请求的处理.
            if (null != params && !isGet) {
                String paramsString = HttpUtil.toParams(params);
                httpRequestString.append("\r\n");
                httpRequestString.append(paramsString);
            }
            // 通过输出类将拼接好的数据交给本地设备再由本地设备输出给URL。
            // 实现自动flush()操作. flush()强制把缓存区的数据读出来
            PrintWriter printWriter = new PrintWriter(client.getOutputStream(), true);
            printWriter.println(httpRequestString);
            // 获取客户端输入流,这时候客户端已经获取到服务端的请求返回的信息
            InputStream inputStream = client.getInputStream();
            // 对文件进行读取成字节流.
            result = readBytes(inputStream, true);
            // 关闭客户端.
            client.close();
        } catch (Exception exception) {
            System.out.println("打印异常原因:" + exception.getMessage());
            // 默认使用{@link StandardCharsets.UTF_8}编码方式已经抛出UnsupportedCharsetException(charsetName)
            result = exception.toString().getBytes(StandardCharsets.UTF_8);
        }
        return result;
    }

    public static byte[] readBytes(InputStream inputStream, boolean fully) throws IOException {

        final int byteSize = 1024;
        // 创建一个字符数据输出流,把从服务端获取的内容写入.
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bytes = new byte[byteSize];
        while (true) {
            // 进行读取,读取到定义的bytes中
            int length = inputStream.read(bytes);
            // 如果读取完成则length为-1,则跳出缓存循环.
            if (-1 == length) {
                break;
            }
            // 将bytes内容读取到输出流中
            byteArrayOutputStream.write(bytes, 0, length);
            // 如果读取的长度不足定义的 byteSize(1024),已经是进行最后一次读取.跳出缓存循环中.
            // 添加一个boolean值,如果读取的内容可能出现 <1024(文件此时还没有读完) 还是强制读取.直到上述代码 (-1 == length)为true从对象文件读取不到数据为止。
            if (!fully && length != byteSize) {
                break;
            }
        }
        // 将输出流读取的在缓存区的内容转化为字符数据.
        return byteArrayOutputStream.toByteArray();
    }
}
