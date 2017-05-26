package com.berinchik.sip.config.condition;

import org.json.JSONObject;

/**
 * Created by Maksim on 24.05.2017.
 */
public interface Condition {
    ConditionId getConditionId();
    JSONObject getConditionValue();
}
