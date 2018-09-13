package com.azimolabs.errorhandler.model;

import com.azimolabs.errorhandler.ErrorField;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Dominik Barwacz <dombar1@gmail.com> on 16/04/2018.
 */
public class ErrorParameter {

    public final List<String> errorFields;
    public final boolean clientError;

    public ErrorParameter(ErrorField value) {
        this.errorFields = Arrays.asList(value.value().replaceAll("\\s+", "").split(","));
        this.clientError = value.clientError();
    }
}
