package com.github.crimscon.autoconfigure.springdoc.processor;

import com.github.crimscon.autoconfigure.springdoc.model.JaxRsBeanDescription;
import com.github.crimscon.autoconfigure.springdoc.model.JaxRsHandlerMethod;
import com.github.crimscon.autoconfigure.springdoc.model.Message;
import com.github.crimscon.autoconfigure.springdoc.resource.CrudMessageSubResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.MethodParameter;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.util.pattern.PathPattern;

import javax.ws.rs.PathParam;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
class JaxRsMethodProcessorTest {

    private static final CrudMessageSubResource RESOURCE = new CrudMessageSubResource();
    private final Map<RequestMappingInfo, HandlerMethod> handlerMethods = new HashMap<>();
    private JaxRsMethodProcessor methodProcessor;
    private ConfigurableListableBeanFactory beanFactory;
    private String applicationPath;

    @BeforeEach
    void setUp() {
        this.beanFactory = mock(ConfigurableListableBeanFactory.class);
        this.applicationPath = "/jaxrs";

        this.methodProcessor = new JaxRsMethodProcessor(
                this.beanFactory,
                this.applicationPath
        );
    }

    @Test
    void shouldCheckGetMethodAndAddCorrectHandlerMethodToMap() throws NoSuchMethodException {
        // given
        JaxRsBeanDescription beanDescription = getJaxRsBeanDescription();
        Method endpoint = beanDescription.getInstanceClass().getMethod("getMessage", String.class);
        RequestMethod requestMethod = RequestMethod.GET;

        // when
        methodProcessor.processMethod(beanDescription, endpoint, requestMethod, handlerMethods);

        // then
        assertEquals(1, handlerMethods.size());

        var handlerMethodEntry = handlerMethods.entrySet().stream().findFirst().orElse(null);
        assertNotNull(handlerMethodEntry);

        checkPath(handlerMethodEntry.getKey(), applicationPath + "/message/{id}");
        checkMethod(handlerMethodEntry.getKey(), requestMethod);
        checkHandlerMethod(handlerMethodEntry.getValue(), "id", List.of(PathVariable.class, PathParam.class));
    }

    @Test
    void shouldCheckPostMethodAndAddCorrectHandlerMethodToMap() throws NoSuchMethodException {
        // given
        JaxRsBeanDescription beanDescription = getJaxRsBeanDescription();
        Method endpoint = beanDescription.getInstanceClass().getMethod("createMessage", Model.class, Message.class);
        RequestMethod requestMethod = RequestMethod.POST;

        // when
        methodProcessor.processMethod(beanDescription, endpoint, requestMethod, handlerMethods);

        // then
        assertEquals(1, handlerMethods.size());

        var handlerMethodEntry = handlerMethods.entrySet().stream().findFirst().orElse(null);
        assertNotNull(handlerMethodEntry);

        checkPath(handlerMethodEntry.getKey(), applicationPath + "/message");
        checkMethod(handlerMethodEntry.getKey(), requestMethod);
        checkHandlerMethod(handlerMethodEntry.getValue(), "message", List.of(RequestBody.class));
    }

    @Test
    void shouldCheckPutMethodAndAddCorrectHandlerMethodToMap() throws NoSuchMethodException {
        // given
        JaxRsBeanDescription beanDescription = getJaxRsBeanDescription();
        Method endpoint = beanDescription.getInstanceClass().getMethod("updateMessage", String.class, Message.class);
        RequestMethod requestMethod = RequestMethod.PUT;

        // when
        methodProcessor.processMethod(beanDescription, endpoint, requestMethod, handlerMethods);

        // then
        assertEquals(1, handlerMethods.size());

        var handlerMethodEntry = handlerMethods.entrySet().stream().findFirst().orElse(null);
        assertNotNull(handlerMethodEntry);

        checkPath(handlerMethodEntry.getKey(), applicationPath + "/message/{id}");
        checkMethod(handlerMethodEntry.getKey(), requestMethod);
        checkHandlerMethod(handlerMethodEntry.getValue(), "id", List.of(PathVariable.class, PathParam.class));
        checkHandlerMethod(handlerMethodEntry.getValue(), "message", List.of(RequestBody.class));
    }

    @Test
    void shouldCheckDeleteMethodAndAddCorrectHandlerMethodToMap() throws NoSuchMethodException {
        // given
        JaxRsBeanDescription beanDescription = getJaxRsBeanDescription();
        Method endpoint = beanDescription.getInstanceClass().getMethod("deleteMessage", String.class);
        RequestMethod requestMethod = RequestMethod.DELETE;

        // when
        methodProcessor.processMethod(beanDescription, endpoint, requestMethod, handlerMethods);

        // then
        assertEquals(1, handlerMethods.size());

        var handlerMethodEntry = handlerMethods.entrySet().stream().findFirst().orElse(null);
        assertNotNull(handlerMethodEntry);

        checkPath(handlerMethodEntry.getKey(), applicationPath + "/message/{id}");
        checkMethod(handlerMethodEntry.getKey(), requestMethod);
        checkHandlerMethod(handlerMethodEntry.getValue(), "id", List.of(PathVariable.class, PathParam.class));
    }

    private JaxRsBeanDescription getJaxRsBeanDescription() {
        Class<?> resourceClass = RESOURCE.getClass();
        String resourceName = resourceClass.getSimpleName();
        doReturn(resourceClass).when(beanFactory).getType(resourceName);

        var beanDescription = new JaxRsBeanDescription(resourceName, RESOURCE);
        beanDescription.setResourcePath("/message");

        return beanDescription;
    }

    private void checkHandlerMethod(
            HandlerMethod handlerMethod,
            String fieldNameToCheck,
            List<Class<? extends Annotation>> annotationContains
    ) {
        assertInstanceOf(JaxRsHandlerMethod.class, handlerMethod);

        MethodParameter[] methodParameters = handlerMethod.getMethodParameters();

        assertTrue(
                Arrays.stream(methodParameters)
                        .filter(methodParameter -> methodParameter.getParameter().getName().equals(fieldNameToCheck))
                        .flatMap(methodParameter -> Arrays.stream(methodParameter.getParameterAnnotations()))
                        .allMatch(annotation -> annotationContains.contains(annotation.annotationType()))
        );
    }

    private void checkMethod(RequestMappingInfo requestMappingInfo, RequestMethod expectedMethod) {
        RequestMethod methodCondition = Optional.of(requestMappingInfo.getMethodsCondition())
                .map(RequestMethodsRequestCondition::getMethods)
                .stream()
                .flatMap(Collection::stream)
                .findFirst()
                .orElse(null);
        assertNotNull(methodCondition);
        assertEquals(expectedMethod, methodCondition);
    }

    private void checkPath(RequestMappingInfo requestMappingInfo, String expectedPath) {
        PathPattern pathPattern = Optional.ofNullable(requestMappingInfo.getPathPatternsCondition())
                .map(PathPatternsRequestCondition::getPatterns)
                .stream()
                .flatMap(Collection::stream)
                .findFirst()
                .orElse(null);
        assertNotNull(pathPattern);
        assertEquals(expectedPath, pathPattern.getPatternString());
    }
}
