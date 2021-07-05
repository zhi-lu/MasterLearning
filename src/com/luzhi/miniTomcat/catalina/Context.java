package com.luzhi.miniTomcat.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import com.luzhi.miniTomcat.classloader.WebAppClassLoader;
import com.luzhi.miniTomcat.exception.WebConfigDuplicatedException;
import com.luzhi.miniTomcat.http.ApplicationServlet;
import com.luzhi.miniTomcat.http.StandardServletConfig;
import com.luzhi.miniTomcat.util.ContextXmlUtil;
import com.luzhi.miniTomcat.watcher.ContextFileChangeWatcher;
import org.apache.jasper.JspC;
import org.apache.jasper.compiler.JspRuntimeContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.util.*;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/5/21
 * 创建方法文件的地址和在系统文件的位置.
 */
@SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
public class Context {

    /**
     * 匹配"/*
     *
     * @see #START_STAR
     */
    private static final String START_STAR = "/*";

    /**
     * 匹配"/*.jsp"文件
     *
     * @see #START_STAR_DOT
     */
    private static final String START_STAR_DOT = "/*.";

    /**
     * servletEvent初始化方法,对应的比较值
     *
     * @see #INIT_VALUE
     */
    private static final String INIT_VALUE = "init";

    /**
     * servletEvent销毁方法,对应的比较值.
     */
    private static final String DESTROYED_VALUE = "destroyed";
    /**
     * 获取映射的资源文件夹地址
     *
     * @see #path
     */
    private String path;

    /**
     * 获取资源地址的在系统的绝对路径.
     *
     * @see #docBase
     */
    private String docBase;

    /**
     * 创建host对象
     *
     * @see Host
     */
    private final Host host;

    /**
     * 是否进行重载
     *
     * @see #reloadable
     */
    private boolean reloadable;

    /**
     * 获取'*'/WEB-INF/xml文件
     *
     * @see #contextWebXmlFile
     */
    private final File contextWebXmlFile;

    /**
     * 设置自定义的ContextFile的监听器
     *
     * @see #contextFileChangeWatcher
     */
    private ContextFileChangeWatcher contextFileChangeWatcher;

    /**
     * 地址映射servlet类名
     *
     * @see #urlToServletClassName
     */
    private final Map<String, String> urlToServletClassName;

    /**
     * 地址映射servlet名
     *
     * @see #urlToServletName
     */
    private final Map<String, String> urlToServletName;

    /**
     * servlet类名映射servlet名.
     *
     * @see #servletClassNameToServletName
     */
    private final Map<String, String> servletClassNameToServletName;

    /**
     * servlet名映射servlet类名.
     *
     * @see #servletNameToServletClassName
     */
    private final Map<String, String> servletNameToServletClassName;

    /**
     * 存放初始化信息
     *
     * @see #servlet_className_init_params
     */
    private final Map<String, Map<String, String>> servlet_className_init_params;

    /**
     * 创建一个存放servlet对象池.
     *
     * @see #servletPool
     */
    private final Map<Class<?>, HttpServlet> servletPool;

    /**
     * 设置类的加载器为自定义的{@link WebAppClassLoader}加载器
     *
     * @see #webAppClassLoader
     */
    private final WebAppClassLoader webAppClassLoader;

    /**
     * 处理定义的Servlet的上下文.
     *
     * @see #servletContext
     */
    private final ServletContext servletContext;

    /**
     * 定义那些是需要自定义初始化的Servlet在Context启动之后.
     *
     * @see #loadOnStartupServletClassNames
     */
    private final List<String> loadOnStartupServletClassNames;

    /**
     * 创建webContext监听器列表.
     */
    private final List<ServletContextListener> servletContextListenerList;
    /**
     * url对应着筛选器的相应的类名
     *
     * @see #urlToFilterClassName
     */
    private final Map<String, List<String>> urlToFilterClassName;

    /**
     * url对应着筛选器
     *
     * @see #urlToFilterNames
     */
    private final Map<String, List<String>> urlToFilterNames;

    /**
     * 筛选器类对应的筛选器名
     *
     * @see #filterClassNameToFilterName
     */
    private final Map<String, String> filterClassNameToFilterName;

    /**
     * 筛选器名对应的筛选器
     *
     * @see #filterNameToFilterClassName
     */
    private final Map<String, String> filterNameToFilterClassName;

