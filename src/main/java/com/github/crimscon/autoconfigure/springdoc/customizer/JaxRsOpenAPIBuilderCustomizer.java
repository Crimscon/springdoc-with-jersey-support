package com.github.crimscon.autoconfigure.springdoc.customizer;

import org.springdoc.core.OpenAPIService;
import org.springdoc.core.customizers.OpenApiBuilderCustomizer;

import javax.ws.rs.Path;

public class JaxRsOpenAPIBuilderCustomizer implements OpenApiBuilderCustomizer {

    @Override
    public void customise(OpenAPIService openApiService) {
        openApiService.getMappingsMap().putAll(openApiService.getContext().getBeansWithAnnotation(Path.class));
    }

}
