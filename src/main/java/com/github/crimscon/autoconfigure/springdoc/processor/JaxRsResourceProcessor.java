package com.github.crimscon.autoconfigure.springdoc.processor;

import com.github.crimscon.autoconfigure.springdoc.converter.JaxRsClassToBeanConverter;
import com.github.crimscon.autoconfigure.springdoc.model.JaxRsBeanDescription;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import java.lang.reflect.Method;
import java.util.Map;

public class JaxRsResourceProcessor {
    private final JaxRsClassToBeanConverter converter;
    private final JaxRsMethodProcessor processor;

    public JaxRsResourceProcessor(JaxRsMethodProcessor methodProcessor, JaxRsClassToBeanConverter converter) {
        this.processor = methodProcessor;
        this.converter = converter;
    }

    public void processResource(
            JaxRsBeanDescription beanDescription,
            Map<RequestMappingInfo, HandlerMethod> handlerMethods
    ) {
        if (beanDescription.getResourcePath() == null) {
            beanDescription.setResourcePath(JaxRsProcessorUtils.getClassPath(beanDescription.getInstanceClass()));
        }

        for (Method method : ReflectionUtils.getAllDeclaredMethods(beanDescription.getInstanceClass())) {
            JaxRsProcessorUtils.findAnnotation(method, HttpMethod.class)
                    .map(annotation -> RequestMethod.valueOf(annotation.value()))
                    .ifPresentOrElse(
                            requestMethod -> processor.processMethod(beanDescription, method, requestMethod, handlerMethods),
                            () -> processSubResource(beanDescription, method, handlerMethods));
        }
    }

    private void processSubResource(
            JaxRsBeanDescription parent,
            Method linkToSubResource,
            Map<RequestMappingInfo, HandlerMethod> handlerMethods
    ) {
        JaxRsProcessorUtils.findAnnotation(linkToSubResource, Path.class)
                .stream()
                .flatMap(annotation -> converter.convert(linkToSubResource.getReturnType()))
                .forEach(beanDescription -> {
                    beanDescription.fillAnnotationsFromParentIfNeeded(parent);
                    beanDescription.setResourcePath(JaxRsProcessorUtils.getSubResourcePath(parent.getResourcePath(), linkToSubResource));
                    processResource(beanDescription, handlerMethods);
                });
    }

}
