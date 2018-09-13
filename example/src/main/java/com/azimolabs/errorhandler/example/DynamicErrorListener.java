package com.azimolabs.errorhandler.example;

import com.azimolabs.errorhandler.AutoHandler;
import com.azimolabs.errorhandler.DefaultError;
import com.azimolabs.errorhandler.ErrorCode;
import com.azimolabs.errorhandler.ErrorField;

import java.util.Map;

@AutoHandler
public interface DynamicErrorListener {

    @ErrorCode(code = "11", passErrors = true)
    void passErrors(Map<String, String> string);

    @ErrorCode("other")
    void dynamicField(@ErrorField("field") String field);

    @DefaultError
    void defaultError();
}
