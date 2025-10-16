package com.rice.mianshi.constant;

/**
 * 跟redis相关的常量
 * @author ricejson
 */
public interface RedisConstant {
    String USER_SIGH_IN_REDIS_KEY_PREFIX = "mianshi:user";
    String USER_ACCESS_REDIS_KEY_PREFIX = "mianshi:user:access";

    /**
     * 获取用户签到 redis key
     * @param userId
     * @param year
     * @return
     */
    static String getUserSignInRedisKey(long userId, int year) {
        return String.format("%s:%d:%d", USER_SIGH_IN_REDIS_KEY_PREFIX, userId, year);
    }

    /**
     * 获取用户访问 redis key
     * @param userId
     * @return
     */
    static String getUserAccessRedisKey(long userId) {
        return String.format("%s:%d", USER_ACCESS_REDIS_KEY_PREFIX, userId);
    }
}
