package com.berinchik.sip.config;

import com.berinchik.sip.config.rule.RuleSet;
import com.berinchik.sip.config.target.ActionTarget;
import com.berinchik.sip.config.target.ServiceTarget;

import java.util.List;

/**
 * Created by Maksim on 24.05.2017.
 */
public interface ServiceConfig {
    public static final String SC_SERVICE_NAME = "flexible-communication";
    public static final String SC_SERVICE_SETTINGS = "service-settings";
    public static final String SC_SERVICE_ACTIVE = "active";
    public static final String SC_SERVICE_NO_REPLY_TIME = "not-reachable-timer";
    public static final String SC_SERVICE_DEFAULT_PERIOD = "default-period";
    public static final String SC_SERVICE_TARGET_LIST = "target-list";


    public static final String SC_TARGET_NAME = "name";
    public static final String SC_TARGET_ADDRESS = "id";

    public static final String SC_RULE_SET = "rule-set";
    public static final String SC_RULE_CUSTOM_NAME = "id";
    public static final String SC_RULE_CONDITIONS = "conditions";
    public static final String SC_RULE_ACTION_SET = "action-set";

    public static final String SC_CONDITION_TYPE = "condition";

    public static final String SC_ACTION_TYPE = "action-type";
    public static final String SC_ACTION_PARALLEL = "parallel-ringing";
    public static final String SC_ACTION_SERIAL = "serial-ringing";
    public static final String SC_ACTION_PERIOD = "period";
    public static final String SC_ACTION_TARGETS = "targets";
    public static final String SC_ACTION_TARGET_NAME = "target";
    public static final String SC_ACTION_TARGET_PERIOD = "period";

    List<ServiceTarget> getTargetList();
    int getDefaultPeriod();
    int getNotReachableTimer();
    RuleSet getRuleSet();
    String getTargetAddressByName(String name);
    List<String> getTargetAddresses(List<ActionTarget> targets);
}
