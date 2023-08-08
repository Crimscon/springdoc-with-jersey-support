package com.github.crimscon.autoconfigure.springdoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.crimscon.autoconfigure.springdoc.config.SpringDocJaxRsConfig;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springdoc.core.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(classes = SpringDocJaxRsConfig.class,
        properties = {
                "springdoc.jax-rs.enabled=false",
                "spring.jersey.application-path=/jaxrs"
        })
class SpringDocWithDisabledJaxRsIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Value(Constants.API_DOCS_URL)
    private String apiDocsUrl;

    @Value("${spring.jersey.application-path}")
    private String jerseyAppPath;

    @Test
    void shouldReturnPathsWithoutJaxRsBecauseScanJaxRsEndpointsIsDisabled() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get(apiDocsUrl)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("OpenAPI definition"))
                .andExpect(jsonPath("$.paths./mvc/test/message.get.tags[0]").value("message-controller"))
                .andExpect(jsonPath("$.paths." + jerseyAppPath + "/message/{id}").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.paths." + jerseyAppPath + "/message/filter").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.paths." + jerseyAppPath + "/message").doesNotHaveJsonPath())
                .andReturn();

        OpenAPI openAPI = mapper.readValue(mvcResult.getResponse().getContentAsString(), OpenAPI.class);

        assertEquals(1, openAPI.getPaths().size());
    }

}
