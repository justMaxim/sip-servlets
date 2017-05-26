package com.berinchik.sip.config;

import com.berinchik.sip.config.rule.RuleSet;
import com.berinchik.sip.config.target.Target;

import java.util.List;

/**
 * Created by Maksim on 24.05.2017.
 */
public interface ServiceConfig {
    List<Target> getTargetList();
    int getDefaultPeriod();
    int getNoReplyTimer();
    RuleSet getRuleSet();
    String getTargetAddressByName(String name);
}
