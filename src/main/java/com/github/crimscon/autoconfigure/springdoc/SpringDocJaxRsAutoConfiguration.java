package com.github.crimscon.autoconfigure.springdoc;

import com.github.crimscon.autoconfigure.springdoc.converter.JaxRsClassToBeanConverter;
import com.github.crimscon.autoconfigure.springdoc.customizer.JaxRsOpenAPIBuilderCustomizer;
import com.github.crimscon.autoconfigure.springdoc.processor.JaxRsMethodProcessor;
import com.github.crimscon.autoconfigure.springdoc.processor.JaxRsResourceProcessor;
import com.github.crimscon.autoconfigure.springdoc.properties.JaxRsConstants;
import com.github.crimscon.autoconfigure.springdoc.properties.JaxRsProperties;
import com.github.crimscon.autoconfigure.springdoc.provider.JaxRsWebProvider;
import org.springdoc.core.SpringDocConfiguration;
import org.springdoc.core.customizers.OpenApiBuilderCustomizer;
import org.springdoc.core.providers.SpringWebProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jersey.JerseyAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.ws.rs.core.Application;
import java.util.List;

@Configuration
@ConditionalOnBean(Application.class)
@ConditionalOnProperty(name = JaxRsConstants.SPRINGDOC_JAXRS_ENABLED, matchIfMissing = true)
@AutoConfigureAfter({SpringDocConfiguration.class, JerseyAutoConfiguration.class})
@EnableConfigurationProperties(JaxRsProperties.class)
public class SpringDocJaxRsAutoConfiguration {

    @Value("${spring.jersey.application-path:}")
    private String applicationPath;

    @Bean
    @Primary
    SpringWebProvider springWebProvider(
            List<Application> applications,
            JaxRsClassToBeanConverter converter,
            ApplicationContext context
    ) {
        return new JaxRsWebProvider(applications, jaxRsResourceProcessor(context, converter), converter);
    }

    @Bean
    JaxRsResourceProcessor jaxRsResourceProcessor(ApplicationContext context, JaxRsClassToBeanConverter converter) {
        return new JaxRsResourceProcessor(jaxRsMethodProcessor(context), converter);
    }

    @Bean
    JaxRsMethodProcessor jaxRsMethodProcessor(ApplicationContext context) {
        return new JaxRsMethodProcessor(((ConfigurableApplicationContext) context).getBeanFactory(), applicationPath);
    }

    @Bean
    JaxRsClassToBeanConverter jaxRsClassToBeanConverter(ApplicationContext context) {
        return new JaxRsClassToBeanConverter(context);
    }

    @Bean
    OpenApiBuilderCustomizer jaxRsOpenApiBuilderCustomizer() {
        return new JaxRsOpenAPIBuilderCustomizer();
    }
}
