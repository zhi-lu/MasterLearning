package com.luzhi.miniTomcat.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.log.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/5/27
 * 对context.xml文件进行解析,配置servlet.
 */
public abstract class ContextXmlUtil {

    public static String getWatchedResource() {
        try {
            String xml = FileUtil.readUtf8String(ConstantTomcat.CONTEXT_XML);
            Document document = Jsoup.parse(xml);
            Element element = document.select("WatchedResource").first();
            return element.text();
        } catch (Exception exception) {
            System.out.println("打印异常原因" + exception.getMessage());
            LogFactory.get().error(exception);
            return "WEB-INF/web.xml";
        }
    }
}
