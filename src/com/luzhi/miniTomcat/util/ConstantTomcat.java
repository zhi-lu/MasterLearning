package com.luzhi.miniTomcat.util;

import cn.hutool.system.SystemUtil;

import java.io.File;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/5/11
 * 该抽象类对相关"头"信息进行定义和相关的资源地址等信息.
 * 参考使用{@link SystemUtil#get(String)} 例如通过系统属性{@code SystemUtil.get("user.dir")} 获取项目主目录 MasterLearning目录
 * 或者使用{@code System.getProperty("user.dir")}来获取
 */
@SuppressWarnings("unused")
public abstract class ConstantTomcat {

    /**
     * @see #CODE_200
     * <p>
     * 活动码为200
     * 静态int值为了处理在Servlet中的资源而产生的相关活动码.
     */
    public static final int CODE_200 = 200;

    /**
     * @see #CODE_302
     * <p>
     * 活动码为302
     */
    public static final int CODE_302 = 302;

    /**
     * @see #CODE_404
     * <p>
     * 活动码为404
     */
    public static final int CODE_404 = 404;

    /**
     * @see #CODE_500
     * <p>
     * 活动码为500
     */
    public static final int CODE_500 = 500;

    /**
     * @see #RESPONSE_HEAD_202
     * 存放http请求成功返回的头信息.
     */
    public static final String RESPONSE_HEAD_202 = "HTTP/1.1 200 OK \r\n" +
            "Content-Type:{}{};charset=UTF-8 \r\n\r\n";

    /**
     * @see #RESPONSE_HEAD_202_GZIP
     * 存放http请求成功返回的头信息并且文件支持压缩.
     */
    public static final String RESPONSE_HEAD_202_GZIP = "HTTP/1.1 200 OK \r\nContent-Type: {}{}\r\n" +
            "Content-Encoding:gzip" +
            "\r\n\r\n";

    /**
     * @see #RESPONSE_HEAD_302
     * 存放http跳转的头信息(301永久跳转,302临时跳转.对于用户来说并不区别,只和搜索引擎处理有关.这里都当作302处理.)
     */
    public static final String RESPONSE_HEAD_302 = "HTTP/1.1 302 FOUND \r\nLocation:{}\r\b\r\n";

    /**
     * @see #RESPONSE_HEAD_404
     * 存放http请求的资源不存在的情况.
     */
    public static final String RESPONSE_HEAD_404 = "HTTP/1.1 404 NOT FOUND \r\n" +
            "Content-Type:text/html;charset=UTF-8 \r\n\r\n";

    /**
     * @see #RESPONSE_HEAD_500
     * 访问服务器出现问题的情况
     */
    public static final String RESPONSE_HEAD_500 = "HTTP/1.1 500 Internal Server Error \r\n" +
            "Content-Type:text/html;charset=UTF-8 \r\n\r\n";

    /**
     * @see #WEBAPPS_FOLDER
     * 获取当前目录下的webapps文件夹
     */
    public static final File WEBAPPS_FOLDER = new File(SystemUtil.get("user.dir"), "webapps");

    /**
     * @see #ROOT_FOLDER
     * 获取webapps下的ROOT文件
     */
    public static final File ROOT_FOLDER = new File(WEBAPPS_FOLDER, "ROOT");
    /**
     * @see #RESOURCE_FOLDER
     * 获取文件夹resource 地址.
     */
    public static final File RESOURCE_FOLDER = new File(SystemUtil.get("user.dir"), "conf");

    /**
     * @see #SERVER_XML
     * 获取resource 文件夹地址下的server.xml文件.
     */
    public static final File SERVER_XML = new File(RESOURCE_FOLDER, "server.xml");

    /**
     * @see #WORK_FOLDER
     * 将.jsp转译成.java文件放在%TOMCAT_HOME%/work下.
     */
    public static final String WORK_FOLDER = SystemUtil.get("user.dir") + File.separator + "work";

    /**
     * @see #SERVER_WEB_XML
     * 获取resource 文件夹地址下的web.xml文件.
     */
    public static final File SERVER_WEB_XML = new File(RESOURCE_FOLDER, "web.xml");

    /**
     * @see #CONTEXT_XML
     * 获取Context.xml对servlet进行配置.
     */
    public static final File CONTEXT_XML = new File(RESOURCE_FOLDER, "context.xml");

    /**
     * @see #FORMAT_STRING_404
     * 资源访问不存在的404界面
     */
    public static final String FORMAT_STRING_404 = "<html><head><title>Mini Tomcat/1.0.1 - Error report</title><style>" +
            "<!--H1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:lightPink;font-size:22px;} " +
            "H2 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:lightPink;font-size:16px;} " +
            "H3 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:lightPink;font-size:14px;} " +
            "BODY {font-family:Tahoma,Arial,sans-serif;color:black;background-color:white;} " +
            "B {font-family:Tahoma,Arial,sans-serif;color:white;background-color:lightPink;} " +
            "P {font-family:Tahoma,Arial,sans-serif;background:white;color:black;font-size:12px;}" +
            "A {color : black;}A.name {color : black;}HR {color : lightPink;}--></style> " +
            "</head><body><h1>HTTP Status 404 - {}</h1>" +
            "<HR size='1' noshade='noshade'><p><b>type</b> Status report</p><p><b>message</b> <u>{}</u></p><p><b>description</b> " +
            "<u>The requested resource is not available.</u></p><HR size='1' noshade='noshade'><h3>miniTomcat 1.0.1</h3>" +
            "</body></html>";
    /**
     * @see #FORMAT_STRING_500
     * 由服务器引发的错误的处理界面。
     */
    public static final String FORMAT_STRING_500 = "<html><head><title>mini Tomcat/1.0.1 - Error report</title><style>"
            + "<!--H1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:lightPink;font-size:22px;} "
            + "H2 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:lightPink;font-size:16px;} "
            + "H3 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:lightPink;font-size:14px;} "
            + "BODY {font-family:Tahoma,Arial,sans-serif;color:black;background-color:white;} "
            + "B {font-family:Tahoma,Arial,sans-serif;color:white;background-color:lightPink;} "
            + "P {font-family:Tahoma,Arial,sans-serif;background:white;color:black;font-size:12px;}"
            + "A {color : black;}A.name {color : black;}HR {color : lightPink;}--></style> "
            + "</head><body><h1>HTTP Status 500 - An exception occurred processing {}</h1>"
            + "<HR size='1' noshade='noshade'><p><b>type</b> Exception report</p><p><b>message</b> <u>An exception occurred processing {}</u></p><p><b>description</b> "
            + "<u>The server encountered an internal error that prevented it from fulfilling this request.</u></p>"
            + "<p>Stacktrace:</p>" + "<pre>{}</pre>" + "<HR size='1' noshade='noshade'><h3>miniTomcat 1.0.1</h3>"
            + "</body></html>";
}
