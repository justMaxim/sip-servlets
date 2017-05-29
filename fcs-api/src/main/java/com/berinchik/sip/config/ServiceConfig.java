package com.berinchik.sip.config;

import com.berinchik.sip.config.rule.RuleSet;
import com.berinchik.sip.config.target.ActionTarget;
import com.berinchik.sip.config.target.ServiceTarget;

import java.util.List;

/**
 * Created by Maksim on 24.05.2017.
 */
public interface ServiceConfig {
    List<ServiceTarget> getTargetList();
    int getDefaultPeriod();
    int getNotReachableTimer();
    RuleSet getRuleSet();
    String getTargetAddressByName(String name);
    List<String> getTargetAddresses(List<ActionTarget> targets);
}
