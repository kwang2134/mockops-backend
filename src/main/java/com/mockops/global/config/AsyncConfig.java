package com.mockops.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 비동기 처리 및 스케줄링 설정
 * Bulk Mock API 생성 등 대용량 작업을 비동기로 처리하기 위한 전용 스레드 풀 구성
 * 헬스 체크 스케줄러 활성화
 */
@Slf4j
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

    /**
     * Bulk Mock API 생성 전용 스레드 풀
     *
     * - corePoolSize: 기본 스레드 수 (2개)
     * - maxPoolSize: 최대 스레드 수 (5개)
     * - queueCapacity: 대기 큐 용량 (100개)
     * - threadNamePrefix: 스레드 이름 접두사
     * - rejectedExecutionHandler: 큐가 가득 찼을 때 호출자 스레드에서 직접 실행
     */
    @Bean(name = "bulkTaskExecutor")
    public Executor bulkTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 기본 스레드 수
        executor.setCorePoolSize(2);

        // 최대 스레드 수
        executor.setMaxPoolSize(5);

        // 대기 큐 용량
        executor.setQueueCapacity(100);

        // 스레드 이름 접두사
        executor.setThreadNamePrefix("bulk-task-");

        // 큐가 가득 찼을 때 처리 정책: 작업을 거부하고 예외 발생
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

        // 애플리케이션 종료 시 모든 작업이 완료될 때까지 대기
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();

        log.info("bulkTaskExecutor 스레드 풀 초기화 완료: corePoolSize={}, maxPoolSize={}, queueCapacity={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }
}