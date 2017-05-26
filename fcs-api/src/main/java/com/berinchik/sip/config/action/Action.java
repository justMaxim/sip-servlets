package com.berinchik.sip.config.action;

import com.berinchik.sip.config.target.Target;

import java.util.List;

/**
 * Created by Maksim on 24.05.2017.
 */
public interface Action {
    ActionId getActionId();
    List<Target> getTargets();
}
