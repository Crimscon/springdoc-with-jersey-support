package com.github.crimscon.autoconfigure.springdoc.provider;

import com.github.crimscon.autoconfigure.springdoc.generator.JaxRsResponseBodyGenerator;
import com.github.crimscon.autoconfigure.springdoc.model.JaxRsHandlerMethod;

import org.springdoc.webmvc.core.SpringWebMvcProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.accept.MappingMediaTypeFileExtensionResolver;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.util.pattern.PathPatternParser;

import java.lang.annotation.Annotation;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.ws.rs.Consumes;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;

public class JaxRsWebProvider extends SpringWebMvcProvider {

    private final Application application;

    private Map<RequestMappingInfo, HandlerMethod> handlerMethods;

    @Value("${spring.jersey.application-path:}")
    private String applicationPath;

    public JaxRsWebProvider(@NonNull Application application) {
        this.application = application;
    }

    public Map<RequestMappingInfo, HandlerMethod> getHandlerMethods() {
        if (handlerMethods == null) {
            this.handlerMethods = initHandlerMethods();
        }
        return this.handlerMethods;
    }

    public Map<RequestMappingInfo, HandlerMethod> initHandlerMethods() {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = new LinkedHashMap<>(super.getHandlerMethods());
        ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
        RequestMappingInfo.BuilderConfiguration options = buildOptions();

        getRegisteredJaxRsClasses()
                .flatMap(beanClass -> Stream.of(applicationContext.getBeanNamesForType(beanClass)))
                .map(beanName -> Map.entry(beanName, applicationContext.getBean(beanName)))
                .forEach(entry -> {
                    Class<?> resourceClass = entry.getValue().getClass();
                    Class<?> resourceClassWithResponseBody = new JaxRsResponseBodyGenerator(entry.getValue()).generate();
                    String resourcePath = getPath(resourceClass);

                    Produces produces = AnnotationUtils.findAnnotation(resourceClass, Produces.class);
                    Consumes consumes = AnnotationUtils.findAnnotation(resourceClass, Consumes.class);

                    for (Method method : ReflectionUtils.getDeclaredMethods(resourceClass)) {
                        Optional.ofNullable(AnnotationUtils.findAnnotation(method, HttpMethod.class))
                                .ifPresent(httpMethod -> {
                                    var handlerMethod = createHandlerMethod(
                                            beanFactory, options, entry.getKey(),
                                            resourceClassWithResponseBody, resourcePath,
                                            produces, consumes, method, httpMethod
                                    );
                                    handlerMethods.put(handlerMethod.getKey(), handlerMethod.getValue());
                                });
                    }
                });

        return handlerMethods;
    }

    private Stream<Class<?>> getRegisteredJaxRsClasses() {
        return Stream.concat(
                Objects.requireNonNull(application)
                        .getSingletons()
                        .stream()
                        .map(Object::getClass),
                Objects.requireNonNull(application)
                        .getClasses()
                        .stream()
        );
    }

    private Map.Entry<RequestMappingInfo, JaxRsHandlerMethod> createHandlerMethod(
            ConfigurableListableBeanFactory beanFactory,
            RequestMappingInfo.BuilderConfiguration options,
            String beanName,
            Class<?> resourceClassWithResponseBody,
            String resourcePath,
            Produces produces,
            Consumes consumes,
            Method method,
            HttpMethod httpMethod
    ) {
        var mappingInfo = RequestMappingInfo.paths(computeFinalPath(resourcePath, method))
                .methods(RequestMethod.valueOf(Objects.requireNonNull(httpMethod).value()))
                .produces(getOverrideAnnotationValue(produces, Produces.class, method))
                .consumes(getOverrideAnnotationValue(consumes, Consumes.class, method))
                .options(options)
                .build();
        var handlerMethod = new JaxRsHandlerMethod(beanName, beanFactory, method, resourceClassWithResponseBody);

        return Map.entry(mappingInfo, handlerMethod);
    }

    private String computeFinalPath(String resourcePath, Method method) {
        String path = (applicationPath + "/" + resourcePath + "/" + getPath(method)).replaceAll("/+", "/");
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    private RequestMappingInfo.BuilderConfiguration buildOptions() {
        RequestMappingInfo.BuilderConfiguration configuration = new RequestMappingInfo.BuilderConfiguration();
        configuration.setPatternParser(new PathPatternParser());

        ContentNegotiationManager contentNegotiationManager = new ContentNegotiationManager();
        MappingMediaTypeFileExtensionResolver resolver = new MappingMediaTypeFileExtensionResolver(
                Map.of(
                        "xml", MediaType.APPLICATION_XML,
                        "json", MediaType.APPLICATION_JSON
                )
        );
        contentNegotiationManager.addFileExtensionResolvers(resolver);

        configuration.setContentNegotiationManager(contentNegotiationManager);
        return configuration;
    }

    private String getPath(GenericDeclaration declaration) {
        Path annotation = AnnotationUtils.findAnnotation(declaration, Path.class);
        return annotation != null ? annotation.value() : "";
    }

    private <A extends Annotation> String[] getOverrideAnnotationValue(A resourceAnnotation, Class<A> annotationClass, Method method) {
        Annotation annotation = AnnotationUtils.findAnnotation(method, annotationClass);

        return Optional.ofNullable(
                        (String[]) AnnotationUtils.getValue(
                                Optional.ofNullable(annotation).orElse(resourceAnnotation)
                        )
                )
                .orElse(new String[0]);
    }
}
