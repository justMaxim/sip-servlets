package com.berinchik.sip.error;

/**
 * Created by Maksim on 26.05.2017.
 */
public class ServiceInactiveException extends RuntimeException {
    public ServiceInactiveException(String s) {
        super(s);
    }
}
