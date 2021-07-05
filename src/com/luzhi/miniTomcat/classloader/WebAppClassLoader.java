package com.luzhi.miniTomcat.classloader;

import cn.hutool.core.io.FileUtil;
import com.luzhi.miniTomcat.catalina.Context;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/5/6
 * 生成web应用的类的加载器.
 */
public class WebAppClassLoader extends URLClassLoader {
    /**
     * 该构成方法创建一个Web应用的类加载器.
     *
     * @param docBase           扫描{@link Context#getDocBase()}获取classes和jar资源
     * @param commonClassLoader 获取父类加载器
     * @see #WebAppClassLoader(String, ClassLoader)
     */
    public WebAppClassLoader(String docBase, ClassLoader commonClassLoader) {
        super(new URL[]{}, commonClassLoader);

        try {
            // 扫描路径
            File webInfFolder = new File(docBase, "WEB-INF");
            File classesFolder = new File(webInfFolder, "classes");
            File jarFolder = new File(webInfFolder, "lib");
            URL url;
            // 将classes目录扫入URL中添加后缀"/",如此这样才会把WEB-INF下的classes当做目录来处理.不然的话会将资源当作(.jar)文件进行处理
            url = new URL("file:" + classesFolder.getAbsolutePath() + "/");
            this.addURL(url);
            // 遍历WEB-INF下的lib中的(.jar)文件.{@code FileUtil.loopFiles(jarFolder)}将结果打包成一个List
            List<File> jarLists = FileUtil.loopFiles(jarFolder);
            for (File file : jarLists) {
                url = new URL("file:" + file.getAbsolutePath());
                this.addURL(url);
            }
        } catch (Exception exception) {
            System.out.println("打印异常原因:" + exception.getMessage());
            exception.printStackTrace();
        }
    }

    /**
     * 关闭该加载器.
     *
     * @see #stop()
     */
    public void stop() {
        try {
            close();
        } catch (IOException exception) {
            System.out.println("打印异常原因:" + exception.getMessage());
            exception.printStackTrace();
        }
    }
}
