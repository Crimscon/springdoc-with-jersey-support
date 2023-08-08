package com.github.crimscon.autoconfigure.springdoc.generator;

import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import javax.ws.rs.DefaultValue;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public class JaxRsEquivalentGenerator {

    private JaxRsEquivalentGenerator() {
    }

    public static Generator inMethodParameters(MethodParameter[] methodParameters) {
        return new Generator(methodParameters);
    }

    public static class Generator {
        private static final String EXIST_ANNOTATION_FIELD_NAME = "combinedAnnotations";

        private final MethodParameter[] methodParameters;
        private final Map<Class<? extends Annotation>, Class<? extends Annotation>> equivalences = new HashMap<>();
        private final Map<Class<? extends Annotation>, Predicate<MethodParameter>> conditionalAdditional = new HashMap<>();

        public Generator(MethodParameter[] methodParameters) {
            this.methodParameters = methodParameters;
        }

        public Generator equivalent(Class<? extends Annotation> jerseyAnnotation, Class<? extends Annotation> springAnnotation) {
            equivalences.put(jerseyAnnotation, springAnnotation);
            return this;
        }

        public Generator additionalForEmptyMethodParameter(Class<? extends Annotation> annotationClass, Predicate<MethodParameter> condition) {
            conditionalAdditional.put(annotationClass, condition);
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
            Set<Annotation> annotations = new HashSet<>(additionalAnnotations(methodParameter));

            for (Annotation existAnnotation : existAnnotations) {
                annotations.add(existAnnotation);
                if (isContainsInSimilarAnnotationsMap(existAnnotation)) {
                    var synthesizeAnnotation = synthesizeAnnotation(methodParameter, existAnnotation);
                    annotations.add(synthesizeAnnotation);
                }
            }

            return annotations.toArray(Annotation[]::new);
        }

        private Set<Annotation> additionalAnnotations(MethodParameter methodParameter) {
            boolean isEmptyParameter = equivalences
                    .keySet()
                    .stream()
                    .noneMatch(methodParameter::hasParameterAnnotation);

            if (isEmptyParameter) {
                Set<Annotation> addedAnnotation = new HashSet<>();

                conditionalAdditional
                        .forEach((additionalAnnotation, condition) -> {
                            if (condition.test(methodParameter)) {
                                addedAnnotation.add(
                                        AnnotationUtils.synthesizeAnnotation(Map.of(), additionalAnnotation, methodParameter.getParameter())
                                );
                            }
                        });

                return addedAnnotation;
            }

            return Set.of();
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
            return equivalences.entrySet()
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
