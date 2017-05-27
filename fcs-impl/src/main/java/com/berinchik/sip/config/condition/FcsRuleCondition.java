package com.berinchik.sip.config.condition;

import static com.berinchik.sip.config.FcsServiceConfig.*;

import com.berinchik.sip.config.error.UnsupportedConditionException;
import org.json.JSONObject;

/**
 * Created by Maksim on 27.05.2017.
 */
public class FcsRuleCondition implements Condition {

    private static final String SC_CONDITION_BUSY = "busy";//486 Busy
    private static final String SC_CONDITION_NOT_REACHABLE = "not-reachable";//No 180 ringing
    private static final String SC_CONDITION_NO_ANSWER = "no-answer";//No 200 Ok after 180 ringing

    private static final String SC_CONDITION_VALID_PERIODS = "valid-periods";
    public static final String SC_CONDITION_VP_PERIOD_TYPE = "period-type";
    public static final String SC_CONDITION_PERIOD_TYPE_VALID_DAYS = "valid-days";
    public static final String SC_CONDITION_PERIOD_TYPE_VALID_TIMES = "valid-periods";

    ConditionId conditionId;
    String stringConditionId;
    JSONObject conditionValue;

    public FcsRuleCondition(JSONObject jsonConditionObject) {
        stringConditionId = jsonConditionObject.getString(SC_CONDITION_TYPE);
        conditionValue = jsonConditionObject;
        initialiseCondition();
    }

    private void initialiseCondition() {
        switch (stringConditionId) {
            case SC_CONDITION_BUSY:
                conditionId = ConditionId.BUSY;
                break;
            case SC_CONDITION_NO_ANSWER:
                conditionId = ConditionId.NO_ANSWER;
                break;
            case SC_CONDITION_NOT_REACHABLE:
                conditionId = ConditionId.NOT_REACHABLE;
                break;
            case SC_CONDITION_VALID_PERIODS:
                conditionId = ConditionId.VALID_PERIODS;
                break;
            default:
                throw new UnsupportedConditionException("Condition " + stringConditionId + " is not supported");
        }
    }


    @Override
    public ConditionId getConditionId() {
        return null;
    }

    @Override
    public JSONObject getConditionValue() {
        return null;
    }
}