    /**
     * 创建筛选器池.
     *
     * @see #filterPool
     */
    private final Map<String, Filter> filterPool;

    /**
     * 筛选器类对应的相关的初始化的参数.
     *
     * @see #filterClassNameToInitParameters
     */
    private final Map<String, Map<String, String>> filterClassNameToInitParameters;


    /**
     * 详细解释通过{@code Thread.currentThread().getContextClassLoader()}获取{@link com.luzhi.miniTomcat.Bootstrap}
     * 中的{@link com.luzhi.miniTomcat.classloader.CommonClassLoader}公共类加载器.根据tomcat将此公共加载器设置为{@link WebAppClassLoader}
     * 的父类加载器。由它获取类和资源.
     *
     * @see #Context(String, String, Host, boolean)
     */
    public Context(String path, String docBase, Host host, boolean reloadable) {
        this.path = path;
        this.docBase = docBase;
        this.host = host;
        this.reloadable = reloadable;
        this.contextWebXmlFile = new File(docBase, ContextXmlUtil.getWatchedResource());
        this.loadOnStartupServletClassNames = new ArrayList<>();
        this.servletContextListenerList = new ArrayList<>();
        this.urlToServletClassName = new HashMap<>(512);
        this.urlToServletName = new HashMap<>(512);
        this.urlToFilterClassName = new HashMap<>(512);
        this.urlToFilterNames = new HashMap<>(512);
        this.filterPool = new HashMap<>(512);
        this.filterNameToFilterClassName = new HashMap<>(512);
        this.filterClassNameToInitParameters = new HashMap<>(512);
        this.filterClassNameToFilterName = new HashMap<>(512);
        this.servletClassNameToServletName = new HashMap<>(512);
        this.servletNameToServletClassName = new HashMap<>(512);
        this.servletPool = new HashMap<>(512);
        this.servlet_className_init_params = new HashMap<>(512);


        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        this.webAppClassLoader = new WebAppClassLoader(this.docBase, classLoader);
        this.servletContext = new ApplicationServlet(this);
        deploy();
    }

    private void deploy() {

        TimeInterval timeInterval = DateUtil.timer();
        LogFactory.get().info("Deploying web application directory {}", this.getPath());
        parseLoadListeners();
        init();
        if (this.reloadable) {
            this.contextFileChangeWatcher = new ContextFileChangeWatcher(this);
            this.contextFileChangeWatcher.start();
        }
        JspC jspC = new JspC();
        // 进行JspRuntimeContext初始化。
        new JspRuntimeContext(servletContext, jspC);
        LogFactory.get().info("Deploying of application directory {} has finished in {}", this.getDocBase(), timeInterval.intervalMs());
    }

    private void init() {
        if (!contextWebXmlFile.exists()) {
            return;
        }
        try {
            checkDuplicated();
        } catch (WebConfigDuplicatedException exception) {
            System.out.println("打印异常原因:" + exception.getMessage());
            exception.printStackTrace();
            return;
        }
        String xml = FileUtil.readUtf8String(this.contextWebXmlFile);
        Document document = Jsoup.parse(xml);
        parseServletMapping(document);
        parseFilterMapping(document);
        parseServletInitName(document);
        parseFilterInitParameters(document);
        initFilter();
        parseLoadOnStartup(document);
        handleLoadOnStartup();
        fireEvent("init");
    }

    private void initFilter() {
        Set<String> filterClassNames = filterClassNameToFilterName.keySet();
        for (String filterClassName : filterClassNames) {
            try {
                Class<?> clazz = this.getWebAppClassLoader().loadClass(filterClassName);
                Map<String, String> initParameters = filterClassNameToInitParameters.get(filterClassName);
                String filterName = filterClassNameToFilterName.get(filterClassName);
                FilterConfig filterConfig = new StandardFilterConfig(servletContext, initParameters, filterName);
                @SuppressWarnings("SuspiciousMethodCalls") Filter filter = filterPool.get(clazz);
                if (null == filter) {
                    filter = (Filter) ReflectUtil.newInstance(clazz);
                    filter.init(filterConfig);
                    filterPool.put(filterClassName, filter);
                }
            } catch (Exception exception) {
                throw new RuntimeException("异常原因:" + exception);
            }
        }
    }

