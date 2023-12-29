package saleson.common.config;

import org.dozer.DozerBeanMapper;
import org.modelmapper.ModelMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.filter.OrderedCharacterEncodingFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.emoldino.framework.filter.EntryFilter;

@Configuration
public class AppConfig {
	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	@Bean
	public DozerBeanMapper dozerBeanMapper() {
		return new DozerBeanMapper();
	}

	@Bean
	public FilterRegistrationBean<EntryFilter> entryFilterRegistrationBean() {
		FilterRegistrationBean<EntryFilter> bean = new FilterRegistrationBean<>();
		bean.setFilter(new EntryFilter());
		bean.addUrlPatterns("/*");
		bean.setOrder(OrderedCharacterEncodingFilter.HIGHEST_PRECEDENCE + 1); // Set the order here
		return bean;
	}

}
