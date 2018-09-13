package com.azimolabs.errorhandler.model;

import com.azimolabs.errorhandler.ErrorCode;
import com.azimolabs.errorhandler.ErrorField;
import com.azimolabs.errorhandler.ProcessingException;
import com.azimolabs.errorhandler.utils.Collections;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

public class ErrorMethod {

    public final String methodName;
    public final boolean clientError;
    public final List<String> errorCodes;
    public final List<ErrorParameter> parameters;
    public final boolean passErrors;
    public final String dynamicParam;

    public ErrorMethod(Element methodElement) {
        ErrorCode annotation = methodElement.getAnnotation(ErrorCode.class);
        this.methodName = methodElement.getSimpleName().toString();
        this.clientError = annotation.clientError();
        this.errorCodes = new ArrayList<>();
        if (!Strings.isNullOrEmpty(annotation.code())) {
            this.errorCodes.add(annotation.code());
        } else if (!Strings.isNullOrEmpty(annotation.value())) {
            this.errorCodes.add(annotation.value());
        } else if (Collections.notEmpty(annotation.codes())) {
            this.errorCodes.addAll(Arrays.asList(annotation.codes()));
        }
        if (!Collections.notEmpty(errorCodes)) {
            throw new ProcessingException(methodElement, String.format("Method %s marked with @ErrorCode must have one of those specified: `value`, `code` or `codes`", methodName));
        }
        List<? extends VariableElement> parameters = ((ExecutableElement) methodElement).getParameters();
        this.passErrors = annotation.passErrors();
        this.parameters = new ArrayList<>();
        if (passErrors) {
            // TODO: use processingEnv.getTypeUtils().isSubtype(parameterType, eventType)
            if (parameters.size() != 1 || !parameters.get(0).asType().toString().equals("java.util.Map<java.lang.String,java.lang.String>")) {
                throw new ProcessingException(methodElement, String.format("Method %s marked with `passErrors=true` must have parameter of type Map<String, String>", methodName));
            }
            this.dynamicParam = "type";
        } else {
            this.dynamicParam = null;
            for (VariableElement parameter : parameters) {
                ErrorField errorParamAnnotation = parameter.getAnnotation(ErrorField.class);
                if (errorParamAnnotation == null) {
                    throw new ProcessingException(parameter, String.format("Parameter %s in method %s must be annotated with @ErrorField", parameter, methodName));
                } else {
                    ErrorParameter param = new ErrorParameter(errorParamAnnotation);
                    for (int i = 0; i < param.errorFields.size(); i++) {
                        String errorField = param.errorFields.get(i);
                        if (Strings.isNullOrEmpty(errorField)) {
                            throw new ProcessingException(parameter, String.format("Parameter named %s in method %s must have non-empty error field name specified (at index %s)", parameter, methodName, i));
                        }
                    }
                    this.parameters.add(param);
                }
            }
        }
    }
}
