package com.berinchik.sip.config;

import com.berinchik.sip.error.ServiceInactiveException;
import com.berinchik.sip.config.rule.FcsServiceRuleSet;
import com.berinchik.sip.config.rule.RuleSet;
import com.berinchik.sip.config.target.ActionTarget;
import com.berinchik.sip.config.target.FscServiceTarget;
import com.berinchik.sip.config.target.ServiceTarget;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by Maksim on 24.05.2017.
 */
public class FcsServiceConfig implements ServiceConfig {

    private static Log logger = LogFactory.getLog(FcsServiceConfig.class);



    private JSONObject jsonConfig;
    private List<ServiceTarget> serviceTargets;
    private Map<String, ServiceTarget> namesToTargetsMap;
    private int defaultPeriod = 8;
    private int defaultNoReply = 8;
    private RuleSet ruleSet;

    public FcsServiceConfig(JSONObject config) {
        jsonConfig = config.getJSONObject(SC_SERVICE_SETTINGS);

        if (!jsonConfig.getBoolean(SC_SERVICE_ACTIVE)) {
            throw new ServiceInactiveException("Service is inactive for this user");
        }

        logger.trace("Configs selected" + jsonConfig);

        namesToTargetsMap = new HashMap<>();
        defaultPeriod = jsonConfig.getInt(SC_SERVICE_DEFAULT_PERIOD);
        defaultNoReply = jsonConfig.getInt(SC_SERVICE_NO_REPLY_TIME);
        initialiseTargetsList();
        initialiseRuleSet();
    }

    private void initialiseTargetsList() {

        JSONArray targetsArray = jsonConfig.getJSONArray(SC_SERVICE_TARGET_LIST);
        serviceTargets = new ArrayList<>();

        logger.trace("Initialising targets");

        for(int i = 0; i < targetsArray.length(); ++i) {
            JSONObject targetJSONObject = targetsArray.getJSONObject(i);
            logger.trace("New target: " + targetJSONObject);
            ServiceTarget newTarget = new FscServiceTarget(targetJSONObject);
            this.serviceTargets.add(newTarget);
            this.namesToTargetsMap.put(newTarget.getName(), newTarget);
        }
    }

    private void initialiseRuleSet() {
        logger.trace("Initialising rule-set");
        ruleSet = new FcsServiceRuleSet(jsonConfig.getJSONArray(SC_RULE_SET));
    }

    @Override
    public List<ServiceTarget> getTargetList() {
        return this.serviceTargets;
    }

    @Override
    public String getTargetAddressByName(String name) {
        return this.namesToTargetsMap.get(name).getAdderss();
    }

    @Override
    public int getDefaultPeriod() {
        return defaultPeriod;
    }

    @Override
    public int getNotReachableTimer() {
        return defaultNoReply;
    }

    @Override
    public RuleSet getRuleSet() {
        return ruleSet;
    }

    @Override
    public List<String> getTargetAddresses(List<ActionTarget> targets) {
        List<String> targetAddresses = new ArrayList<>();

        for (ActionTarget target :
                targets) {
            targetAddresses.add(getTargetAddressByName(target.getName()));
        }
        return targetAddresses;
    }
}
