package com.berinchik.sip.config.error;

/**
 * Created by Maksim on 27.05.2017.
 */
public class FcsUnexpectedException extends IllegalArgumentException {
    public FcsUnexpectedException(String s) {
        super(s);
    }
}
