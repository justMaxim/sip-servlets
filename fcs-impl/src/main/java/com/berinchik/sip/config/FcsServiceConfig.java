package com.berinchik.sip.config;

import com.berinchik.sip.config.error.ServiceInactiveException;
import com.berinchik.sip.config.rule.Rule;
import com.berinchik.sip.config.rule.RuleSet;
import com.berinchik.sip.config.target.ServiceTarget;
import com.berinchik.sip.config.target.Target;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by Maksim on 24.05.2017.
 */
public class FcsServiceConfig implements ServiceConfig {

    public static final String SC_SERVICE_NAME = "flexible-communication";
    public static final String SC_SERVICE_ACTIVE = "active";
    public static final String SC_SERVICE_NO_REPLY_TIME = "no-reply-timer";
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
    public static final String SC_ACTION_PERIOD = "period";
    public static final String SC_ACTION_TARGETS = "targets";
    public static final String SC_ACTION_TARGET_NAME = "target";

    private JSONObject jsonConfig;
    private List<Target> serviceTargets;
    private Map<String, Target> namesToTargetsMap;
    private int defaultPeriod = 15;
    private int defaultNoReply = 15;

    private RuleSet ruleSet;

    public FcsServiceConfig(JSONObject config) {
        jsonConfig = config.getJSONObject(SC_SERVICE_NAME);

        if (!jsonConfig.getBoolean(SC_SERVICE_ACTIVE)) {
            throw new ServiceInactiveException("Service is inactive for this user");
        }

        namesToTargetsMap = new HashMap<>();
        defaultPeriod = jsonConfig.getInt(SC_SERVICE_DEFAULT_PERIOD);
        defaultNoReply = jsonConfig.getInt(SC_SERVICE_NO_REPLY_TIME);
        initialiseTargetsList();
        initialiseRuleSet();
    }

    private void initialiseTargetsList() {

        JSONArray targetsArray = jsonConfig.getJSONArray(SC_SERVICE_TARGET_LIST);
        serviceTargets = new ArrayList<>();

        for(int i = 0; i < targetsArray.length(); ++i) {
            JSONObject targetJSONObject = targetsArray.getJSONObject(i);
            Target newTarget = new ServiceTarget(targetJSONObject);
            this.serviceTargets.add(newTarget);
            this.namesToTargetsMap.put(newTarget.getName(), newTarget);
        }
    }

    private void initialiseRuleSet() {

    }

    @Override
    public List<Target> getTargetList() {
        return this.serviceTargets;
    }

    @Override
    public String getTargetAddressByName(String name) {
        return ((Target)this.namesToTargetsMap.get(name)).getAdderss();
    }

    @Override
    public int getDefaultPeriod() {
        return defaultPeriod;
    }

    @Override
    public int getNoReplyTimer() {
        return defaultNoReply;
    }

    @Override
    public RuleSet getRuleSet() {
        return ruleSet;
    }
}
