package com.luzhi.miniTomcat.test;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.luzhi.miniTomcat.util.MiniBrowser;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/5/11
 * 为自动测试类
 */
public class TomcatTest {

    /**
     * @see #PORT
     * <p>
     * 测试端口
     */
    private static final int PORT = 9091;

    /**
     * @see #HOST
     * <p>
     * 本地主机地址
     */
    private static final String HOST = "127.0.0.1";

    /**
     * @see #POOL_SIZE
     * <p>
     * 测试的线程数为3
     */
    private static final int POOL_SIZE = 3;

    /**
     * @see #beforeClass()
     * 该类为测试类,所有的测试之前Jvm执行该方法一次并使用{@link BeforeClass}注解,注解的方法必须为 public static void 所修饰
     * 它和{@link org.junit.Before} 的区别是每个测试都执行一遍. 注解的方法必须为 public static void 所修饰
     */
    @BeforeClass
    public static void beforeClass() {
        if (NetUtil.isUsableLocalPort(PORT)) {
            System.err.println("miniTomcat服务还没有启动,请检查端口是否启动该服务.谢谢");
            // 终止当前的Java虚拟机,非零表示为异常终止.
            System.exit(1);
        } else {
            System.out.println("miniTomcat服务已经启动啦, 下面进行单元测试哦!");

        }
    }

    /**
     * @see #testTomcat()
     * 对字符文本进行操作
     */
    @Test
    public void testTomcat() {
        String content = getContentString("/");
        Assert.assertEquals(content, "<h1 align='center' style='color:pink;'> Hello, I't is new simplify tomcat</h1>");
    }

    /**
     * @see #testHtml()
     * 对h5进行文本进行操作
     */
    @Test
    public void testHtml() {
        String content = getContentString("/login.html");
        Assert.assertEquals(content, "<h1 align='center' style='color:pink;'> Hello, I't is new simplify tomcat</h1>");
    }

    /**
     * @see #textHtmlCrossXml()
     * 对多应用配置(使用xml)进行配置进行测试
     */
    @Test
    public void textHtmlCrossXml() {
        String content = getContentString("/resource/manyConf.html");
        Assert.assertEquals(content, "<h1 style='color:pink; align-content: center'> Hello, I't is new simplify tomcat</h1>");
    }

    @Test
    public void testServletText() {
        String content = getContentString("/hello");
        Assert.assertEquals(content, "<h1 align=center style='color:pink'>Hello, It's really easy servlet</h1>");
    }

    /**
     * @see #testOtherHtml()
     * 对webapps的多应用文件进行测试
     */
    @Test
    public void testOtherHtml() {
        String content = getContentString("/a/start.html");
        Assert.assertEquals(content, "<h1 style='color:pink; align-content: center'> Hello, I't is new simplify tomcat</h1>");
    }

    /**
     * @see #testPage404()
     * 对"404"页面进行测试
     */
    @Test
    public void testPage404() {
        String response = getHttpString("/not_exist.html");
        containAssert(response, "HTTP/1.1 404 NOT FOUND");
    }

    /**
     * @see #testPage500()
     * 对"500"页面进行测试.
     */
    @Test
    public void testPage500() {
        String response = getHttpString("/500.html");
        containAssert(response, "HTTP/1.1 500 Internal Server Error");
    }

    @Test
    public void testJ2eeHello() {
        String content = getContentString("/j2ee/hello");
        Assert.assertEquals(content, "<h1 align=center style='color:pink'>Hello, It's really easy servlet</h1>");
    }

    @Test
    public void testJavaWebHello() {
        String content = getContentString("/javaweb/hello");
        String contentTest = getContentString("/javaweb/hello");
        Assert.assertEquals(content, contentTest);
    }

    /**
     * @see #testLoginHtml()
     * 创建3个线程对页面login.html进行访问.由于BootStrap现在是多线程处理.
     */

    @Test
    public void testLoginHtml() throws InterruptedException {
        // 创建倒数锁存器并初始化为3
        CountDownLatch countDownLatch = new CountDownLatch(3);
        // 使用计数器.统计下面线程运行的时间。默认初始 (isNano 为 false).
        TimeInterval timeInterval = DateUtil.timer();
        for (int i = 0; i < POOL_SIZE; i++) {
            //noinspection AlibabaAvoidManuallyCreateThread
            new Thread(() -> {
                getContentString("/login.html");
                // 将倒数锁存器初始化倒数逐级递减.
                countDownLatch.countDown();
            }, "Thread" + i).start();
        }
        // 将当前线程进行挂起.直到倒数锁存器的倒数递减为零.释放当前挂起的线程.
        countDownLatch.await();
        // 统计时间,从开始到当前所用的时间.默认为毫秒.
        long duration = timeInterval.intervalMs();
        // 因为是线程池进行处理.自然 duration 小于3000毫秒.
        Assert.assertTrue(duration < 3000);
    }

    /**
     * @see #testJpgFile() 对图片文件读取进行测试
     * 测试手段,比较文件长度大小.
     */
    @Test
    public void testJpgFile() {
        byte[] bytes = getContentBytes("/lastPic.jpg");
        final int fileLength = 505797;
        Assert.assertEquals(fileLength, bytes.length);
    }

    /**
     * @see #testPdfFile() 对pdf文件进行读取测试
     * 测试手段,上同.
     */
    @Test
    public void testPdfFile() {
        byte[] bytes = getContentBytes("/healthBook.pdf");
        final int pdfFileLength = 262524;
        Assert.assertEquals(pdfFileLength, bytes.length);
    }

