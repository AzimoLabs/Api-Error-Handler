package com.azimolabs.errorhandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks method as part of error handling
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface ErrorCode {

    /**
     * Convenience for {@link #codes()} with one argument
     */
    String value() default "";

    /**
     * Convenience for {@link #codes()} with one argument
     */
    String code() default "";

    /**
     * List of error codes this method should be allowed to attempt to handle
     */
    String[] codes() default {};

    /**
     * Whether this error should never happen (ie. client error triggered it)
     * This invocation will be tracked together with {@link ErrorPayload#requestId()}.
     */
    boolean clientError() default false;

    /**
     * Sometimes the list of possible error fields is dynamic - you can't
     * specify full list.
     * In that case set this to true and instead of matching errors code with
     * specific fields, you will be given {@code Map<String, Object>} with flattened errors}
     * <p>
     * When method has this set to true, it can have only one parameter whose type is
     * {@code Map<String, Object>}
     * <p>
     * Any other type of argument, or additional arguments, are incorrect and not supported.
     */
    boolean passErrors() default false;
}
