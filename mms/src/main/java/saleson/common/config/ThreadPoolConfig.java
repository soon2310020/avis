package saleson.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ThreadPoolConfig {

	@Bean(initMethod = "initialize", destroyMethod = "destroy")
	@Primary
	public ThreadPoolTaskExecutor applicationTaskExecutor() {
		ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
		te.setCorePoolSize(8);
		te.setMaxPoolSize(50);
		te.setQueueCapacity(1000);
		te.setThreadNamePrefix("task-");
		te.setAllowCoreThreadTimeOut(true);
		te.setKeepAliveSeconds(60);
		te.setWaitForTasksToCompleteOnShutdown(false);
		te.setAwaitTerminationSeconds(120);
		return te;
	}

	@Bean(initMethod = "initialize", destroyMethod = "destroy")
	public ThreadPoolTaskExecutor transferTaskExecutor() {
		ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
		te.setCorePoolSize(5);
		te.setMaxPoolSize(5);
		te.setQueueCapacity(20);
		te.setThreadNamePrefix("transfer-");
		te.setAllowCoreThreadTimeOut(true);
		te.setKeepAliveSeconds(60);
		te.setWaitForTasksToCompleteOnShutdown(true);
		te.setAwaitTerminationSeconds(120);
		return te;
	}

	@Bean(initMethod = "initialize", destroyMethod = "destroy")
	public ThreadPoolTaskExecutor transfer2TaskExecutor() {
		ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
		te.setCorePoolSize(25);
		te.setMaxPoolSize(25);
		te.setQueueCapacity(20);
		te.setThreadNamePrefix("transfer2-");
		te.setAllowCoreThreadTimeOut(true);
		te.setKeepAliveSeconds(60);
		te.setWaitForTasksToCompleteOnShutdown(true);
		te.setAwaitTerminationSeconds(120);
		return te;
	}

	@Bean(initMethod = "initialize", destroyMethod = "destroy")
	public ThreadPoolTaskExecutor woTaskExecutor() {
		ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
		te.setCorePoolSize(10);
		te.setMaxPoolSize(10);
		te.setQueueCapacity(20);
		te.setThreadNamePrefix("wo-");
		te.setAllowCoreThreadTimeOut(true);
		te.setKeepAliveSeconds(60);
		te.setWaitForTasksToCompleteOnShutdown(true);
		te.setAwaitTerminationSeconds(120);
		return te;
	}

	@Bean(initMethod = "initialize", destroyMethod = "destroy")
	public ThreadPoolTaskExecutor dataTaskExecutor() {
		ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
		te.setCorePoolSize(30);
		te.setMaxPoolSize(30);
		te.setQueueCapacity(20);
		te.setThreadNamePrefix("data-");
		te.setAllowCoreThreadTimeOut(true);
		te.setKeepAliveSeconds(60);
		te.setWaitForTasksToCompleteOnShutdown(true);
		te.setAwaitTerminationSeconds(120);
		return te;
	}

	@Bean(initMethod = "initialize", destroyMethod = "destroy")
	public ThreadPoolTaskExecutor notiTaskExecutor() {
		ThreadPoolTaskExecutor te = new ThreadPoolTaskExecutor();
		te.setCorePoolSize(10);
		te.setMaxPoolSize(10);
		te.setQueueCapacity(20);
		te.setThreadNamePrefix("noti-");
		te.setAllowCoreThreadTimeOut(true);
		te.setKeepAliveSeconds(60);
		te.setWaitForTasksToCompleteOnShutdown(true);
		te.setAwaitTerminationSeconds(120);
		return te;
	}

}
