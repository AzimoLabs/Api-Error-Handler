package com.azimolabs.errorhandler.example;

import com.azimolabs.errorhandler.AutoHandler;
import com.azimolabs.errorhandler.DefaultError;
import com.azimolabs.errorhandler.ErrorCode;
import com.azimolabs.errorhandler.ErrorField;

import java.util.Map;

@AutoHandler
public interface FieldsErrorListener {

    @ErrorCode(code = "404")
    void oneFieldError(@ErrorField("levelOne. levelTwo") String errorMessage);

    @ErrorCode(codes = {"404", "422"})
    void multipleFieldsError(@ErrorField("one.two, other.other2.other3") String errorMessage);

    @ErrorCode(code = "425", clientError = true)
    void unexpected(@ErrorField("one.two") String errorMessage);

    @ErrorCode(code = "408", clientError = true)
    void clientError();

    @ErrorCode(value = "ccc", passErrors = true)
    void dynamic(Map<String, String> errors);

    @DefaultError
    void defaultError();
}
