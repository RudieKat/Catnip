package com.eveningoutpost.dexdrip.utils.validation;

public class Ensure {
    public static void notNull(Object value) {
        if (value==null) {
            throw new IllegalArgumentException("Argument sent was null");
        }
    }
}
