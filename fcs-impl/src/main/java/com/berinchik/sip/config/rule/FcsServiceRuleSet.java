package com.berinchik.sip.config.rule;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Maksim on 26.05.2017.
 */
public class FcsServiceRuleSet implements RuleSet {

    private List<Rule> ruleSet;
    private JSONArray jsonArrayRuleSet;

    public FcsServiceRuleSet(JSONArray jsonRuleSet) {
        jsonArrayRuleSet = jsonRuleSet;
    }

    private void initialiseRuleSet() {
        ruleSet = new ArrayList<>();
        for(int i = 0; i < jsonArrayRuleSet.length(); ++i) {
            JSONObject ruleJsonObject = jsonArrayRuleSet.getJSONObject(i);
            ruleSet.add(new FcsServiceRule(ruleJsonObject));
        }
    }

    @Override
    public List<Rule> getRules() {
        return ruleSet;
    }
}
