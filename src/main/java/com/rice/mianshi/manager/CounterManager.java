package com.rice.mianshi.manager;

import com.alibaba.excel.util.StringUtils;
import com.rice.mianshi.constant.RedisConstant;
import io.swagger.models.auth.In;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.IntegerCodec;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 通用计数器实现类
 * @author ricejson
 */
@Component
public class CounterManager {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 累加并统计计数结果
     * 默认为1分钟内的
     * @param key
     * @return
     */
    public int incrAndGetCounter(String key) {
        return incrAndGetCounter(key, 1, TimeUnit.MINUTES);
    }

    /**
     *
     * @param key key
     * @param interval 统计间隔
     * @param timeUnit 时间单位
     * @return
     */
    public int incrAndGetCounter(String key, int interval, TimeUnit timeUnit) {
        // 获取过期时间
        int expireTimeInSeconds;
        switch (timeUnit) {
            case HOURS: {
                expireTimeInSeconds = interval * 60 * 60;
                break;
            }
            case MINUTES: {
                expireTimeInSeconds = interval * 60;
                break;
            }
            case SECONDS: {
                expireTimeInSeconds = interval;
                break;
            }
            default: {
                throw new IllegalArgumentException("Unsupported TimeUnit, Please use SECONDS、MINUTES、HOURS");
            }
        }
        return incrAndGetCounter(key, interval, timeUnit, expireTimeInSeconds);
    }

    /**
     *
     * @param key key
     * @param interval 统计间隔
     * @param timeUnit 时间单位
     * @param expireTime 过期时间(秒数)
     * @return
     */
    public int incrAndGetCounter(String key, int interval, TimeUnit timeUnit, int expireTime) {
        if (StringUtils.isBlank(key)) {
            return 0;
        }
        long offset = 0;
        switch (timeUnit) {
            case HOURS: {
                offset = Instant.now().getEpochSecond() / (interval * 60 * 60);
                break;
            }
            case MINUTES: {
                offset = Instant.now().getEpochSecond() / (interval * 60);
                break;
            }

            case SECONDS: {
                offset = Instant.now().getEpochSecond() / interval;
                break;
            }
            default: {
                throw new IllegalArgumentException("Unsupported TimeUnit, Please use SECONDS、MINUTES、HOURS");
            }
        }
        String redisKey = key + ":" + offset;
        String script = "if redis.call('exists', KEYS[1]) == 1 then\n" +
                "  return redis.call('incr', KEYS[1]);\n" +
                "else \n" +
                "  redis.call('set', KEYS[1], 1);\n" +
                "  redis.call('expire', KEYS[1], ARGV[1]);\n" +
                "  return 1;\n" +
                "end";
        return redissonClient.getScript(IntegerCodec.INSTANCE).eval(
                RScript.Mode.READ_WRITE,
                script,
                RScript.ReturnType.INTEGER,
                Collections.singletonList(redisKey),
                expireTime
        );
    }

}
