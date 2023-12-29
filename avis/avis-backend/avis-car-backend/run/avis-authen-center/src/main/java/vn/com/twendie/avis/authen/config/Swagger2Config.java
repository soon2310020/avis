package vn.com.twendie.avis.authen.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class Swagger2Config {

    @Bean
    public Docket api() {
        List<Parameter> parameters = new ArrayList<Parameter>() {{
            add(new ParameterBuilder()
                    .name("Authorization")
                    .modelRef(new ModelRef("string"))
                    .parameterType("header")
                    .defaultValue("token")
                    .required(true)
                    .build());
            add(new ParameterBuilder()
                    .name("Accept-Language")
                    .modelRef(new ModelRef("string"))
                    .parameterType("header")
                    .defaultValue("vi")
                    .required(true)
                    .build());
        }};
        return new Docket(DocumentationType.SWAGGER_2).select()
                .apis(RequestHandlerSelectors.basePackage("vn.com.twendie.avis.authen.controller"))
                .paths(PathSelectors.any())
                .build()
                .pathMapping("")
                .globalOperationParameters(parameters);
    }

}
