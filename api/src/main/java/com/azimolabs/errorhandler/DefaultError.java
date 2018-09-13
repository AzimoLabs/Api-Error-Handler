package com.azimolabs.errorhandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method that is not related to any specific error but allows to track
 * whether error codes with multiple possible error fields were actually consumed.
 * (there was at least one error field found for given parameter)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface DefaultError {
    /**
     * Marks that specific situation should never happen,
     * when set to true, will force logging of request id
     * <p>
     * Useful when error code serves other, specific purpose or
     * so it happens that you encounter more fields than you expected.
     */
    boolean unexpected() default true;

    /**
     * When set to true, will force method annotated with this class
     * to be called after every successful handling
     * <p>
     * Useful when you have method 'finishValidation' or similar.
     */
    boolean callPostHandling() default false;
}
