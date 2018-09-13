package com.azimolabs.errorhandler.example;

import com.azimolabs.errorhandler.AutoHandler;
import com.azimolabs.errorhandler.ErrorCode;

import java.util.Map;

@AutoHandler
public interface ErrorEnabledView {

    @ErrorCode(codes = {"11", "22"}, passErrors = true)
    void passErrors(Map<String, String> string);

    Map<String, String> getApiErrors();

    void removeApiError(String fieldName);

}
