package com.berinchik.sip.config.action;

import java.util.List;

/**
 * Created by Maksim on 24.05.2017.
 */
public interface ActionSet {
    ActionSetId getActionSetId();
    List<Action> getActions();
    Action getNextAction();
}
