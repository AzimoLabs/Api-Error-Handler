package com.azimolabs.errorhandler.example;

import com.azimolabs.errorhandler.AutoHandler;
import com.azimolabs.errorhandler.ErrorCode;

@AutoHandler
public interface SimplerErrorListener {

    @ErrorCode("422")
    void userError();

    @ErrorCode(codes = {"500", "501", "503"})
    void multiple();

}
