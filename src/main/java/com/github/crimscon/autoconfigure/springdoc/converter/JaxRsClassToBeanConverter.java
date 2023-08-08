package com.github.crimscon.autoconfigure.springdoc.converter;

import com.github.crimscon.autoconfigure.springdoc.model.JaxRsBeanDescription;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import java.util.stream.Stream;

public class JaxRsClassToBeanConverter {
    private final ApplicationContext context;

    public JaxRsClassToBeanConverter(ApplicationContext context) {
        this.context = context;
    }

    public Stream<JaxRsBeanDescription> convert(Class<?> beanClass) {
        return Stream.of(context.getBeanNamesForType(beanClass))
                .map(beanName -> {
                    var beanDescription = new JaxRsBeanDescription(beanName, context.getBean(beanName));

                    beanDescription.setProduces(AnnotationUtils.findAnnotation(beanDescription.getInstanceClass(), Produces.class));
                    beanDescription.setConsumes(AnnotationUtils.findAnnotation(beanDescription.getInstanceClass(), Consumes.class));

                    return beanDescription;
                });
    }
}
