package com.rice.mianshi.blacklist;

import cn.hutool.bloomfilter.BitMapBloomFilter;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 黑名单工具类
 * @author ricejson
 */
public class BlackIpListUtils {

    /**
     * bloomFilter 布隆过滤器
     */
    private static BitMapBloomFilter bloomFilter;

    /**
     * 判断ip是否在黑名单中
     * @param ip 待校验ip
     * @return
     */
    public static boolean isInBlackIpList(String ip) {
        return bloomFilter.contains(ip);
    }

    /**
     * 重建布隆过滤器
     * @param configInfo
     */
    public static void reBuildBloomFilter(String configInfo) {
        if (StringUtils.isBlank(configInfo)) {
            configInfo = "{}";
        }
        synchronized (BlackIpListUtils.class) {
            bloomFilter = new BitMapBloomFilter(200);
            Yaml yaml = new Yaml();
            Map map = yaml.loadAs(configInfo, Map.class);
            List<String> blackIpList = (List<String>) map.getOrDefault("blackIpList", new ArrayList<>());
            for (String ip : blackIpList) {
                bloomFilter.add(ip);
            }
        }
    }


}
