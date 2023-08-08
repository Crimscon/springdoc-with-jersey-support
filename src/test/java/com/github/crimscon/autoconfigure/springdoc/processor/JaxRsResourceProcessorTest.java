package com.github.crimscon.autoconfigure.springdoc.processor;

import com.github.crimscon.autoconfigure.springdoc.converter.JaxRsClassToBeanConverter;
import com.github.crimscon.autoconfigure.springdoc.model.JaxRsBeanDescription;
import com.github.crimscon.autoconfigure.springdoc.resource.CrudMessageSubResource;
import com.github.crimscon.autoconfigure.springdoc.resource.MessageResource;
import com.github.crimscon.autoconfigure.springdoc.resource.SuperMessageResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class JaxRsResourceProcessorTest {

    private final static Object TEST_SUBRESOURCE = new CrudMessageSubResource();
    private final static Object TEST_INSTANSE_WITH_SUBRESOURCE = new MessageResource((CrudMessageSubResource) TEST_SUBRESOURCE);
    private final static Object TEST_INSTANSE_WITHOUT_SUBRESOURCE = new SuperMessageResource();

    @InjectMocks
    private JaxRsResourceProcessor resourceProcessor;

    @Mock
    private JaxRsMethodProcessor methodProcessor;

    @Mock
    private JaxRsClassToBeanConverter converter;

    @Test
    void shouldCallOnlyMethodProcessorBecauseSubResourcesNotFound() {
        // given
        var beanDescription = new JaxRsBeanDescription(TEST_INSTANSE_WITHOUT_SUBRESOURCE.getClass().getSimpleName(), TEST_INSTANSE_WITHOUT_SUBRESOURCE);
        beanDescription.setResourcePath("/test/message/resource");
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = Map.of();

        // when
        resourceProcessor.processResource(beanDescription, handlerMethods);

        // then
        Method method = beanDescription.getInstanceClass().getDeclaredMethods()[0];
        verify(methodProcessor).processMethod(beanDescription, method, RequestMethod.POST, handlerMethods);
        verifyNoInteractions(converter);
    }

    @Test
    void shouldCallMethodProcessorWithConverterBecauseSubResourceExist() {
        // given
        var beanDescription = new JaxRsBeanDescription(TEST_INSTANSE_WITH_SUBRESOURCE.getClass().getSimpleName(), TEST_INSTANSE_WITH_SUBRESOURCE);
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = Map.of();

        when(converter.convert(TEST_SUBRESOURCE.getClass())).thenReturn(
                Stream.of(new JaxRsBeanDescription(TEST_SUBRESOURCE.getClass().getSimpleName(), TEST_SUBRESOURCE))
        );

        // when
        resourceProcessor.processResource(beanDescription, handlerMethods);

        // then
        verify(methodProcessor, times(5)).processMethod(any(), any(), any(), eq(handlerMethods));
        verify(converter).convert(TEST_SUBRESOURCE.getClass());
    }

}