    /**
     * 具体的映射解析.
     *
     * @param document 需要解析web.xml
     */
    private void parseServletMapping(Document document) {
        // 地址对servletName的映射
        Elements elementsUrlToServletName = document.select("servlet-mapping url-pattern");
        for (Element elementOne : elementsUrlToServletName) {
            String urlPattern = elementOne.text();
            String servletName = elementOne.parent().select("servlet-name").first().text();
            urlToServletName.put(urlPattern, servletName);
        }
        // servlet-name 和 servlet-className 之间的相互映射
        Elements elementServletClassNameToServletName = document.select("servlet servlet-name");
        for (Element elementTwo : elementServletClassNameToServletName) {
            String servletName = elementTwo.text();
            String servletClassName = elementTwo.parent().select("servlet-class").first().text();
            servletNameToServletClassName.put(servletName, servletClassName);
            servletClassNameToServletName.put(servletClassName, servletName);
        }
        // url地址对servlet-className的映射.
        Set<String> urls = urlToServletName.keySet();
        for (String url : urls) {
            String servletName = urlToServletName.get(url);
            String servletClass = servletNameToServletClassName.get(servletName);
            urlToServletClassName.put(url, servletClass);
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private void parseFilterInitParameters(Document document) {
        Elements elements = document.select("filter-class");
        for (Element element : elements) {
            String filterClassName = element.text();
            Elements elementsInitParam = elements.parents().select("init-param");
            if (elementsInitParam.isEmpty()) {
                continue;
            }
            Map<String, String> initParam = new HashMap<>(512);
            for (Element elementInitParam : elementsInitParam) {
                String name = elementInitParam.select("init-name").get(0).text();
                String value = elementInitParam.select("init-value").get(0).text();
                initParam.put(name, value);
            }
            filterClassNameToInitParameters.put(filterClassName, initParam);
        }
    }

    /**
     * 解析配置的筛选器相关的表
     *
     * @param document 获取需要解析的web.xml文件
     * @see #parseFilterMapping(Document)
     */
    public void parseFilterMapping(Document document) {
        // url对应相关定义的筛选器的名称。
        Elements elements = document.select("filter-mapping url-pattern");
        for (Element element : elements) {
            String urlName = element.text();
            String filterName = element.parent().select("filter-name").first().text();
            List<String> filterNames = urlToFilterNames.computeIfAbsent(urlName, k -> new ArrayList<>());
            filterNames.add(filterName);
        }
        // filterName 和 filterClassName 表的解析
        Elements elementsTwo = document.select("filter filter-name");
        for (Element element : elementsTwo) {
            String filterName = element.text();
            String filterClassName = element.parent().select("filter-class").first().text();
            filterNameToFilterClassName.put(filterName, filterClassName);
            filterClassNameToFilterName.put(filterClassName, filterName);
        }
        // url对应的筛选器类.
        Set<String> urls = urlToFilterNames.keySet();
        for (String url : urls) {
            List<String> filterNames = urlToFilterNames.computeIfAbsent(url, k -> new ArrayList<>());
            for (String filterName : filterNames) {
                String filterClassName = filterNameToFilterClassName.get(filterName);
                List<String> filterClassNames = urlToFilterClassName.computeIfAbsent(url, k -> new ArrayList<>());
                filterClassNames.add(filterClassName);
            }
        }
    }

    /**
     * 使用{@link #checkDuplicated(Document, String, String)} 具体检查和配置相关的参数.
     *
     * @see #checkDuplicated()
     */
    private void checkDuplicated() throws WebConfigDuplicatedException {
        String xml = FileUtil.readUtf8String(this.contextWebXmlFile);
        Document document = Jsoup.parse(xml);
        checkDuplicated(document, "servlet servlet-name", "在servlet中出现相同的servlet-name:{},请检查WEB-INF/web.xml.");
        checkDuplicated(document, "servlet servlet-class", "在servlet中出现相同的servlet-class:{},请检查WEB-INF/web.xml");
        checkDuplicated(document, "servlet-mapping url-pattern", "在servlet-mapping中出现相同的url-pattern:{},请检查WEB-INF/web.xml");
    }

    /**
     * 检查xml中是否出现出现同样的配置出现的异常为{@link WebConfigDuplicatedException}
     *
     * @param document 获取{@link Document} 将xml转化的对象。
     * @param mapping  在xml中的结点对象.
     * @param desc     对异常处理的描述.
     * @see #checkDuplicated(Document, String, String)
     */
    private void checkDuplicated(Document document, String mapping, String desc) throws WebConfigDuplicatedException {
        Elements elements = document.select(mapping);
        List<String> contentList = new ArrayList<>();
        for (Element element : elements) {
            contentList.add(element.text());
        }
        // 进行排序,查看是否出现相同的配置
        Collections.sort(contentList);
        for (int i = 0; i < contentList.size() - 1; i++) {
            String preContent = contentList.get(i);
            String lastContent = contentList.get(i + 1);
            if (preContent.equals(lastContent)) {
                throw new WebConfigDuplicatedException(StrUtil.format(desc, preContent));
            }
        }
    }

    public String getPath() {
        return path;
    }

    public String getDocBase() {
        return docBase;
    }

    public void setDocBase(String docBase) {
        this.docBase = docBase;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public WebAppClassLoader getWebAppClassLoader() {
        return webAppClassLoader;
    }

    public String getServletClassName(String uri) {
        return urlToServletClassName.get(uri);
    }

    public boolean isReloadable() {
        return reloadable;
    }

    public void setReloadable(boolean reloadable) {
        this.reloadable = reloadable;
    }

    /**
     * 添加监听器
     *
     * @param listener 监听器
     * @see #addListener(ServletContextListener)
     */
    public void addListener(ServletContextListener listener) {
        this.servletContextListenerList.add(listener);
    }

    /**
     * 停止并销毁
     *
     * @see #stop()
     */
    public void stop() {
        webAppClassLoader.stop();
        contextFileChangeWatcher.stop();
        destroyServlets();
        fireEvent("destroyed");
    }

    /**
     * 加载父类对象{@link Host}去重载{@code Context.this}。
     *
     * @see #reload()
     */
    public void reload() {
        host.reload(this);
    }

    /**
     * 获取servlet类名
     *
     * @param uri 通过uri进行获取。
     * @see #useUrlToServletClassName(String)
     */
    public String useUrlToServletClassName(String uri) {
        return urlToServletClassName.get(uri);
    }

    /**
     * 通过{@link #servletPool} 获取servlet对象,如果在哈希表中不存在,则利用{@link Class#newInstance()}构造一个servlet对象。
     *
     * @param clazz 获取类{@code Class<?> clazz} servlet对象.
     * @see #getServlet(Class)
     */
    public synchronized HttpServlet getServlet(Class<?> clazz) throws
            InstantiationException, IllegalAccessException, ServletException {
        HttpServlet servlet = servletPool.get(clazz);
        if (servlet == null) {
            servlet = (HttpServlet) clazz.newInstance();
            ServletContext servletContextOne = this.getServletContext();
            String clazzName = clazz.getName();
            String servletName = servletClassNameToServletName.get(clazzName);
            Map<String, String> initParameters = servlet_className_init_params.get(clazzName);
            StandardServletConfig standardServletConfig = new StandardServletConfig(servletContext, initParameters, servletName);
            servlet.init(standardServletConfig);
            servletPool.put(clazz, servlet);
        }
        return servlet;
    }

    /**
     * 将web.xml进行解析,获取servletClassName属性,初始化名和初始化值的属性。存储到{@link #servlet_className_init_params}中.
     *
     * @param document 获取{@link Document}对象
     * @see #parseServletInitName(Document)
     */
    @SuppressWarnings("DuplicatedCode")
    private void parseServletInitName(Document document) {
        Elements servletClassNameElements = document.select("servlet-class");
        for (Element servletClassNameElement : servletClassNameElements) {
            String servletClassName = servletClassNameElement.text();
            Elements initElements = servletClassNameElement.parent().select("init-param");
            if (initElements.isEmpty()) {
                continue;
            }
            Map<String, String> initParams = new HashMap<>(512);
            for (Element element : initElements) {
                String name = element.select("init-name").get(0).text();
                String value = element.select("init-value").get(0).text();
                initParams.put(name, value);
            }
            servlet_className_init_params.put(servletClassName, initParams);
        }
    }

    /**
     * 解析相关到web.xml文件.获取web中定义到监听器。
     *
     * @see #parseLoadListeners()
     */
    private void parseLoadListeners() {
        try {
            if (!contextWebXmlFile.exists()) {
                return;
            }
            String xml = FileUtil.readUtf8String(contextWebXmlFile);
            Document document = Jsoup.parse(xml);

            Elements elements = document.select("listener listener-class");
            for (Element element : elements) {
                String listenerClassName = element.text();

                Class<?> clazz = this.getWebAppClassLoader().loadClass(listenerClassName);
                ServletContextListener listener = (ServletContextListener) clazz.newInstance();
                addListener(listener);
            }
        } catch (ClassNotFoundException | IORuntimeException | InstantiationException | IllegalAccessException exception) {
            throw new RuntimeException("异常原因:");
        }
    }

    private void fireEvent(String type) {
        ServletContextEvent event = new ServletContextEvent(servletContext);
        for (ServletContextListener servletContextListener : servletContextListenerList) {
            if (INIT_VALUE.equals(type)) {
                servletContextListener.contextInitialized(event);
            } else if (DESTROYED_VALUE.equals(type)) {
                servletContextListener.contextDestroyed(event);
            } else {
                System.out.println("请输入正确的type值.");
            }
        }
    }

    /**
     * 此方法获取那些Servlet对象是需要进行"自启动"操作的.
     *
     * @param document 获取xml文件转化为{@link Document}的对象
     * @see #parseLoadOnStartup(Document)
     */
    private void parseLoadOnStartup(Document document) {
        Elements loadOnStartupElements = document.select("load-on-startup");
        for (Element loadOnStartupElement : loadOnStartupElements) {
            String loadOnStartupServletClassName = loadOnStartupElement.parent().select("servlet-class").text();
            loadOnStartupServletClassNames.add(loadOnStartupServletClassName);
        }
    }

    /**
     * 设置getter方法.
     *
     * @see #getServletContext()
     */
    public ServletContext getServletContext() {
        return servletContext;
    }

    /**
     * 准备这个方法删除所有的Servlet对象.
     *
     * @see #destroyServlets()
     */
    private void destroyServlets() {
        Collection<HttpServlet> servlets = servletPool.values();
        for (HttpServlet servlet : servlets) {
            servlet.destroy();
        }
    }

    /**
     * 对这些类进行自启动.
     *
     * @see #handleLoadOnStartup()
     */
    public void handleLoadOnStartup() {
        for (String loadOnStartupServletClassName : loadOnStartupServletClassNames) {
            try {
                Class<?> clazz = webAppClassLoader.loadClass(loadOnStartupServletClassName);
                getServlet(clazz);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ServletException e) {
                LogFactory.get().info("异常原因:" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 三种匹配模式
     *
     * @param uri 需要匹配的资源路径
     * @see #getMatchedFilters(String)
     */
    public List<Filter> getMatchedFilters(String uri) {
        List<Filter> filters = new ArrayList<>();
        Set<String> patterns = urlToFilterClassName.keySet();
        Set<String> matchedPatterns = new HashSet<>();
        Set<String> matchedFilterClassNames = new HashSet<>();
        for (String pattern : patterns) {
            if (match(pattern, uri)) {
                matchedPatterns.add(pattern);
            }
            List<String> filterClassName = urlToFilterClassName.get(pattern);
            matchedFilterClassNames.addAll(filterClassName);
        }
        for (String filterClassName : matchedFilterClassNames) {
            Filter filter = filterPool.get(filterClassName);
            filters.add(filter);
        }
        return filters;
    }

    /**
     * 匹配方式
     *
     * @param pattern 匹配的模式
     * @param uri     需要匹配的资源字段
     * @see #match(String, String)
     */
    private boolean match(String pattern, String uri) {
        // 完全匹配;
        if (StrUtil.equals(pattern, uri)) {
            return true;
        }
        if (StrUtil.equals(pattern, START_STAR)) {
            return true;
        }
        if (StrUtil.startWith(pattern, START_STAR_DOT)) {
            String patternExtension = StrUtil.subAfter(pattern, ".", false);
            String uriExtension = StrUtil.subAfter(pattern, ".", false);
            return patternExtension.equals(uriExtension);
        }
        return false;
    }
}
