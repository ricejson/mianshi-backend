package com.rice.mianshi.satoken;

import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentParser;
import com.qcloud.cos.Headers;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 设备工具类
 * @author ricejson
 */
public class DeviceUtils {

    /**
     * 获取用户登录设备
     * @return
     */
    public static String getUserLoginDevice(HttpServletRequest request) {
        String userAgentInfo = request.getHeader(Headers.USER_AGENT);
        UserAgent userAgent = UserAgentParser.parse(userAgentInfo);
        // 默认为pc
        String device = "pc";
        if (isMiniProgram(userAgentInfo)) {
            device = "miniProgram";
        } else if (isPad(userAgentInfo)) {
            device = "pad";
        } else if (userAgent.isMobile()) {
            device = "mobile";
        }
        return device;
    }

    /**
     * 是否是小程序（通过是否包含 MicroMessenger）
     * @param userAgent
     * @return
     */
    private static boolean isMiniProgram(String userAgent) {
        return  StringUtils.containsIgnoreCase(userAgent, "MicroMessenger")
                && StringUtils.containsIgnoreCase(userAgent, "MiniProgram");
    }

    /**
     * 是否是平板
     * @param userAgent
     * @return
     */
    private static boolean isPad(String userAgent) {
        // 判断是否是苹果ipad
        boolean isIpad = StringUtils.containsIgnoreCase(userAgent, "iPad");
        // 判断是否是安卓平板
        boolean isAndroidPad = StringUtils.containsIgnoreCase(userAgent, "Android")
                && !StringUtils.containsIgnoreCase(userAgent, "Mobile");
        return  isIpad || isAndroidPad;
    }
}
