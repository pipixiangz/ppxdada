package com.ppx.ppxdada.config;

import com.zhipu.oapi.ClientV4;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ai")
@Data
public class AiConfig {
    /**
     * apiKey,需要从开放平台获取
     */

    // 这个名称和 application.yml 中的 ai.api-key 对应
    private String apiKey;

    // 使用 @Bean 注解，将 ClientV4 注入到 Spring 容器中，省的每次都要 new 一个
    @Bean
    public ClientV4 getClientV4() {
        return new ClientV4.Builder(apiKey).build();
    }
}

