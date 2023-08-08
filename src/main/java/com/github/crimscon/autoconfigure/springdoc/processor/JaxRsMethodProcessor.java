package com.github.crimscon.autoconfigure.springdoc.processor;

import com.github.crimscon.autoconfigure.springdoc.model.JaxRsBeanDescription;
import com.github.crimscon.autoconfigure.springdoc.model.JaxRsHandlerMethod;
import org.springdoc.core.SpringDocUtils;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.http.MediaType;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.accept.MappingMediaTypeFileExtensionResolver;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.util.pattern.PathPatternParser;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import java.lang.reflect.Method;
import java.util.Map;

public class JaxRsMethodProcessor {
    private static final SpringDocUtils SPRING_DOC_UTILS = SpringDocUtils.getConfig();

    private final RequestMappingInfo.BuilderConfiguration options = buildOptions();
    private final ConfigurableListableBeanFactory beanFactory;
    private final String applicationPath;

    public JaxRsMethodProcessor(ConfigurableListableBeanFactory beanFactory, String applicationPath) {
        this.beanFactory = beanFactory;
        this.applicationPath = applicationPath;
    }

    private static RequestMappingInfo.BuilderConfiguration buildOptions() {
        var configuration = new RequestMappingInfo.BuilderConfiguration();
        configuration.setPatternParser(new PathPatternParser());

        var contentNegotiationManager = new ContentNegotiationManager();
        var resolver = new MappingMediaTypeFileExtensionResolver(
                Map.of(
                        "xml", MediaType.APPLICATION_XML,
                        "json", MediaType.APPLICATION_JSON
                )
        );
        contentNegotiationManager.addFileExtensionResolvers(resolver);

        configuration.setContentNegotiationManager(contentNegotiationManager);
        return configuration;
    }

    public void processMethod(
            JaxRsBeanDescription beanDescription,
            Method endpoint,
            RequestMethod requestMethod,
            Map<RequestMappingInfo, HandlerMethod> handlerMethods
    ) {
        var handlerMethod = createHandlerMethod(beanDescription, endpoint, requestMethod);
        handlerMethods.put(handlerMethod.getKey(), handlerMethod.getValue());

        SPRING_DOC_UTILS.addRestControllers(beanDescription.getInstanceClass());
    }

    private Map.Entry<RequestMappingInfo, JaxRsHandlerMethod> createHandlerMethod(
            JaxRsBeanDescription beanDescription,
            Method endpoint,
            RequestMethod requestMethod
    ) {
        var mappingInfo =
                RequestMappingInfo.paths(JaxRsProcessorUtils.computeFinalPath(applicationPath, beanDescription.getResourcePath(), endpoint))
                        .methods(requestMethod)
                        .produces(JaxRsProcessorUtils.getOverrideAnnotationValue(beanDescription.getProduces(), Produces.class, endpoint))
                        .consumes(JaxRsProcessorUtils.getOverrideAnnotationValue(beanDescription.getConsumes(), Consumes.class, endpoint))
                        .options(options)
                        .build();
        var handlerMethod = new JaxRsHandlerMethod(beanDescription.getName(), beanFactory, endpoint);

        return Map.entry(mappingInfo, handlerMethod);
    }
}
