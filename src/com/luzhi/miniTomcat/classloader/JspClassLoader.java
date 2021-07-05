package com.luzhi.miniTomcat.classloader;

import cn.hutool.core.util.StrUtil;
import com.luzhi.miniTomcat.catalina.Context;
import com.luzhi.miniTomcat.util.ConstantTomcat;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/6/21
 * Jsp文件类的加载器.
 */
public class JspClassLoader extends URLClassLoader {

    private static final Map<String, JspClassLoader> MAP = new HashMap<>(512);

    /**
     * 默认路径
     *
     * @see #DEFAULT_PATH
     */
    private static final String DEFAULT_PATH = "/";

    /**
     * 移除无效的Jsp类加载器
     *
     * @param uri     资源地址
     * @param context 获取context地址.
     * @see #invalidJspClassLoader(String, Context)
     */
    public static void invalidJspClassLoader(String uri, Context context) {
        String key = context.getPath() + DEFAULT_PATH + uri;
        MAP.remove(key);
    }

    /**
     * 获取jsp类的加载器.
     *
     * @param uri     资源地址
     * @param context 获取context地址.
     * @see #getJspClassLoader(String, Context)
     */
    public static JspClassLoader getJspClassLoader(String uri, Context context) {
        String key = context.getPath() + DEFAULT_PATH + uri;
        JspClassLoader loader = MAP.get(key);
        if (null == loader) {
            loader = new JspClassLoader(context);
            MAP.put(key, loader);
        }
        return loader;
    }

    private JspClassLoader(Context context) {
        super(new URL[]{}, context.getWebAppClassLoader());
        try {
            String path = context.getPath();
            String subFolder;
            if (DEFAULT_PATH.equals(path)) {
                subFolder = "_";
            } else {
                subFolder = StrUtil.subAfter(path, DEFAULT_PATH, false);
            }
            File classesFolder = new File(ConstantTomcat.WORK_FOLDER, subFolder);
            URL url = new URL("file:" + classesFolder.getAbsolutePath() + DEFAULT_PATH);
            this.addURL(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
