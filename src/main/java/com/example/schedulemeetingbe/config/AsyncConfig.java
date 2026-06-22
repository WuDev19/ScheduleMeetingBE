package com.example.schedulemeetingbe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "outboxExecutor")
    public Executor outboxThreadPoolExecutor(){
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(5);
        threadPoolTaskExecutor.setMaxPoolSize(10);
        threadPoolTaskExecutor.setQueueCapacity(500);
        threadPoolTaskExecutor.setThreadNamePrefix("out-box");
        return threadPoolTaskExecutor;
    }

//    @Bean(name = "emailExecutor")
//    public Executor emailThreadPoolExecutor(){
//        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
//        threadPoolTaskExecutor.setCorePoolSize(5);
//        threadPoolTaskExecutor.setMaxPoolSize(10);
//        threadPoolTaskExecutor.setQueueCapacity(500);
//        threadPoolTaskExecutor.setThreadNamePrefix("email");
//        return threadPoolTaskExecutor;
//    }

}
