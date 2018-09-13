package com.azimolabs.errorhandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method annotated with {@link ErrorCode} can either be called without arguments,
 * or be passed data returned in request. This annotation specifies name of fields
 * to extract.
 * <p>
 * Note: method itself will only be called when error fields are non-empty.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface ErrorField {

    /**
     * Error field position
     * <p>
     * '.' is supported if errors are nested.
     * <p>
     * ',' is supported if multiple fields should be matched to one method.
     */
    String value();

    /**
     * This shouldn't happen, error is on client.
     * Similar to {@link ErrorCode#clientError()} but for specific fields only.
     */
    boolean clientError() default false;
}