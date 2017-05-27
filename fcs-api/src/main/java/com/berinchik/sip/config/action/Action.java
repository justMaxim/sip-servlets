package com.berinchik.sip.config.action;

import com.berinchik.sip.config.target.ActionTarget;

import java.util.List;

/**
 * Created by Maksim on 24.05.2017.
 */
public interface Action {
    ActionId getActionId();
    int getPeriod();
    List<ActionTarget> getTargets();
    ActionTarget getNextTarget();
    ActionTarget getCurrentTarget();
}
