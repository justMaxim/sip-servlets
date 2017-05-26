package com.berinchik.sip.config.rule;

import com.berinchik.sip.config.action.ActionSet;
import com.berinchik.sip.config.condition.Condition;

import java.util.List;

/**
 * Created by Maksim on 24.05.2017.
 */
public interface Rule {
    String getId();
    List<Condition> getConditions();
    ActionSet getActionSet();
}
