package com.rice.mianshi.blacklist;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;


/**
 * nacos配置监听器
 * InitializingBean 只会执行一次
 */
@Slf4j
@Component
public class NacosConfigListener implements InitializingBean {
    @Value("${nacos.config.data-id}")
    private String dataId;

    @Value("${nacos.config.group}")
    private String group;

    @NacosInjected
    private ConfigService configService;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("开始注册监听器，监听nacos config 变更...");
        configService.addListener(dataId, group, new Listener() {
            @Override
            public Executor getExecutor() {
                return null;
            }

            @Override
            public void receiveConfigInfo(String s) {
                log.info("监听到 nacos config 变更...");
                BlackIpListUtils.reBuildBloomFilter(s);
            }
        });
        // 一开始没有变更也需要读取！！！
        String config = configService.getConfig(dataId, group, 5000);
        BlackIpListUtils.reBuildBloomFilter(config);
    }
}
