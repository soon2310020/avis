package vn.com.twendie.avis.notification.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import vn.com.twendie.avis.api.core.config.EnableApiCore;
import vn.com.twendie.avis.api.rest.config.EnableRestCommonApi;
import vn.com.twendie.avis.data.config.EnableDataModel;
import vn.com.twendie.avis.queue.config.EnableCommonQueue;

@Configuration
@EnableApiCore
@EnableDataModel
@EnableCommonQueue
@EnableRestCommonApi
@EnableJpaRepositories("vn.com.twendie.avis.notification.repository")
@ComponentScan({"vn.com.twendie.avis.notification"})
public class ModuleConfiguration {
}
