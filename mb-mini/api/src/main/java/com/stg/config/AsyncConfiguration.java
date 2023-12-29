package com.stg.config;

import com.stg.config.properties.AsyncProperties;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbc.JdbcLockProvider;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import javax.sql.DataSource;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;


/**
 * WARNING: Hạn chế (Không) tạo thêm executor
 * int numOfCores = Runtime.getRuntime().availableProcessors();
 * Number of threads = Number of Available Cores * (1 + Wait time / Service time)
 * Number of threads = Number of Available Cores * Target CPU utilization * (1 + Wait time / Service time)
 */
@EnableAsync
@Configuration
@RequiredArgsConstructor
public class AsyncConfiguration implements AsyncConfigurer, SchedulingConfigurer {

    private final AsyncProperties asyncProperties;

    public static final String TAKE_TIME_HIGH_CPU = "takeTimeHighCPUExecutor";
    public static final String TAKE_TIME_LOW_CPU = "takeTimeLowCPUExecutor";

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcLockProvider(dataSource);
    }

    /**
     * FOR [takes less time]
     * => normal logic
     * => max pool size: vừa phải
     */
    @Override
    @Bean
    public ThreadPoolTaskExecutor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(asyncProperties.getNormal().getCorePoolSize());
        //executor.setMaxPoolSize(asyncProperties.getNormal().getMaxPoolSize());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();

        return executor;
    }

    /**
     * FOR [takes time and HIGH CPU]
     * => import-upload file, tính toán, save dữ liệu lớn, read-write I/O,... => khi đó thời gian chờ lâu, mức sử dụng CPU cao
     * => max pool size: nhỏ, để cân đối các tác vụ khác của hệ thống (cần sử dụng CPU)
     */
    @Bean(TAKE_TIME_HIGH_CPU)
    public ThreadPoolTaskExecutor getTakeTimeHighCPUExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(asyncProperties.getTakesTimeHighCpu().getCorePoolSize());
        //executor.setMaxPoolSize(asyncProperties.getTakesTimeHighCpu().getMaxPoolSize());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        executor.setThreadNamePrefix("TakeTimeHighCPUExecutor-");
        return executor;
    }

    /**
     * FOR [takes time but LOW CPU]
     * => call api third-party-service,... => khi đó thời gian chờ lâu, mức sử dụng CPU thấp
     * => max pool size: lớn, vì mức sử dụng CPU thấp, chủ yếu là thời gian chờ và khá lâu => queue size có thể lớn => cần giảm queue size!
     */
    @Bean(TAKE_TIME_LOW_CPU)
    public ThreadPoolTaskExecutor getTakeTimeLowCPUExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(asyncProperties.getTakesTimeLowCpu().getCorePoolSize());
        //executor.setMaxPoolSize(asyncProperties.getTakesTimeLowCpu().getMaxPoolSize());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        executor.setThreadNamePrefix("TakeTimeLowCPUExecutor-");
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(scheduledThreadPoolExecutor());
    }

    public ScheduledThreadPoolExecutor scheduledThreadPoolExecutor() {
        ThreadFactory threadFactory = new CustomizableThreadFactory("ScheduledExecutor-");

        return new ScheduledThreadPoolExecutor(asyncProperties.getSchedule().getCorePoolSize(), threadFactory);
    }
}
