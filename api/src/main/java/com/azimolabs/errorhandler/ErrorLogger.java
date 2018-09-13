package com.azimolabs.errorhandler;

/**
 * Your error logger needs to implement this
 */
public interface ErrorLogger {

    void logException(Throwable error);
}
