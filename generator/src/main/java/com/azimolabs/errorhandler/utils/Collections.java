package com.azimolabs.errorhandler.utils;

import java.util.Collection;

public class Collections {

    public static boolean notEmpty(Collection collection) {
        return collection != null && !collection.isEmpty();
    }

    public static boolean notEmpty(Object[] array) {
        return array != null && array.length != 0;
    }
}
