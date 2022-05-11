package com.eveningoutpost.dexdrip.utils.validation;

public class StringTools {

    public static boolean isEmpty(String s) {
        Ensure.notNull(s);
        return s.length()>0;
    }
}
