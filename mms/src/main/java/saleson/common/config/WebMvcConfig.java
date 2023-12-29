package saleson.common.config;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import saleson.common.context.EmoldinoResourceMessageBundleSource;
import saleson.common.interceptor.MenuHandlerInterceptor;

@EnableWebMvc
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	public static final Instant appStartTime = Instant.now();

	@Value("${file.storage.location}")
	private String fileStorageLocation;

	@Value("${file.upload.dir}")
	private String fileUploadDir;

	@Value("${app.resource.static.cache-period:86400}")
	private int staticResourceCachePeriod;

//	@Bean
//	public LayoutDialect layoutDialect() {
//		return new LayoutDialect();
//	}

//	private static final String[] RESOURCE_LOCATIONS = { //
//			"/modules/**", //
//			"/imgaes/**", //
//			"/css/**", //
//			"/js/**", //
//			"*.html", //
//			"/ui/**", //
//			"/**/*.jpg"//
//	};

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {

		registry.addResourceHandler("/**")//
				.addResourceLocations("classpath:/static/")//
				.setCachePeriod(staticResourceCachePeriod);

		// 4 weeks
		registry.addResourceHandler("/js/cdn/**")//
				.addResourceLocations("classpath:/static/external/js/cdn/")//
				.setCachePeriod(2419200);

		// 4 weeks
		registry.addResourceHandler("/modules/**")//
				.addResourceLocations("classpath:/static/external/modules/")//
				.setCachePeriod(2419200);

		// 4 weeks
		registry.addResourceHandler("/ui/node_modules/**")//
				.addResourceLocations("classpath:/static/ui/node_modules/")//
				.setCachePeriod(2419200);

		registry.addResourceHandler("/upload/**")//
				.addResourceLocations("file:" + fileStorageLocation + fileUploadDir + "/");

		registry.addResourceHandler("swagger-ui.html")//
				.addResourceLocations("classpath:/META-INF/resources/");

		registry.addResourceHandler("/webjars/**")//
				.addResourceLocations("classpath:/META-INF/resources/webjars/");
	}

	@Autowired
	private MenuHandlerInterceptor menuHandlerInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(menuHandlerInterceptor)//
				.addPathPatterns("/**")//
				.excludePathPatterns(//
						"/login", //
						"/common", //
						"/common/", //
						"/asset", //
						"/asset/", //
						"/supplychain", //
						"/supplychain/", //
						"/analysis", //
						"/analysis/", //
						"/create-account/**", //
						"/modules/**", //
						"/error/**", //
						"/api/**", //
						"/images/**", //
						"/components/**", //
						"/upload/**", //
						"/favicon.ico", //
						"/css/**", //
						"/js/**", //
						"/**/*.html", //
						"*.html", //
						"/ui/**"//
				);
		//registry.addInterceptor(localeChangeInterceptor());
		registry.addInterceptor(paramCommonInterceptor())//
				.addPathPatterns("/**")//
				.excludePathPatterns(//
						"/common", //
						"/common/", //
						"/asset", //
						"/asset/", //
						"/supplychain", //
						"/supplychain/", //
						"/analysis", //
						"/analysis/", //
						"/api/**", //
						"/images/**", //
						"/components/**", //
						"/upload/**", //
						"/favicon.ico", //
						"/css/**", //
						"/js/**", //
						"/**/*.html", //
						"*.html"//
				);
	}

	public HandlerInterceptor paramCommonInterceptor() {
		HandlerInterceptor handlerInterceptor = new HandlerInterceptor() {
			@Override
			public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
				if (modelAndView != null) {
					modelAndView.addObject("noCacheVer", "v=" + appStartTime.toEpochMilli());
				}
				HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
			}
		};
		return handlerInterceptor;
	}

	//@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
		LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
		lci.setParamName("lang");
		return lci;
	}

	@Bean
	public LocaleResolver localeResolver() {
		SessionLocaleResolver slr = new SessionLocaleResolver();
		slr.setDefaultLocale(Locale.ENGLISH);
		return slr;
	}

	@Bean
	public MessageSource messageSource() {
		EmoldinoResourceMessageBundleSource messageSource = new EmoldinoResourceMessageBundleSource();
		messageSource.setDefaultEncoding("UTF-8");
		messageSource.setBasename("classpath:/messages");
		messageSource.setCacheSeconds(10);
		return messageSource;
	}

	/**
	 * FetchType.LAZY로 설정된 경우 500에러 발생함 -> 아래와 같이 messageConverter 설정 변경으로 처리 가능.
	 * 주요 내용은
	 *     new Hibernate5Module()
	 *     SerializationFeature.FAIL_ON_EMPTY_BEANS, false
	 *
	 *     ref : https://lalwr.blogspot.com/2018/04/java-23spring-boot-http-message.html
	 *
	 * issue : 이렇게 적용했더니 카테고리 쪽 children 데이터가 잘 안나옴.
	 *     -> 처음 FetchType.LAZY 문제가 아니고 참조 OBJect가 null 인 경우의 문제였음.
	 *     -> 다시 정확히 확인해 볼것.
	 *
	 * @param converters
	 */
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		// http://jsonobject.tistory.com/235 iso8601 형식으로 날짜 정보 출력하기
//		ObjectMapper mapper = Jackson2ObjectMapperBuilder.json().featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).modules(new JavaTimeModule()).build();
//		mapper.registerModule(new Hibernate5Module());
//
//		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
//		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(mapper);
//		converter.setPrettyPrint(true);
//
//		converters.add(converter);
	}

//	@Bean
//	public CodeMapper codeMapper() {
//		CodeMapper codeMapper = new CodeMapper();
//	
//		codeMapper.put(Code.USER_TYPE, UserType.class);
//		codeMapper.put(Code.ROLE_TYPE, RoleType.class);
//		codeMapper.put(Code.COMPANY_TYPE, CompanyType.class);
//		codeMapper.put(Code.EQUIPMENT_STATUS, EquipmentStatus.class);
//		codeMapper.put(Code.MAINTENANCE_STATUS, MaintenanceStatus.class);
//	
//		codeMapper.put(Code.SIZE_UNIT, SizeUnit.class);
//		codeMapper.put(Code.WEIGHT_UNIT, WeightUnit.class);
//		codeMapper.put(Code.RUNNER_TYPE, RunnerType.class);
//		codeMapper.put(Code.TOOLING_CONDITION, ToolingCondition.class);
//	
//		return codeMapper;
//	}

}
