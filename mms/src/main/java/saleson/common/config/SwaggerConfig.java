package saleson.common.config;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.springframework.context.annotation.*;

import springfox.documentation.RequestHandler;
import springfox.documentation.builders.*;
import springfox.documentation.spi.*;
import springfox.documentation.spring.web.plugins.*;
import springfox.documentation.swagger2.annotations.*;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableSwagger2
@Profile({"developer","local","local2"})
public class SwaggerConfig {

	@Bean
	public Docket api() {
//		return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.any()).paths(PathSelectors.any()).build();
		List<Predicate<RequestHandler>> predicateList = new ArrayList<>();
		predicateList.add(RequestHandlerSelectors.basePackage("com.emoldino.api"));
		predicateList.add(RequestHandlerSelectors.basePackage("saleson.api.imageupload"));
		Docket docket = new Docket(DocumentationType.SWAGGER_2).select().apis(Predicates.or(predicateList)).paths(PathSelectors.ant("/**")).build();
		docket.apiInfo(new ApiInfoBuilder().title("eMoldino Total Solution's API Documentation")
				.description("eMoldino Total Solution Provides API for Backend Supply Chain Management.").version("0.0.1-SNAPSHOT").build());
		return docket;
	}

}
