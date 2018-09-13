package com.azimolabs.errorhandler;

import javax.lang.model.element.TypeElement;

public class HandleErrorAnnotatedClass {

    private final String annotatedClassName;

    private TypeElement annotatedClassElement;

    public HandleErrorAnnotatedClass(TypeElement classElement) throws ProcessingException {
        this.annotatedClassElement = classElement;
        this.annotatedClassName = classElement.getQualifiedName().toString();
    }

    public String getId() {
        return getAnnotatedClassName();
    }

    public TypeElement getTypeElement() {
        return annotatedClassElement;
    }

    public String getAnnotatedClassName() {
        return annotatedClassName;
    }
}
