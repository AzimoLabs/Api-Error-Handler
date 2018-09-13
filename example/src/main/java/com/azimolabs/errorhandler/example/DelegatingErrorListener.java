package com.azimolabs.errorhandler.example;

import com.azimolabs.errorhandler.AutoHandler;
import com.azimolabs.errorhandler.DefaultError;
import com.azimolabs.errorhandler.ErrorCode;
import com.azimolabs.errorhandler.ErrorField;

@AutoHandler(delegates = {"422", "404"})
public interface DelegatingErrorListener {

    @ErrorCode("401")
    void userNameError(@ErrorField("one") String errorMessage);

    @ErrorCode(code = "422", clientError = true)
    void userError();

    @DefaultError(unexpected = false)
    void defaultError();
}
