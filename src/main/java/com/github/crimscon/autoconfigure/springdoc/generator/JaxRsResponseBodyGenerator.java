package com.github.crimscon.autoconfigure.springdoc.generator;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.web.bind.annotation.ResponseBody;

public class JaxRsResponseBodyGenerator {
    private final Object beanNeedsSupport;

    public JaxRsResponseBodyGenerator(Object beanNeedsSupport) {
        this.beanNeedsSupport = beanNeedsSupport;
    }

    public Class<?> generate() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(beanNeedsSupport.getClass());

        Class<?>[] interfaces = ArrayUtils.add(beanNeedsSupport.getClass().getInterfaces(), JaxRsResponseBodySupporter.class);
        enhancer.setInterfaces(interfaces);

        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> proxy.invokeSuper(obj, args));

        return enhancer.create().getClass();
    }

    @ResponseBody
    public interface JaxRsResponseBodySupporter {
    }
}
