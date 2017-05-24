package com.berinchik.sip.config;

import javax.servlet.sip.Address;

/**
 * Created by Maksim on 24.05.2017.
 */
public interface Target {
    public Address getAdderss();
    public int getRingingPeriod();
}
