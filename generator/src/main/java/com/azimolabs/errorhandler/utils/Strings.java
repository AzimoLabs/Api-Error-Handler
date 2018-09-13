package com.azimolabs.errorhandler.utils;

/**
 * Created by Dominik Barwacz <dombar1@gmail.com> on 16/04/2018.
 */
public class Strings {
    public static String variableName(Class<?> clazz) {
        String name = clazz.getSimpleName();

        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }
}
