package com.luzhi.miniTomcat.classloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/5/21
 * 生成公共类的加载器,加载.{@code System.getProperty("user.dir")}/lib 的jar文件.
 * 有关tomcat的类的加载机制:(主要的三层结构)
 * 《1》: Common Class Loader (公共类加载器)
 * 负责加载%tomcat_home%/lib 下的所有jar和类
 * 《2》: WebAppClassLoader (web应用类的加载器)
 * 负责加载某个Web应用,例如j2ee下的WEb-INF/Classes/,或者WEB-INF/lib/下的类和jar.
 * 《3》: JspClassLoader (Jsp 类加载器)
 * 负责加载jsp转化为java文件编译.class文件.
 * <br/>
 * 备注:在miniTomcat中%tomcat_home%/catalina/ 和 %tomcat_home%/shared/ 可有可无.
 * tomcat打破双亲委派机制--但如果存在自定义恶意HashMap不会存在影响,只是自定义的类加载器不同,顶层还是一样的.
 * <p>
 * BootStrap Class Loader -> Extension Class Loader -> AppClassLoader -> Common Class Loader -> <one>catalina Class Loader
 * -> <two>shared class loader -> WebClassLoader -> JspClassLoader
 * tomcat为了分隔性,打破双亲委派机制.(除了顶层BooStrap Class Loader 以外,其他类的加载器都应该由父类的加载器进行加载.) 以便于每个web加载器互不干扰.
 */
public class CommonClassLoader extends URLClassLoader {

    public CommonClassLoader() {

        super(new URL[]{});
        try {
            File workFolder = new File(System.getProperty("user.dir"));
            File libCatalog = new File(workFolder, "lib");
            // 读取lib目录下的所有文件.备注:如果lib不是目录或出现 I/O异常则返回null.
            File[] jarLibFile = libCatalog.listFiles();
            // 设置断言,设置lib下的文件不为空.
            assert jarLibFile != null;
            for (File file : jarLibFile) {
                // 指定后缀为jar
                if (file.getName().endsWith("jar")) {
                    URL url = new URL("file:" + file.getAbsolutePath());
                    this.addURL(url);
                }
            }

        } catch (MalformedURLException exception) {
            System.out.println("打印异常原因:" + exception.getMessage());
            exception.printStackTrace();
        }
    }
}
