package vn.com.twendie.avis.tracking.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import vn.com.twendie.avis.api.core.config.EnableApiCore;
import vn.com.twendie.avis.api.rest.config.EnableRestCommonApi;

@Configuration
@EnableApiCore
@EnableRestCommonApi
@ComponentScan({"vn.com.twendie.avis.tracking"})
public class ModuleConfiguration {
}
