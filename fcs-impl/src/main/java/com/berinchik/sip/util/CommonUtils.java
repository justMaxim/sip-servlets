package com.berinchik.sip.util;

/**
 * Created by Maksim on 26.05.2017.
 */
public class CommonUtils {
    private CommonUtils() { }

    public static long getCurrentTimestampInSeconds() {
        return System.currentTimeMillis() / 1000;
    }
}