    @Test
    public void testGetParam() {
        String uri = "/javaweb/param";
        //noinspection HttpUrlsUsage
        String url = StrUtil.format("http://{}:{}{}", HOST, PORT, uri);
        //noinspection MismatchedQueryAndUpdateOfCollection
        Map<String, Object> params = new HashMap<>(64);
        params.put("name", "鲁滍");
        // 测试GET方法
        String result = MiniBrowser.getContentString(url, params, true);
        Assert.assertEquals(result, "get的name属性为:鲁滍");
    }

    @Test
    public void testPostParam() {
        String uri = "/javaweb/param";
        //noinspection HttpUrlsUsage
        String url = StrUtil.format("http://{}:{}{}", HOST, PORT, uri);
        //noinspection MismatchedQueryAndUpdateOfCollection
        Map<String, Object> params = new HashMap<>(64);
        params.put("name", "白上吹雪");
        // 对POST请求体处理
        String result = MiniBrowser.getContentString(url, params, false);
        Assert.assertEquals(result, "post的name属性为:白上吹雪");
    }

    @Test
    public void testHeader() {
        String result = getContentString("/javaweb/header");
        Assert.assertEquals(result, "mini browser / java1.8");
    }

    @Test
    public void testCookie() {
        String html = getHttpString("/javaweb/setCookie");
        containAssert(html, "Set-Cookie:name=luzhi;Expires=");
    }

    @Test
    public void testGetCookie() throws IOException {
        @SuppressWarnings("HttpUrlsUsage")
        String url = StrUtil.format("http://{}:{}{}", HOST, PORT, "/javaweb/getCookie");
        URL u = new URL(url);
        HttpURLConnection httpUrlConnection = (HttpURLConnection) u.openConnection();
        httpUrlConnection.setRequestProperty("Cookie", "name=luzhi");
        httpUrlConnection.connect();
        InputStream inputStream = httpUrlConnection.getInputStream();
        String html = IoUtil.read(inputStream, StandardCharsets.UTF_8);
        containAssert(html, "name:luzhi");
    }

    @Test
    public void testSession() throws IOException {
        String jSessionId = getContentString("/javaweb/setSession");
        if (null != jSessionId) {
            jSessionId = jSessionId.trim();
        }
        //noinspection HttpUrlsUsage
        String url = StrUtil.format("http://{}:{}{}", HOST, PORT, "/javaweb/getSession");
        URL u = new URL(url);
        HttpURLConnection httpUrlConnection = (HttpURLConnection) u.openConnection();
        httpUrlConnection.setRequestProperty("Cookie", "JSESSIONID=" + jSessionId);
        httpUrlConnection.connect();
        InputStream inputStream = httpUrlConnection.getInputStream();
        String html = IoUtil.read(inputStream, StandardCharsets.UTF_8);
        containAssert(html, "luzhi");
    }

    @Test
    public void testGzip() {
        byte[] gzipContent = getContentBytes("/", true);
        byte[] unGzipContent = ZipUtil.unGzip(gzipContent);
        String content = new String(unGzipContent);
        containAssert(content, "<h1 align=\"center\" style=\"color: pink; align-content: center\">Welcome miniTomcat! miniTomcat already start.</h1>");
    }

    @Test
    public void testJavaWeb0Hello() {
        String html = getContentString("/javaweb0/hello.jsp");
        System.out.println(html);
        containAssert(html,"Hello");
    }

    @Test
    public void testJsp() {
        String html = getContentString("/javaweb/");
        Assert.assertEquals(html, "hello jsp@This is javaweb");
    }

    /**
     * @see #getContentString(String)
     * 该私有静态方法获取浏览器的内容,通过{@link MiniBrowser#getContentString(String)}方法进行获取.
     */
    private static String getContentString(String uri) {
        //noinspection HttpUrlsUsage
        String url = StrUtil.format("http://{}:{}{}", HOST, PORT, uri);
        return MiniBrowser.getContentString(url);
    }

    /**
     * @see #getHttpString(String)
     * 模拟资源不存在
     */
    private String getHttpString(String uri) {
        //noinspection HttpUrlsUsage
        String url = StrUtil.format("http://{}:{}{}", HOST, PORT, uri);
        return MiniBrowser.getHttpString(url);
    }

    /**
     * @see #containAssert(String, String)
     * 当资源不存在则设置断言.
     */
    private void containAssert(String html, String string) {
        boolean match = StrUtil.containsAny(html, string);
        Assert.assertTrue(match);
    }

    /**
     * @see #getContentString(String) 将路径资源文件读取为字节流.
     * 详情请看{@link #getContentBytes(String, boolean)}
     */
    private byte[] getContentBytes(@SuppressWarnings("SameParameterValue") String uri) {
        return getContentBytes(uri, false);
    }

    /**
     * @see #getContentBytes(String, boolean) 默认无压缩模式
     * 详细操作请看 {@link MiniBrowser#getContentString(String, boolean)};
     */
    private byte[] getContentBytes(String uri, @SuppressWarnings("SameParameterValue") boolean gzip) {
        //noinspection HttpUrlsUsage
        String url = StrUtil.format("http://{}:{}{}", HOST, PORT, uri);
        return MiniBrowser.getContentBytes(url, gzip);
    }
}
