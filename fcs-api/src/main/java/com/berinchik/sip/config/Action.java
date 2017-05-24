package com.berinchik.sip.config;

import java.util.List;

/**
 * Created by Maksim on 24.05.2017.
 */
public interface Action {
    ACTION_ID getActionId();
    List<Target> getTargets();
}
