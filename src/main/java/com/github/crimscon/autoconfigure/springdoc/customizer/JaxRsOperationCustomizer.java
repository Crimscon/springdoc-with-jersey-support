package com.github.crimscon.autoconfigure.springdoc.customizer;

import com.github.crimscon.autoconfigure.springdoc.model.JaxRsHandlerMethod;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import org.springdoc.core.OpenAPIService;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springframework.web.method.HandlerMethod;

import java.util.Locale;

public class JaxRsOperationCustomizer implements GlobalOperationCustomizer {
    private final OpenAPIService openAPIService;

    public JaxRsOperationCustomizer(OpenAPIService openAPIService) {
        this.openAPIService = openAPIService;
    }

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        if (handlerMethod instanceof JaxRsHandlerMethod method && method.isProxyEnabled()) {
            operation.getTags().clear();

            method.disableProxy();
            openAPIService.buildTags(method, operation, new OpenAPI(), Locale.getDefault());
            method.enableProxy();
        }

        return operation;
    }
}
