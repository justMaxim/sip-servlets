package com.berinchik.sip.config.condition;

import org.json.JSONObject;

/**
 * Created by Maksim on 24.05.2017.
 */
public interface Condition {
    static final String SC_CONDITION_BUSY = "busy";//486 Busy
    static final String SC_CONDITION_NOT_REACHABLE = "not-reachable";//No 180 ringing
    static final String SC_CONDITION_NO_ANSWER = "no-answer";//No 200 Ok after 180 ringing

    static final String SC_CONDITION_VALID_PERIODS = "valid-periods";
    static final String SC_PERIODS_ARRAY = "periods";
    static final String SC_CONDITION_VP_PERIOD_TYPE = "period-type";
    static final String SC_CONDITION_PERIOD_TYPE_VALID_DAYS = "valid-days";
    static final String SC_CONDITION_PERIOD_TYPE_VALID_TIMES = "valid-times";
    static final String SC_CONDITION_PERIOD_TYPE_VALUES = "values";

    static final String SC_CONDITION_PERIOD_TIME_FROM= "from";
    static final String SC_CONDITION_PERIOD_TIME_TO = "to";


    ConditionId getConditionId();
    JSONObject getConditionValue();
}
