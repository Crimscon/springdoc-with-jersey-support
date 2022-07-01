package com.github.crimscon.autoconfigure.springdoc.generator;

import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import javax.ws.rs.DefaultValue;

public class JaxRsEquivalentGenerator {

    private JaxRsEquivalentGenerator() {
    }

    public static Generator inMethodParameters(MethodParameter[] methodParameters) {
        return new Generator(methodParameters);
    }

    public static class Generator {
        private static final String EXIST_ANNOTATION_FIELD_NAME = "combinedAnnotations";

        private final MethodParameter[] methodParameters;
        private final Map<Class<? extends Annotation>, Class<? extends Annotation>> equivalenceMap = new HashMap<>();

        public Generator(MethodParameter[] methodParameters) {
            this.methodParameters = methodParameters;
        }

        public Generator equivalent(Class<? extends Annotation> jerseyAnnotation, Class<? extends Annotation> springAnnotation) {
            equivalenceMap.put(jerseyAnnotation, springAnnotation);
            return this;
        }

        public MethodParameter[] generate() {
            for (MethodParameter methodParameter : methodParameters) {
                Optional.ofNullable(ReflectionUtils.findField(methodParameter.getClass(), EXIST_ANNOTATION_FIELD_NAME))
                        .ifPresent(existAnnotationsField -> {
                            ReflectionUtils.makeAccessible(existAnnotationsField);

                            Annotation[] withSimilarSpringAnnotations = fillAnnotationArray(
                                    methodParameter,
                                    methodParameter.getParameterAnnotations()
                            );

                            ReflectionUtils.setField(
                                    existAnnotationsField,
                                    methodParameter,
                                    withSimilarSpringAnnotations
                            );
                        });
            }

            return methodParameters;
        }

        private Annotation[] fillAnnotationArray(MethodParameter methodParameter, Annotation[] existAnnotations) {
            return Stream.concat(
                            Stream.of(existAnnotations)
                                    .filter(this::isContainsInSimilarAnnotationsMap)
                                    .map(annotation -> synthesizeAnnotation(methodParameter, annotation))
                                    .filter(Objects::nonNull),
                            Stream.of(existAnnotations))
                    .toArray(Annotation[]::new);
        }

        private Annotation synthesizeAnnotation(MethodParameter methodParameter, Annotation annotation) {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(AnnotationUtils.VALUE, AnnotationUtils.getValue(annotation));

            Optional.ofNullable(methodParameter.getParameterAnnotation(DefaultValue.class))
                    .ifPresent(defaultValue -> attributes.put("defaultValue", defaultValue.value()));

            AtomicReference<Annotation> equivalent = new AtomicReference<>();

            findSimilarAnnotationClass(annotation).ifPresent(replacementClass ->
                    equivalent.set(
                            AnnotationUtils.synthesizeAnnotation(attributes, replacementClass,
                                    methodParameter.getParameter())
                    )
            );

            return equivalent.get();
        }

        private Optional<? extends Class<? extends Annotation>> findSimilarAnnotationClass(Annotation annotation) {
            return equivalenceMap.entrySet()
                    .stream()
                    .filter(entry -> entry.getKey().isInstance(annotation))
                    .map(Map.Entry::getValue)
                    .findAny();
        }

        private boolean isContainsInSimilarAnnotationsMap(Annotation annotation) {
            return findSimilarAnnotationClass(annotation).isPresent();
        }
    }

}
