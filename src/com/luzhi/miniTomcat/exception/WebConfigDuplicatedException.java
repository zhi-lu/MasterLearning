package com.luzhi.miniTomcat.exception;

/**
 * @author apple
 * @version jdk1.8
 * // TODO : 2021/5/21
 * 自定义异常不允许WEB-INF/web.xml出现同样对配置.
 */
public class WebConfigDuplicatedException extends Exception {

    public WebConfigDuplicatedException(String message) {
        super(message);
    }
}
