package com.azimolabs.errorhandler.model;

import com.azimolabs.errorhandler.AutoHandler;
import com.azimolabs.errorhandler.DefaultError;
import com.azimolabs.errorhandler.ErrorCode;
import com.azimolabs.errorhandler.ProcessingException;
import com.azimolabs.errorhandler.utils.Collections;
import com.google.common.base.Strings;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

/**
 * Created by Dominik Barwacz <dombar1@gmail.com> on 16/04/2018.
 */
public class ErrorHandler {
    public final Map<String, List<ErrorMethod>> methods;
    public final List<DelegateErrorMethod> delegatedMethods;
    public final List<String> delegatedCodes;
    @Nullable
    public final String defaultError;
    public final boolean defaultUnexpected;
    public final boolean defaultAlwaysCalled;
    public final boolean returnHandled;
    public final boolean handleAfterSuccessfulDelegate;
    public final boolean dynamicMethod;
    public final String requestedClassName;
    public final boolean isDelegate;

    public ErrorHandler(TypeElement typeElement) {
        List<? extends Element> elements = typeElement.getEnclosedElements();
        this.delegatedCodes = new ArrayList<>();
        AutoHandler annotation = typeElement.getAnnotation(AutoHandler.class);
        if (annotation != null) {
            if (!Strings.isNullOrEmpty(annotation.delegate())) {
                delegatedCodes.add(annotation.delegate());
            }
            if (Collections.notEmpty(annotation.delegates())) {
                delegatedCodes.addAll(Arrays.stream(annotation.delegates())
                        .filter(Objects::nonNull)
                        .filter(value -> value.length() > 0)
                        .collect(toList()));
            }
            this.isDelegate = annotation.delegated();
            this.handleAfterSuccessfulDelegate = annotation.delegateEndsHandling();
            this.requestedClassName = annotation.generatedClassName();
        } else {
            this.isDelegate = false;
            this.requestedClassName = "";
            this.handleAfterSuccessfulDelegate = false;
        }
        if (isDelegate) {
            delegatedMethods = typeElement.getEnclosedElements()
                    .stream()
                    .map(DelegateErrorMethod::new)
                    .collect(toList());
        } else {
            delegatedMethods = java.util.Collections.emptyList();
        }
        methods = elements
                .stream()
                .filter(element -> element.getKind() == ElementKind.METHOD)
                .filter(element -> element.getAnnotation(ErrorCode.class) != null)
                .map(ErrorMethod::new)
                .flatMap(item -> item.errorCodes.stream()
                        .map(itemCode -> new SimpleEntry<>(itemCode, item)))
                .collect(groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue, toList())));
        Optional<ErrorMethod> requiresDefault = methods.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(method -> Collections.notEmpty(method.parameters))
                .findFirst();
        Optional<ErrorMethod> dynamicMethodFound = methods.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(method -> method.passErrors)
                .findFirst();
        if (dynamicMethodFound.isPresent() && !requiresDefault.isPresent()) {
            Optional<DefaultError> defaultError = elements
                    .stream()
                    .filter(element -> element.getKind() == ElementKind.METHOD)
                    .filter(element -> element.getAnnotation(DefaultError.class) != null)
                    .map(element -> element.getAnnotation(DefaultError.class))
                    .findFirst();
            if (defaultError.isPresent()) {
                if (!defaultError.get().callPostHandling()) {
                    throw new ProcessingException(typeElement, "Dynamic method (one that passes errors directly as map) and "
                            + DefaultError.class.getSimpleName() + " doesn't make any sense together, unless default method is" +
                            " expected to be called at all times (callPostHandling = true)");
                }
            }
            this.defaultUnexpected = false;
            this.defaultAlwaysCalled = false;
            this.defaultError = null;
            this.returnHandled = false;
            this.dynamicMethod = true;
        } else if (requiresDefault.isPresent()) {
            Element defaultError = elements
                    .stream()
                    .filter(element -> element.getKind() == ElementKind.METHOD)
                    .filter(element -> element.getAnnotation(DefaultError.class) != null)
                    .findFirst()
                    .orElseThrow(() -> new ProcessingException(typeElement, "You need to annotate one argument-less method with " + DefaultError.class.getSimpleName()));
            this.defaultUnexpected = defaultError.getAnnotation(DefaultError.class).unexpected();
            this.defaultAlwaysCalled = defaultError.getAnnotation(DefaultError.class).callPostHandling();
            this.defaultError = defaultError.getSimpleName().toString();
            this.returnHandled = false;
            this.dynamicMethod = dynamicMethodFound.isPresent();
        } else {
            this.defaultError = null;
            this.defaultUnexpected = false;
            this.defaultAlwaysCalled = false;
            this.returnHandled = Collections.notEmpty(delegatedMethods);
            this.dynamicMethod = false;
        }
    }
}
