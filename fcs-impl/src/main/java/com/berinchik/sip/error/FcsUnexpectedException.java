package com.berinchik.sip.error;

/**
 * Created by Maksim on 27.05.2017.
 */
public class FcsUnexpectedException extends IllegalArgumentException {
    public FcsUnexpectedException(String s) {
        super(s);
    }
}
