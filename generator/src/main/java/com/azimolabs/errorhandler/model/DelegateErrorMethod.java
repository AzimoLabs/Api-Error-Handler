package com.azimolabs.errorhandler.model;

import com.azimolabs.errorhandler.ErrorField;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

public class DelegateErrorMethod {

    public final String methodName;
    public final List<ErrorParameter> parameters;
    public final Element methodElement;

    public DelegateErrorMethod(Element methodElement) {
        this.methodElement = methodElement;
        this.methodName = methodElement.getSimpleName().toString();
        List<? extends VariableElement> parameters = ((ExecutableElement) methodElement).getParameters();
        this.parameters = new ArrayList<>();
        for (VariableElement parameter : parameters) {
            ErrorField errorParamAnnotation = parameter.getAnnotation(ErrorField.class);
            this.parameters.add(new ErrorParameter(errorParamAnnotation));
        }
    }
}
