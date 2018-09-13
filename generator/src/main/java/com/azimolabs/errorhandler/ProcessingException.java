package com.azimolabs.errorhandler;

import javax.lang.model.element.Element;

public class ProcessingException extends RuntimeException {

    Element element;

    public ProcessingException(Element element, String msg, Object... args) {
        super(String.format(msg, args));
        this.element = element;
    }

    public Element getElement() {
        return element;
    }
}