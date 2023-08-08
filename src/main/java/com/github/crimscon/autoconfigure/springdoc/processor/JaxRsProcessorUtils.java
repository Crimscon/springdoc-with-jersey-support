package com.github.crimscon.autoconfigure.springdoc.processor;

import org.springframework.core.annotation.AnnotationUtils;

import javax.ws.rs.Path;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

class JaxRsProcessorUtils {

    private JaxRsProcessorUtils() {
    }

    public static String getClassPath(Class<?> resourceClass) {
        return findAnnotation(resourceClass, Path.class)
                .map(Path::value)
                .orElse("");
    }

    private static String getMethodPath(Method method) {
        return findAnnotation(method, Path.class)
                .map(Path::value)
                .orElse("");
    }

    public static String getSubResourcePath(String parentResourcePath, Method subResourcePath) {
        return computePath(parentResourcePath, getMethodPath(subResourcePath));
    }

    public static String computeFinalPath(String applicationPath, String resourcePath, Method method) {
        return computePath(applicationPath, resourcePath, getMethodPath(method));
    }

    private static String computePath(String... components) {
        var path = String.join("/", components).replaceAll("/+", "/");
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    public static <A extends Annotation> String[] getOverrideAnnotationValue(
            A resourceAnnotation,
            Class<A> annotationClass,
            Method method
    ) {
        return findAnnotation(method, annotationClass)
                .or(() -> Optional.ofNullable(resourceAnnotation))
                .map(AnnotationUtils::getValue)
                .map(String[].class::cast)
                .orElse(new String[0]);
    }

    public static <A extends Annotation> Optional<A> findAnnotation(Method method, Class<A> annotationClass) {
        return Optional.ofNullable(AnnotationUtils.findAnnotation(method, annotationClass));
    }

    public static <A extends Annotation> Optional<A> findAnnotation(Class<?> resource, Class<A> annotationClass) {
        return Optional.ofNullable(AnnotationUtils.findAnnotation(resource, annotationClass));
    }

}
