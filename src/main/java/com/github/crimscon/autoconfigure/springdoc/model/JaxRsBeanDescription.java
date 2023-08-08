package com.github.crimscon.autoconfigure.springdoc.model;

import org.springframework.aop.support.AopUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

public class JaxRsBeanDescription {
    private final String name;
    private final Class<?> instanceClass;

    private String resourcePath;
    private Produces produces;
    private Consumes consumes;

    public JaxRsBeanDescription(
            String name,
            Object instance
    ) {
        this.name = name;
        this.instanceClass = AopUtils.getTargetClass(instance);
    }

    public String getName() {
        return name;
    }

    public Class<?> getInstanceClass() {
        return instanceClass;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public Produces getProduces() {
        return produces;
    }

    public void setProduces(Produces produces) {
        this.produces = produces;
    }

    public Consumes getConsumes() {
        return consumes;
    }

    public void setConsumes(Consumes consumes) {
        this.consumes = consumes;
    }

    public void fillAnnotationsFromParentIfNeeded(JaxRsBeanDescription parentBeanDescription) {
        if (getConsumes() == null) {
            setConsumes(parentBeanDescription.getConsumes());
        }

        if (getProduces() == null) {
            setProduces(parentBeanDescription.getProduces());
        }
    }
}
