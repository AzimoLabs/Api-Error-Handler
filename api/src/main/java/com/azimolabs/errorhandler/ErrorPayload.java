package com.azimolabs.errorhandler;

import java.util.Map;

/**
 * Model of body of failed request
 */
public interface ErrorPayload {

    /**
     * Custom code error body should contain.
     */
    String code();

    /**
     * Identifier of request,
     * useful to track errors with owners of API.
     * Will be logged in case of unexpected responses.
     */
    String requestId();

    /**
     * Developer-friendly error description returned from API.
     */
    String message();

    /**
     * Errors payload stored in form {@code <name, payload>}
     */
    Map<String, Object> errors();
}
