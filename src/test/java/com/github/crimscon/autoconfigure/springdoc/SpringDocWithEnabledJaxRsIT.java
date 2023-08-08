package com.github.crimscon.autoconfigure.springdoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.crimscon.autoconfigure.springdoc.config.SpringDocJaxRsConfig;
import com.github.crimscon.autoconfigure.springdoc.model.Message;
import com.github.crimscon.autoconfigure.springdoc.model.MessageFilter;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.junit.jupiter.api.Test;
import org.springdoc.core.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(classes = SpringDocJaxRsConfig.class,
        properties = "spring.jersey.application-path=/jaxrs")
class SpringDocWithEnabledJaxRsIT {

    private static final Consumer<Schema<?>> CHECK_MESSAGE = (schema) -> {
        assertNull(schema.getType());
        assertEquals("#/components/schemas/" + Message.class.getSimpleName(), schema.get$ref());
    };

    private static final Consumer<Schema<?>> CHECK_MESSAGE_FILTER = (schema) -> {
        assertNull(schema.getType());
        assertEquals("#/components/schemas/" + MessageFilter.class.getSimpleName(), schema.get$ref());
    };

    private static final Consumer<Schema<?>> CHECK_TYPE = (schema) -> {
        assertEquals("string", schema.getType());
        assertNull(schema.get$ref());
    };

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Value(Constants.API_DOCS_URL)
    private String apiDocsUrl;

    @Value("${spring.jersey.application-path}")
    private String jerseyAppPath;

    @Test
    void shouldReturnPathsWithJaxRsBecauseScanJaxRsEndpointsIsEnabled() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                        get(apiDocsUrl).accept(APPLICATION_JSON_VALUE)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("OpenAPI definition"))
                .andExpect(jsonPath("$.paths." + jerseyAppPath + "/message/{id}").hasJsonPath())
                .andExpect(jsonPath("$.paths." + jerseyAppPath + "/message/filter").hasJsonPath())
                .andExpect(jsonPath("$.paths." + jerseyAppPath + "/message").hasJsonPath())
                .andReturn();

        OpenAPI openAPI = mapper.readValue(mvcResult.getResponse().getContentAsString(), OpenAPI.class);
        assertEquals(4, openAPI.getPaths().size());

        PathItem pathItemWithId = openAPI.getPaths().get(jerseyAppPath + "/message/{id}");
        checkMessageGet(pathItemWithId.getGet());
        checkMessagePut(pathItemWithId.getPut());
        checkMessageDelete(pathItemWithId.getDelete());

        PathItem pathItemJustMessage = openAPI.getPaths().get(jerseyAppPath + "/message");
        checkMessagePost(pathItemJustMessage.getPost());

        PathItem pathItemWithFilter = openAPI.getPaths().get(jerseyAppPath + "/message/filter");
        checkMessageFilter(pathItemWithFilter.getPost());
    }

    private void checkMessageGet(Operation operation) {
        // operation
        assertNotNull(operation);
        assertTrue(operation.getTags().contains("crud-message-sub-resource"));

        // parameters
        List<Parameter> parameters = operation.getParameters();
        assertEquals(1, parameters.size());

        Parameter parameter = parameters.get(0);
        assertEquals("id", parameter.getName());
        assertEquals("path", parameter.getIn());
        assertTrue(parameter.getRequired());

        // request body
        assertNull(operation.getRequestBody());

        // response
        ApiResponses responses = operation.getResponses();
        assertEquals(1, responses.size());

        ApiResponse response = responses.get("200");
        assertNotNull(response);
        assertNull(response.get$ref());

        Content content = response.getContent();
        checkContent(content, APPLICATION_JSON_VALUE, CHECK_MESSAGE);
    }

    private void checkMessagePut(Operation operation) {
        // operation
        assertNotNull(operation);
        assertTrue(operation.getTags().contains("crud-message-sub-resource"));

        // parameters
        List<Parameter> parameters = operation.getParameters();
        assertEquals(1, parameters.size());

        Parameter parameter = parameters.get(0);
        assertEquals("id", parameter.getName());
        assertEquals("path", parameter.getIn());
        assertTrue(parameter.getRequired());
        CHECK_TYPE.accept(parameter.getSchema());

        // request body
        RequestBody requestBody = operation.getRequestBody();
        assertNull(requestBody.get$ref());
        assertTrue(requestBody.getRequired());
        checkContent(requestBody.getContent(), APPLICATION_JSON_VALUE, CHECK_MESSAGE);

        // response
        ApiResponses responses = operation.getResponses();
        assertEquals(1, responses.size());

        ApiResponse response = responses.get("200");
        assertNotNull(response);
        assertNull(response.get$ref());

        Content content = response.getContent();
        checkContent(content, APPLICATION_JSON_VALUE, CHECK_MESSAGE);
    }

    private void checkMessageDelete(Operation operation) {
        // operation
        assertNotNull(operation);
        assertTrue(operation.getTags().contains("crud-message-sub-resource"));

        // parameters
        List<Parameter> parameters = operation.getParameters();
        assertEquals(1, parameters.size());

        Parameter parameter = parameters.get(0);
        assertEquals("id", parameter.getName());
        assertEquals("path", parameter.getIn());
        assertTrue(parameter.getRequired());
        assertEquals("string", parameter.getSchema().getType());

        // request body
        assertNull(operation.getRequestBody());

        // response
        ApiResponses responses = operation.getResponses();
        assertEquals(1, responses.size());

        ApiResponse response = responses.get("200");
        assertNotNull(response);
        assertNull(response.get$ref());
        assertNull(response.getContent());
    }

    private void checkMessagePost(Operation operation) {
        // operation
        assertNotNull(operation);
        assertTrue(operation.getTags().contains("crud-message-sub-resource"));

        // parameters
        assertNull(operation.getParameters());

        // request body
        RequestBody requestBody = operation.getRequestBody();
        assertNull(requestBody.get$ref());
        assertTrue(requestBody.getRequired());
        checkContent(requestBody.getContent(), APPLICATION_JSON_VALUE, CHECK_MESSAGE);

        // response
        ApiResponses responses = operation.getResponses();
        assertEquals(1, responses.size());

        ApiResponse response = responses.get("200");
        assertNotNull(response);
        assertNull(response.get$ref());

        Content content = response.getContent();
        checkContent(content, TEXT_HTML_VALUE, CHECK_TYPE);
    }

    private void checkMessageFilter(Operation operation) {
        // operation
        assertNotNull(operation);
        assertTrue(operation.getTags().contains("message-resource"));

        // parameters
        assertNull(operation.getParameters());

        // request body
        RequestBody requestBody = operation.getRequestBody();
        assertNull(requestBody.get$ref());
        assertTrue(requestBody.getRequired());
        checkContent(requestBody.getContent(), APPLICATION_JSON_VALUE, CHECK_MESSAGE_FILTER);

        // response
        ApiResponses responses = operation.getResponses();
        assertEquals(1, responses.size());

        ApiResponse response = responses.get("200");
        assertNotNull(response);
        assertNull(response.get$ref());

        Content content = response.getContent();
        checkContent(content, APPLICATION_JSON_VALUE, schema -> {
            assertEquals("array", schema.getType());
            assertNull(schema.get$ref());
            CHECK_MESSAGE.accept(schema.getItems());
        });
    }

    private void checkContent(Content content, String typeValue, Consumer<Schema<?>> checkContentType) {
        assertEquals(1, content.size());

        MediaType mediaType = content.get(typeValue);
        assertNotNull(mediaType);
        checkContentType.accept(mediaType.getSchema());
    }
}
