package com.github.crimscon.autoconfigure.springdoc.model;


import com.github.crimscon.autoconfigure.springdoc.generator.JaxRsEquivalentGenerator;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;

import javax.ws.rs.*;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;

import static javax.ws.rs.HttpMethod.*;

public class JaxRsHandlerMethod extends HandlerMethod {
    private static final Set<String> REQUIRED_REQUEST_BODY_METHODS = Set.of(POST, PUT, PATCH);

    private MethodParameter[] methodParameters;

    public JaxRsHandlerMethod(String beanName, BeanFactory beanFactory, Method method) {
        super(beanName, beanFactory, method);
    }

    @Override
    @NonNull
    public MethodParameter[] getMethodParameters() {
        if (this.methodParameters == null) {
            this.methodParameters = JaxRsEquivalentGenerator
                    .inMethodParameters(super.getMethodParameters())
                    .equivalent(HeaderParam.class, RequestHeader.class)
                    .equivalent(QueryParam.class, RequestParam.class)
                    .equivalent(FormParam.class, RequestParam.class)
                    .equivalent(PathParam.class, PathVariable.class)
                    .equivalent(MatrixParam.class, MatrixVariable.class)
                    .equivalent(CookieParam.class, CookieValue.class)
                    .additionalForEmptyMethodParameter(RequestBody.class, this::conditionToRequestBody)
                    .generate();
        }

        return this.methodParameters;
    }

    private boolean conditionToRequestBody(MethodParameter methodParameter) {
        return Optional.ofNullable(methodParameter.getMethod())
                .map(method -> AnnotationUtils.findAnnotation(method, HttpMethod.class))
                .map(AnnotationUtils::getValue)
                .map(String.class::cast)
                .filter(REQUIRED_REQUEST_BODY_METHODS::contains)
                .isPresent();
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
