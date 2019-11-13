package com.tcs.internal.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Configuration class for Swagger
 *
 * @author Neeraj Sharma
 */
@PropertySource("classpath:swagger.properties")
@Configuration
@EnableSwagger2
public class SwaggerConfig extends WebMvcConfigurationSupport {

    /**
     * Swagger plugin
     *
     * @return Docket
     */
    @Bean
    public Docket swaggerPlugin() {
        return new Docket(DocumentationType.SWAGGER_2).useDefaultResponseMessages(false)
            .apiInfo(apiEndPointsInfo())
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.tcs.internal.userservice.controller"))
            .paths(PathSelectors.regex("(?!/healthcheck.html)/.*"))
            .build();
    }

    /**
     * Adds the resource handlers
     *
     * @param registry The resource handler registry
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    /**
     * Constructs the API endpoints info
     *
     * @return ApiInfo
     */
    private ApiInfo apiEndPointsInfo() {
        return new ApiInfoBuilder().title("User Service REST API")
            .description("API helps to manage the users")
            .version("1.0")
            .build();
    }
}
