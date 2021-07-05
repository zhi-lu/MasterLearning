package com.luzhi.miniTomcat.catalina;

import cn.hutool.log.LogFactory;
import com.luzhi.miniTomcat.http.Request;
import com.luzhi.miniTomcat.http.Response;
import com.luzhi.miniTomcat.util.ThreadPoolUtil;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/5/21
 * 生成对server.xml"中连接器解析".
 */
@SuppressWarnings("unused")
public class Connector implements Runnable {

    /**
     * 服务端口
     *
     * @see #port
     */
    private int port;

    /**
     * 获取Service对象.
     *
     * @see #service
     */
    private final Service service;

    /**
     * 创建compression属性值(是否进行压缩)
     *
     * @see #compression
     */
    private String compression;

    /**
     * 创建compressionMinSize(超过多少字节数进行压缩)
     *
     * @see #compressionMinSize
     */
    private int compressionMinSize;

    /**
     * 创建onCompressionUserAgents(那些浏览器不需要压缩)
     *
     * @see #onCompressionUserAgents
     */
    private String onCompressionUserAgents;

    /**
     * 创建compressionMimeType(那些文件需要进行压缩.)
     *
     * @see #compressibleMimeType
     */
    private String compressibleMimeType;

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            //noinspection InfiniteLoopStatement
            while (true) {
                // 收到一个浏览器服务器的请求.
                Socket socket = serverSocket.accept();
                Runnable runnable = () -> {
                    try {
                        Request request = new Request(socket, Connector.this);
                        System.out.println("浏览器读取的信息为: \r\n" + request.getRequestString());
                        System.out.println("Uri信息为: \r\n" + request.getUri());
                        Response response = new Response();
                        HttpProcessor processor = new HttpProcessor();
                        processor.execute(socket, request, response);
                    } catch (IOException exception) {
                        // 日志打印输出异常.
                        LogFactory.get().error(exception);
                    }
                };
                // 把当前定义的 runnable 丢入定义的线程池中.
                ThreadPoolUtil.run(runnable);
            }
        } catch (Exception exception) {
            LogFactory.get().error(exception);
            System.out.println("异常的原因:" + exception.getMessage());
        }

    }

    public void init() {
        LogFactory.get().info("Init ProtocolHandler [http-bio-{}]", port);
    }

    @SuppressWarnings("AlibabaAvoidManuallyCreateThread")
    public void start() {
        LogFactory.get().info("Start ProtocolHandler [http-bio-{}]", port);
        new Thread(this).start();
    }

    public Connector(Service service) {
        this.service = service;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Service getService() {
        return service;
    }

    public int getCompressionMinSize() {
        return compressionMinSize;
    }

    public int getPort() {
        return port;
    }

    public String getCompressibleMimeType() {
        return compressibleMimeType;
    }

    public String getCompression() {
        return compression;
    }

    public void setCompression(String compression) {
        this.compression = compression;
    }

    public String getOnCompressionUserAgents() {
        return onCompressionUserAgents;
    }

    public void setCompressibleMimeType(String compressibleMimeType) {
        this.compressibleMimeType = compressibleMimeType;
    }

    public void setCompressionMinSize(int compressionMinSize) {
        this.compressionMinSize = compressionMinSize;
    }

    public void setOnCompressionUserAgents(String onCompressionUserAgents) {
        this.onCompressionUserAgents = onCompressionUserAgents;
    }
}
