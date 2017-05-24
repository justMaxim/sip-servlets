package com.berinchik.sip.config;

import org.json.JSONObject;

/**
 * Created by Maksim on 24.05.2017.
 */
public interface Condition {
    CONDITION_ID getConditionId();
    void setConditionId(CONDITION_ID conditionId);

    JSONObject getConditionValue();
    void setConditionValue(CONDITION_ID conditionId, JSONObject conditionValue);
}
