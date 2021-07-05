package com.luzhi.miniTomcat.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import com.luzhi.miniTomcat.http.Request;
import com.luzhi.miniTomcat.http.Response;
import com.luzhi.miniTomcat.http.StandardSession;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/6/15
 * 生成对Session的管理配置器
 */
public abstract class SessionManagerUtil {


    static {
        startCheckOutDateSession();
    }

    /**
     * @see #SESSION_MAP
     * <p>
     * 将Session对象保存到该图中.
     */
    private static final Map<String, StandardSession> SESSION_MAP = new HashMap<>(512);

    /**
     * @see #DEFAULT_TIME
     * <p>
     * 获取Session超时时间.
     */
    private static final int DEFAULT_TIME = getTimeout();

    /**
     * @see #getTimeout()
     * <p>
     * 该静态方法获取超时时间,一种获取通过本目录下{@code {System.getProperty("user.dir")}}/conf获取web.xml
     * 解析session超时配置的标签获取超时时间,当该标签值不存在,获取解析初始错误.将超时间默认值设为30分钟。
     */
    private static int getTimeout() {
        int defaultTime = 30;
        try {
            Document document = Jsoup.parse(ConstantTomcat.RESOURCE_FOLDER, "utf-8");
            Elements elements = document.select("session-config session-timeout");
            if (elements.isEmpty()) {
                return defaultTime;
            }
            return Convert.toInt(elements.get(0).text());
        } catch (IOException exception) {
            return defaultTime;
        }
    }

    /**
     * @see #startCheckOutDateSession()
     * <p>
     * 创建服务线程,每个30秒检查是否存在超时的Session对象.
     */
    @SuppressWarnings("AlibabaAvoidManuallyCreateThread")
    private static void startCheckOutDateSession() {
        new Thread(() -> {
            while (true) {
                checkOutDateSession();
                ThreadUtil.sleep(1000 * 30);
            }
        }).start();
    }

    /**
     * @see #checkOutDateSession()
     * <p>
     * 对存放{@link StandardSession}的map中的Session对象进行判断是否超时.如果超时则删除该Session对象.
     */
    private static void checkOutDateSession() {
        Set<String> jSessionIdSet = SESSION_MAP.keySet();
        List<String> jSessionOutDateList = new ArrayList<>();
        for (String jSessionId : jSessionIdSet) {
            StandardSession standardSession = SESSION_MAP.get(jSessionId);
            long interval = System.currentTimeMillis() - standardSession.getLastAccessedTime();
            if (interval > (long) standardSession.getMaxInactiveInterval() * 1000 * 60) {
                jSessionOutDateList.add(jSessionId);
            }
        }
        for (String jSessionIdOutDate : jSessionOutDateList) {
            SESSION_MAP.remove(jSessionIdOutDate);
        }
    }

    /**
     * @see #generateSessionId()
     * <p>
     * 生成一个JSessionId.
     */
    public static synchronized String generateSessionId() {
        String result;
        byte[] bytes = RandomUtil.randomBytes(16);
        result = new String(bytes);
        // 使用md5对SessionId进行加密
        result = SecureUtil.md5(result);
        result = result.toUpperCase(Locale.getDefault());
        return result;
    }

    /**
     * 结合JSessionId情况创建Session对象.
     *
     * @param jSessionId 获取JSESSIONID是否存在,并结合情况创建Session对象
     * @param request    接受{@link Request} 对象
     * @param response   接受{@link Response} 对象。
     * @see #getSession(String, Request, Response)
     */
    public static HttpSession getSession(String jSessionId, Request request, Response response) {
        // 如果jSession不存在则通过方法{@code newSession(Request var, Response var2)}直接创建
        if (null == jSessionId) {
            return newSession(request, response);
        } else {
            StandardSession standardSession = SESSION_MAP.get(jSessionId);
            // 如果当前的jSession对象失效,则通过方法{@code newSession(Request var, Response var2)} 直接创建.
            if (null == standardSession) {
                return newSession(request, response);
            } else {
                standardSession.setLastAccessedTime(System.currentTimeMillis());
                createCookieBySession(standardSession, request, response);
                return standardSession;
            }

        }
    }

    /**
     * <1>创建一个Session对象.
     *
     * @param request  通过Request对象获取ServletContext()对象.
     * @param response 将Response对象传入{@link #createCookieBySession(HttpSession, Request, Response)} 添加Cookie。
     * @see #newSession(Request, Response)
     */
    private static HttpSession newSession(Request request, Response response) {
        ServletContext servletContext = request.getServletContext();
        String jSessionId = generateSessionId();
        StandardSession session = new StandardSession(jSessionId, servletContext);
        session.setMaxInactiveInterval(DEFAULT_TIME);
        SESSION_MAP.put(jSessionId, session);
        createCookieBySession(session, request, response);
        return session;
    }

    /**
     * 将session对象的JSESSIONID和其属性值添加到的Cookie对象中
     *
     * @param session  通过Session对象创建{@link Cookie}对象.设置JSESSIONID属性
     * @param request  获取Context对象资源路径.
     * @param response 将Session对象保存到Cookie中.
     * @see #createCookieBySession(HttpSession, Request, Response)
     */
    private static void createCookieBySession(HttpSession session, Request request, Response response) {
        Cookie cookie = new Cookie("JSESSIONID", session.getId());
        cookie.setMaxAge(session.getMaxInactiveInterval());
        cookie.setPath(request.getContext().getPath());
        response.addCookie(cookie);
    }
}
