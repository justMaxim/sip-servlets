package com.berinchik.sip.config.rule;

import com.berinchik.sip.config.action.ActionSet;
import com.berinchik.sip.config.action.FcsActionSet;
import com.berinchik.sip.config.condition.Condition;
import static com.berinchik.sip.config.FcsServiceConfig.*;


import com.berinchik.sip.config.condition.FcsRuleCondition;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Maksim on 27.05.2017.
 */
public class FcsServiceRule implements Rule {

    private String ruleId;
    private List<Condition> conditionSet;
    private ActionSet actionSet;
    private JSONObject ruleJSONObject;
    private JSONArray conditionSetJSONArray;


    public FcsServiceRule(JSONObject ruleJSONObject) {
        this.ruleJSONObject = ruleJSONObject;
        ruleId = ruleJSONObject.getString(SC_RULE_CUSTOM_NAME);
    }

    private void initialiseActionSet() {
        actionSet = new FcsActionSet(ruleJSONObject.getJSONArray(SC_RULE_ACTION_SET));
    }

    private void initialiseConditionSet() {
        conditionSetJSONArray = ruleJSONObject.getJSONArray(SC_RULE_CONDITIONS);
        conditionSet = new ArrayList<>();

        for(int i = 0; i < conditionSetJSONArray.length(); ++i) {
            conditionSet.add(new FcsRuleCondition(conditionSetJSONArray.getJSONObject(i)));
        }
    }

    @Override
    public String getId() {
        return ruleId;
    }

    @Override
    public List<Condition> getConditions() {
        return conditionSet;
    }

    @Override
    public ActionSet getActionSet() {
        return actionSet;
    }
}
