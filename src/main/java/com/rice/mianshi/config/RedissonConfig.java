package com.rice.mianshi.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * RedissonConfig Redisson 配置类
 * @author ricejson
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {
    private String host;
    private String port;
    private String username;
    private String password;
    private Integer database;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer() // 单节点
                .setAddress("redis://" + host + ":" + port)
                .setDatabase(database)
                .setUsername(username)
                .setPassword(password); // 设置密码
        return Redisson.create(config);
    }

}
