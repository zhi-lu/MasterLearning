// noinspection JSUnusedGlobalSymbols

/**
 * @author apple
 * @version javaScript11
 * @since 1.0
 * // TODO : 2021/5/21
 */
import(document)

/**
 * 设置cookie值
 * @see #setCookie
 * @param paramName 设置参数名
 * @param paramValue 设置参数值
 * @param exDays 设置cookie多少天过期
 */
function setCookie(paramName, paramValue, exDays) {
    let date = new Date();
    date.setTime(date.getTime() + (exDays * 24 * 60 * 60 * 1000));
    let expires = "expires=" + date.toUTCString();
    document.cookie = paramName + "=" + paramValue + ";" + expires;
}

/**
 * 获取cookie值
 * @see getCookie
 * @param paramName 获取cookie键值
 */
function getCookie(paramName) {
    let name = paramName + "=";
    let cookieSet = document.cookie.split(";");
    for (let i = 0; i < cookieSet.length; i++) {
        let result = cookieSet[i].trim();
        if (result.indexOf(name) === 0) {
            return result.substring(name.length, result.length);
        }
    }
}

/**
 * 检查cookie值
 * @see checkCookie
 * @param paramName 需要检查的cookie值
 */
function checkCookie(paramName) {
    let name = getCookie(paramName);
    if (name !== "" && name != null) {
        window.alert("cookie值存在" + name + "呀");
    } else {
        name = window.prompt("输出键值:", "");
        if (name !== "" && name != null) {
            setCookie(paramName, name, 1);
        }
    }
}