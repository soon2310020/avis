package com.emoldino.api.common.resource.composite.schexe.agent;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

@Profile("developer")
@EnableScheduling
@Component
public class SchExeAgent2 {

//	@Scheduled(fixedDelay = (10000), initialDelay = (10000))
//	public void summarizeMoldProcChgBatchJob() {
//		if ("central".equals(ConfigUtils.getServerName())) {
//			return;
//		}
//		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(MoldProcChgService.class, "summarizeBatch"), () -> BeanUtils.get(MoldProcChgService.class).summarizeBatch());
//	}

}
