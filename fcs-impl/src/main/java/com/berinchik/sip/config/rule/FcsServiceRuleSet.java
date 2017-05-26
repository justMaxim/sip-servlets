package com.berinchik.sip.config.rule;


import org.json.JSONArray;

import java.util.List;

/**
 * Created by Maksim on 26.05.2017.
 */
public class FcsServiceRuleSet implements RuleSet {

    private List<Rule> rulesSet;

    public FcsServiceRuleSet(JSONArray jsonRuleSet) {

    }


    @Override
    public List<Rule> getRules() {
        return rulesSet;
    }
}
