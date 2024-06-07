package com.ppx.ppxdada.config;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@Data
public class VipSchedulerConfig {

    @Bean
    public Scheduler vipScheduler() {
        // 创建一个自定义的线程工厂，用于为线程池创建线程
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                // 创建一个新的线程，并使用 VIPThreadPool- 前缀和一个递增的线程编号命名线程
                Thread t = new Thread(r, "VIPThreadPool-" + threadNumber.getAndIncrement());
                t.setDaemon(false); // 设置为非守护线程（即 JVM 退出时不强制关闭此线程）
                return t;
            }
        };

        // 使用自定义的线程工厂创建一个定长线程池，线程池大小为 10
        ExecutorService executorService = Executors.newScheduledThreadPool(10, threadFactory);

        // 从 ExecutorService 创建一个 Scheduler 并返回
        return Schedulers.from(executorService);
    }
}
