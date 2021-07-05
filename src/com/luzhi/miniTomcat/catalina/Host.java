package com.luzhi.miniTomcat.catalina;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import com.luzhi.miniTomcat.util.ConstantTomcat;
import com.luzhi.miniTomcat.util.ServerXmlUtil;
import com.luzhi.miniTomcat.watcher.WarFileWatcher;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/5/21
 * 对miniTomcat的内置对象的Host进行处理
 */
@SuppressWarnings("unused")
public class Host {

    private String name;
    private final Engine engine;
    private static final String ROOT = "ROOT";
    private static Map<String, Context> contextMap;

    public Host(String name, Engine engine) {
        this.engine = engine;
        this.name = name;
        contextMap = new LinkedHashMap<>(128);

        scanContextsOnServerXml();
        scanContextsOnWebAppsFolder();
        LogFactory.get().info("<====================== HOST ======================>");
        scanWarOnWebAppsFolder();
        LogFactory.get().info("<====================== HOST END ===================>");
        new WarFileWatcher(this).start();
    }

    /**
     * 遍历webapps文件夹下的文件夹和文件.如果是文件的话直接跳过,
     * 如果是文件夹交由{@link #loadContext(File)}方法进行处理.
     *
     * @see #scanContextsOnWebAppsFolder()
     */
    private void scanContextsOnWebAppsFolder() {
        File[] folders = ConstantTomcat.WEBAPPS_FOLDER.listFiles();
        // 设置断言因为在File类中listFiles()如果被遍历的文件夹下如果为空则该方法返回null.
        assert folders != null;
        for (File folder : folders) {
            if (!folder.isDirectory()) {
                continue;
            }
            loadContext(folder);
        }
    }

    /**
     * 加载{@link Context}对象
     *
     * @param folder 获取需要处理的文件夹.
     * @see #loadContext(File)
     */
    private void loadContext(File folder) {
        String path = folder.getName();
        if (ROOT.equals(path)) {
            path = "/";
        } else {
            path = "/" + path;
        }
        String docBase = folder.getAbsolutePath();
        Context context = new Context(path, docBase, this, true);
        // 将获取web下的资源路径和初始化的context对象写入Map中.
        contextMap.put(context.getPath(), context);
    }

    /**
     * 把文件加加载为{@link Context} 对象;
     *
     * @param folder 需要处理的文件
     * @see #load(File)
     */
    public void load(File folder) {
        String path = folder.getName();
        if (ROOT.equals(path)) {
            path = "/";
        } else {
            path = "/" + path;
        }
        String docBase = folder.getAbsolutePath();
        Context context = new Context(path, docBase, this, false);
        contextMap.put(context.getPath(), context);
    }

    /**
     * 把war文件进行解析为目录,并把文件加载为相关的Context
     *
     * @param warFile 获取相关的war文件
     * @see #loadWar(File)
     */
    public void loadWar(File warFile) {
        String fileName = warFile.getName();
        String folderName = StrUtil.subBefore(fileName, ".", true);
        // 是否存在相应的Context对象
        Context context = getContext("/" + folderName);
        if (null != context) {
            return;
        }
        // 是否存在相应的文件夹对象.
        File folder = new File(ConstantTomcat.WEBAPPS_FOLDER, folderName);
        if (folder.exists()) {
            return;
        }
        // 移动war文件,因为jar命令只支持解压当前目录下
        File tempWarFile = FileUtil.file(ConstantTomcat.WEBAPPS_FOLDER, folderName, fileName);
        File contextFolder = tempWarFile.getParentFile();
        //noinspection ResultOfMethodCallIgnored
        contextFolder.mkdir();
        FileUtil.copyFile(warFile, tempWarFile);
        // 进行解压
        String command = "jar xvf " + fileName;
        System.out.println(command);
        Process process = RuntimeUtil.exec(null, contextFolder, command);
        try {
            process.waitFor();
        } catch (InterruptedException exception) {
            System.out.println("打印异常原因:" + exception.getMessage());
        }
        // 解压后删除文件对象
        //noinspection ResultOfMethodCallIgnored
        tempWarFile.delete();
        // 创建新的Context;
        load(contextFolder);
    }

    /**
     * 多应用配置.将{@link ServerXmlUtil#contextList(Host)}解析的{@link Context}进行遍历
     * 和保存的ContextMap中.
     *
     * @see #scanContextsOnServerXml()
     */
    private void scanContextsOnServerXml() {
        List<Context> contexts = ServerXmlUtil.contextList(this);
        for (Context context : contexts) {
            contextMap.put(context.getPath(), context);
        }
    }

    /**
     * 扫描webapps下的文件,处理.war文件
     *
     * @see #scanWarOnWebAppsFolder()
     */
    private void scanWarOnWebAppsFolder() {
        File folder = FileUtil.file(ConstantTomcat.WEBAPPS_FOLDER);
        File[] files = folder.listFiles();
        assert files != null;
        LogFactory.get().info("<HELLO>" + Arrays.stream(files).count());
        for (File file : files) {
            if (file.getName().toLowerCase().endsWith(".war")) {
                LogFactory.get().info("文件名:" + file.getName());
                loadWar(file);
            }
        }
    }

    /**
     * 具体重载操作.
     *
     * @param context 获取子类对象{@code Context.this}
     * @see #reload(Context)
     */
    public void reload(Context context) {
        LogFactory.get().info("Reloading context with name [{}] has started", context.getPath());
        String path = context.getPath();
        String docBase = context.getDocBase();
        boolean reloadable = context.isReloadable();
        // stop
        context.stop();
        // remove
        contextMap.remove(path);
        // 重新设置对应关系.
        Context newContext = new Context(path, docBase, this, reloadable);
        contextMap.put(newContext.getPath(), newContext);
        LogFactory.get().info("Reloading Context with name [{}] has completed", context.getPath());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Context getContext(String path) {
        return contextMap.get(path);
    }

    public Engine getEngine() {
        return engine;
    }
}
