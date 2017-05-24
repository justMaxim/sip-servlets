package com.berinchik.sip.config;

import java.util.List;

/**
 * Created by Maksim on 24.05.2017.
 */
public interface ServiceConfig {
    List<Target> getTargetList();
    int getDefaultPeriod();
    List<Rule> getRuleset();
}
