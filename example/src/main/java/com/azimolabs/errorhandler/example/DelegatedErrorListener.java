package com.azimolabs.errorhandler.example;

import com.azimolabs.errorhandler.AutoHandler;
import com.azimolabs.errorhandler.ErrorField;

@AutoHandler(delegated = true)
public interface DelegatedErrorListener {

    void fieldsError(@ErrorField("one.two,other.other2.other3") String errorMessage);
}
