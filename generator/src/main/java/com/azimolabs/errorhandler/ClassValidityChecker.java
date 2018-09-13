package com.azimolabs.errorhandler;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class ClassValidityChecker {

    public void checkValidClass(HandleErrorAnnotatedClass item) throws ProcessingException {
        TypeElement classElement = item.getTypeElement();

        if (classElement.getModifiers().contains(Modifier.PRIVATE))
            throw new ProcessingException(classElement, "The class %s is visible, at least default visible is required",
                    classElement.getQualifiedName().toString());

        if (classElement.getModifiers().contains(Modifier.FINAL))
            throw new ProcessingException(classElement,
                    "The class %s is final. You can't annotate final classes with @%",
                    classElement.getQualifiedName().toString(), AutoHandler.class.getSimpleName());
    }

}
