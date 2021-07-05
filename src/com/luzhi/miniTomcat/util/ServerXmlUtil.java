package com.luzhi.miniTomcat.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import com.luzhi.miniTomcat.catalina.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/5/21
 * 生成对xml进行解析对类.
 */
public abstract class ServerXmlUtil {

    /**
     * @see #contextList(Host)
     * 该方法对象xml的标签Context进行解析.
     */
    public static List<Context> contextList(Host host) {
        List<Context> contexts = new ArrayList<>();
        // 将xml文件以字符串的形式进行读取.
        String xml = FileUtil.readUtf8String(ConstantTomcat.SERVER_XML);
        // 将生成的xml字符串有Jsoup解析成Document形式
        Document document = Jsoup.parse(xml);
        // 对挑选标签进行解析成Element列表.
        Elements elements = document.select("Context");
        for (Element element : elements) {
            // 获取属性key对应的value
            String path = element.attr("path");
            String docBase = element.attr("docBase");
            // 通过hutool将元素属性转化为bool值.
            boolean reloadable = Convert.toBool(element.attr("reloadable"), true);
            // 根据path,docBase 生成 一个Context对象
            Context context = new Context(path, docBase, host, reloadable);
            contexts.add(context);
        }
        return contexts;
    }

    /**
     * @param service 获取Service对象。用来获取Connector标签对象.
     * @see #connectorList(Service)
     */
    public static List<Connector> connectorList(Service service) {
        List<Connector> connectorList = new ArrayList<>();
        // 解析xml
        String xml = FileUtil.readUtf8String(ConstantTomcat.SERVER_XML);
        // 将解析对字符串生成文档形式
        Document document = Jsoup.parse(xml);
        // 对元素Connector进行解析.
        Elements elements = document.select("Connector");
        for (Element element : elements) {
            int port = Convert.toInt(element.attr("port"));
            String compression = element.attr("compression");
            int compressionMinSize = Convert.toInt(element.attr("compressionMinSize"), 0);
            String onCompressionUserAgents = element.attr("onCompressionUserAgents");
            String compressibleMimeType = element.attr("compressibleMimeType");
            Connector connector = new Connector(service);
            connector.setPort(port);
            connector.setCompression(compression);
            connector.setCompressionMinSize(compressionMinSize);
            connector.setOnCompressionUserAgents(onCompressionUserAgents);
            connector.setCompressibleMimeType(compressibleMimeType);
            connectorList.add(connector);
        }
        return connectorList;
    }

    /**
     * @param engine 通过engine(servlet),获取标签下的所有Host
     * @see #getEngineName()
     */
    public static List<Host> getHosts(Engine engine) {
        List<Host> result = new ArrayList<>();
        String xml = FileUtil.readUtf8String(ConstantTomcat.SERVER_XML);
        Document document = Jsoup.parse(xml);
        Elements elements = document.select("Host");
        for (Element element : elements) {
            String name = element.attr("name");
            Host host = new Host(name, engine);
            result.add(host);
        }
        return result;
    }

    /**
     * @see #getHostName()
     * @deprecated 弃用对xml进行解析获取主机名.
     */
    public static String getHostName() {
        String xml = FileUtil.readUtf8String(ConstantTomcat.SERVER_XML);
        Document document = Jsoup.parse(xml);
        Element host = document.select("Host").first();
        return host.attr("name");
    }

    /**
     * @see #getEngineName()
     * 对xml进行的标签Engine标签进行解析.
     */
    public static String getEngineName() {
        String xml = FileUtil.readUtf8String(ConstantTomcat.SERVER_XML);
        Document document = Jsoup.parse(xml);
        Element engine = document.select("Engine").first();
        return engine.attr("defaultHost");
    }

    /**
     * @see #getServiceName()
     * 对xml进行的标签Service标签进行解析.
     */
    public static String getServiceName() {
        String xml = FileUtil.readUtf8String(ConstantTomcat.SERVER_XML);
        Document document = Jsoup.parse(xml);
        Element service = document.select("Service").first();
        return service.attr("name");
    }
}
