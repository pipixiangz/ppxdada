package com.ppx.ppxdada.scoring;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 表示这个注解可以应用于类、接口（包括注解类型）或枚举声明上
@Target(ElementType.TYPE)
// 这个注解将在运行时保留，因此可以通过反射机制访问
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface ScoringStrategyConfig {

    /**
     * 应用类型
     *
     * @return
     */
    int appType();

    /**
     * 评分策略
     *
     * @return
     */
    int scoringStrategy();
}
