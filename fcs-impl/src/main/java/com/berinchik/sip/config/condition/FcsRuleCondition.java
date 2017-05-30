package com.berinchik.sip.config.condition;

import static com.berinchik.sip.config.FcsServiceConfig.*;

import com.berinchik.sip.error.UnsupportedConditionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

/**
 * Created by Maksim on 27.05.2017.
 */
public class FcsRuleCondition implements Condition {

    private static Log logger = LogFactory.getLog(FcsRuleCondition.class);

    ConditionId conditionId;
    String stringConditionId;
    JSONObject conditionValue;

    public FcsRuleCondition(JSONObject jsonConditionObject) {
        stringConditionId = jsonConditionObject.getString(SC_CONDITION_TYPE);
        conditionValue = jsonConditionObject;
        initialiseCondition();
        logger.trace("Condition ID: " + getConditionId());
        logger.trace("Condition value: " + getConditionValue());
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
                logger.error("Condition ID not parsed: " + stringConditionId);
                throw new UnsupportedConditionException("Condition " + stringConditionId + " is not supported");
        }
    }


    @Override
    public ConditionId getConditionId() {
        return conditionId;
    }

    @Override
    public JSONObject getConditionValue() {
        return conditionValue;
    }
}
