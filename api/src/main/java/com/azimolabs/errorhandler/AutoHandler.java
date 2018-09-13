package com.azimolabs.errorhandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks interface as part of error handling.
 * Generated handler will attempt to only call methods
 * additionally annotated with {@link ErrorCode}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface AutoHandler {

    String GENERATED_CLASS_SUFFIX = "Handler";

    /**
     * Whether this method is a delegate which means that only thing supported is mapping
     * error fields to methods.
     * <p>
     * Delegate is most useful when you need to write custom, complex
     * handling code and want to 'inject' it for single error code and have there rest
     * generated as usual.
     */
    boolean delegated() default false;

    /**
     * Alias of {@link #delegates()} for cases with one argument
     */
    String delegate() default "";

    /**
     * Error codes this handler should pass on to injectable object.
     * Using this field will add additional dependency on generated
     * {@link AutoHandler}: {@code Map<Integer,} {@link DelegatedErrorHandler}>
     * <p>
     * Delegate is most useful when you need to write custom, complex
     * handling code and want to 'inject' it for single error code and have there rest
     * generated as usual.
     */
    String[] delegates() default "";

    /**
     * When set to true, first delegate that returns true will end error handling
     */
    boolean delegateEndsHandling() default false;

    /**
     * How the generated handler class should be named,
     * annotated class name + {@value #GENERATED_CLASS_SUFFIX} by default.
     */
    String generatedClassName() default "";
}
