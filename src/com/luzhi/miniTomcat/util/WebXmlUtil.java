package com.luzhi.miniTomcat.util;

import cn.hutool.core.io.FileUtil;
import com.luzhi.miniTomcat.catalina.Context;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;
import java.io.File;


/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/5/21
 * 生成对conf下的web.xml进行解析.
 */

public abstract class WebXmlUtil {

    /**
     * @see #MIME_MAPPING_TYPE
     * 此Map对象存放Extension(文件扩展名)和Mime-Type(浏览器所识别对类型)。
     */
    private static final Map<String, String> MIME_MAPPING_TYPE = new HashMap<>(1024);

    /**
     * @see #isInitMime
     * 是否进行初始化对于hashMap表来说。
     */
    private static volatile boolean isInitMime = false;

    /**
     * @see #getMimeName(String)
     * 使用 valotile 避免出现两次出现化哈希表{@link #MIME_MAPPING_TYPE};
     * 双重锁机制,保证线程中的数据的可见性和互斥性.
     */
    public static String getMimeName(String extensionName) {
        if (!isInitMime) {
            // 不允许其他线程对该类进行访问.直到在local memory执行完毕,把操作好的数据数据刷入main memory为止.
            synchronized (WebXmlUtil.class) {
                if (!isInitMime) {
                    initMimeMapping();
                    isInitMime = true;
                }
            }
        }
        String mineName = MIME_MAPPING_TYPE.get(extensionName);
        if (null == mineName) {
            return "text/html";
        }
        return mineName;
    }

    /**
     * @see #initMimeMapping()
     * 对{@link #MIME_MAPPING_TYPE} HashMap进行初始化.
     * 填充文件扩展名,和文件mime-type(被浏览器所识别.)
     */
    private static void initMimeMapping() {
        String xml = FileUtil.readUtf8String(ConstantTomcat.SERVER_WEB_XML);
        // 进行解析
        Document document = Jsoup.parse(xml);
        Elements elements = document.select("mime-mapping");
        for (Element element : elements) {
            String extensionName = element.select("extension").text();
            String mimeType = element.select("mime-type").text();
            MIME_MAPPING_TYPE.put(extensionName, mimeType);
        }

    }

    /**
     * @param context 获取一个{@link Context} 对象获取"欢迎文件"的地址
     *                对web.xml进行解析.获取欢迎文件。
     * @see #getWelcomeFile(Context)
     */
    public static String getWelcomeFile(Context context) {
        String webXml = FileUtil.readUtf8String(ConstantTomcat.SERVER_WEB_XML);
        // 进行解析
        Document document = Jsoup.parse(webXml);
        // 获取元素标签
        Elements elements = document.select("welcome-file");
        for (Element element : elements) {
            // 对解析的标签获取其中的文本.
            String welcomeFileName = element.text();
            // 根据地址和文件名,获取文件对象.
            File file = new File(context.getDocBase(), welcomeFileName);
            // 是否存在该文件.
            if (file.exists()) {
                return file.getName();
            }
        }
        return "index.html";
    }
}
