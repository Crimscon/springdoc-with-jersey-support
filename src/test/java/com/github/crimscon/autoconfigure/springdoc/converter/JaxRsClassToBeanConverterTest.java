package com.github.crimscon.autoconfigure.springdoc.converter;

import com.github.crimscon.autoconfigure.springdoc.model.JaxRsBeanDescription;
import com.github.crimscon.autoconfigure.springdoc.resource.CrudMessageSubResource;
import com.github.crimscon.autoconfigure.springdoc.resource.MessageResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class JaxRsClassToBeanConverterTest {

    private static final Class<?> TEST_CLASS = MessageResource.class;
    private final MessageResource mockBean = new MessageResource(mock(CrudMessageSubResource.class));

    @InjectMocks
    private JaxRsClassToBeanConverter converter;
    @Mock
    private ApplicationContext context;

    @Test
    void shouldReturnEmptyStreamBecauseBeansNotFoundByClass() {
        // given
        when(context.getBeanNamesForType(TEST_CLASS)).thenReturn(new String[0]);

        // when
        Stream<JaxRsBeanDescription> beanDescriptionStream = converter.convert(TEST_CLASS);

        // then
        assertEquals(0, beanDescriptionStream.count());
        verify(context).getBeanNamesForType(TEST_CLASS);
        verify(context, times(0)).getBean(anyString());
    }

    @Test
    void shouldFindResourceAndConstructBeanDescription() {
        // given
        when(context.getBeanNamesForType(TEST_CLASS)).thenReturn(new String[]{TEST_CLASS.getName()});
        when(context.getBean(TEST_CLASS.getName())).thenReturn(mockBean);

        // when
        List<JaxRsBeanDescription> beanDescriptions = converter.convert(TEST_CLASS).toList();

        // then
        assertEquals(1, beanDescriptions.size());
        JaxRsBeanDescription beanDescription = beanDescriptions.get(0);
        assertEquals(TEST_CLASS.getName(), beanDescription.getName());
        assertEquals(TEST_CLASS, beanDescription.getInstanceClass());
        assertArrayEquals(new String[]{MediaType.APPLICATION_JSON}, beanDescription.getProduces().value());
        assertArrayEquals(new String[]{MediaType.APPLICATION_JSON}, beanDescription.getConsumes().value());

        verify(context).getBeanNamesForType(TEST_CLASS);
        verify(context).getBean(TEST_CLASS.getName());
    }

}
