package com.github.crimscon.autoconfigure.springdoc.provider;

import com.github.crimscon.autoconfigure.springdoc.converter.JaxRsClassToBeanConverter;
import com.github.crimscon.autoconfigure.springdoc.processor.JaxRsResourceProcessor;
import org.springdoc.core.SpringDocUtils;
import org.springdoc.webmvc.core.SpringWebMvcProvider;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Stream;

public class JaxRsWebProvider extends SpringWebMvcProvider {
    private static final SpringDocUtils SPRING_DOC_UTILS = SpringDocUtils.getConfig();

    private final List<Application> applications;
    private final JaxRsResourceProcessor processor;
    private final JaxRsClassToBeanConverter converter;

    private Map<RequestMappingInfo, HandlerMethod> handlerMethodsWithJaxRs;

    public JaxRsWebProvider(
            List<Application> applications,
            JaxRsResourceProcessor processor,
            JaxRsClassToBeanConverter converter
    ) {
        this.applications = Optional.ofNullable(applications).orElse(new ArrayList<>());
        this.processor = processor;
        this.converter = converter;

        SPRING_DOC_UTILS.addResponseTypeToIgnore(Response.class);
        SPRING_DOC_UTILS.addAnnotationsToIgnore(Context.class);
    }

    @Override
    public Map<RequestMappingInfo, HandlerMethod> getHandlerMethods() {
        if (handlerMethodsWithJaxRs == null) {
            //noinspection rawtypes,unchecked
            this.handlerMethodsWithJaxRs = new HashMap<>(super.getHandlerMethods());

            Stream.concat(
                            applications
                                    .stream()
                                    .flatMap(application -> application.getSingletons().stream())
                                    .map(Object::getClass),
                            applications
                                    .stream()
                                    .flatMap(application -> application.getClasses().stream())
                    )
                    .distinct()
                    .flatMap(converter::convert)
                    .forEach(beanDescription -> processor.processResource(beanDescription, this.handlerMethodsWithJaxRs));
        }

        return this.handlerMethodsWithJaxRs;
    }
}
