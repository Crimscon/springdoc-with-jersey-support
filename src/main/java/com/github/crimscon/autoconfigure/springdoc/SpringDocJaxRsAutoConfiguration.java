package com.github.crimscon.autoconfigure.springdoc;

import com.github.crimscon.autoconfigure.springdoc.customizer.JaxRsOpenAPIBuilderCustomizer;
import com.github.crimscon.autoconfigure.springdoc.customizer.JaxRsOperationCustomizer;
import com.github.crimscon.autoconfigure.springdoc.properties.JaxRsConstants;
import com.github.crimscon.autoconfigure.springdoc.properties.JaxRsProperties;
import com.github.crimscon.autoconfigure.springdoc.provider.JaxRsWebProvider;

import org.springdoc.core.OpenAPIService;
import org.springdoc.core.SpringDocConfiguration;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springdoc.core.customizers.OpenApiBuilderCustomizer;
import org.springdoc.core.providers.SpringWebProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jersey.JerseyAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.ws.rs.core.Application;

@Configuration
@ConditionalOnBean(Application.class)
@ConditionalOnProperty(name = JaxRsConstants.SPRINGDOC_JAXRS_ENABLED, matchIfMissing = true)
@AutoConfigureAfter({SpringDocConfiguration.class, JerseyAutoConfiguration.class})
@EnableConfigurationProperties(JaxRsProperties.class)
public class SpringDocJaxRsAutoConfiguration {

    @Bean
    @Primary
    SpringWebProvider springWebProvider(Application application) {
        return new JaxRsWebProvider(application);
    }


    @Bean
    OpenApiBuilderCustomizer jaxRsOpenApiBuilderCustomizer() {
        return new JaxRsOpenAPIBuilderCustomizer();
    }

    @Bean
    GlobalOperationCustomizer jaxRsGlobalOperationCustomizer(OpenAPIService openAPIService) {
        return new JaxRsOperationCustomizer(openAPIService);
    }
}
