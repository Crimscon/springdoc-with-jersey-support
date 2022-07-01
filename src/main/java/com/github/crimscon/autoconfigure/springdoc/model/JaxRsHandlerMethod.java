package com.github.crimscon.autoconfigure.springdoc.model;

import com.github.crimscon.autoconfigure.springdoc.generator.JaxRsEquivalentGenerator;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

public class JaxRsHandlerMethod extends HandlerMethod {
    private final Class<?> proxyBeanType;

    private MethodParameter[] methodParameters;

    private boolean proxyEnabled;

    @Override
    @NonNull
    public Class<?> getBeanType() {
        return proxyBeanType != null && isProxyEnabled() ? proxyBeanType : super.getBeanType();
    }

    public JaxRsHandlerMethod(String beanName, BeanFactory beanFactory, Method method, Class<?> resourceClassWithResponseBody) {
        super(beanName, beanFactory, method);
        this.proxyBeanType = resourceClassWithResponseBody;
        this.proxyEnabled = true;
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
                    .generate();
        }

        return this.methodParameters;
    }

    public boolean isProxyEnabled() {
        return proxyEnabled;
    }

    public void disableProxy() {
        this.proxyEnabled = false;
    }

    public void enableProxy() {
        this.proxyEnabled = true;
    }
}
