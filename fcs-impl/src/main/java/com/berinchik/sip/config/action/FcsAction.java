package com.berinchik.sip.config.action;

import com.berinchik.sip.error.ActionTypeParseException;
import com.berinchik.sip.error.FcsUnexpectedException;
import com.berinchik.sip.config.target.ActionTarget;
import com.berinchik.sip.config.target.FcsParallelRingingTarget;
import com.berinchik.sip.config.target.FcsSerialRingingTarget;

import static com.berinchik.sip.config.FcsServiceConfig.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Maksim on 27.05.2017.
 */
public class FcsAction implements Action {

    private static Log logger = LogFactory.getLog(FcsAction.class);

    private ActionId actionId;
    private List<ActionTarget> targets;
    private JSONObject actionJSONObject;
    private int period = 10;

    private int currentTarget = 0;

    public FcsAction(JSONObject actionJson) {
        actionJSONObject = actionJson;
        period = actionJson.getInt(SC_ACTION_PERIOD);
        initialiseActionID();
        logger.trace("ActionID: " + getActionId());
        initialiseTargetList();
    }

    @Override
    public ActionId getActionId() {
        return actionId;
    }

    @Override
    public List<ActionTarget> getTargets() {
        return targets;
    }

    @Override
    public ActionTarget getNextTarget() {
        if (actionId == ActionId.PARALLEL || targets.size() <= currentTarget - 1) {
            return null;
        }

        ActionTarget nextTarget = targets.get(currentTarget);
        currentTarget += 1;

        return nextTarget;
    }

    @Override
    public ActionTarget getCurrentTarget() {
        if (actionId == ActionId.PARALLEL) {
            return null;
        }
        return targets.get(currentTarget);
    }

    @Override
    public int getPeriod() {
        return period;
    }

    private void initialiseActionID() {
        String actionIdString = actionJSONObject.getString(SC_ACTION_TYPE);
        if(SC_ACTION_SERIAL.equals(actionIdString)) {
            actionId = ActionId.SERIAL;
        }
        else if (SC_ACTION_PARALLEL.equals(actionIdString)) {
            actionId = ActionId.PARALLEL;
        }
        else {
            throw new ActionTypeParseException("Incompatible action type: " + actionIdString);
        }
    }

    private void initialiseTargetList() {
        targets = new ArrayList<>();
        JSONArray actionsJsonArray = actionJSONObject.getJSONArray(SC_ACTION_TARGETS);
        logger.trace("Initialising targets list: ");
        for(int i = 0; i < actionsJsonArray.length(); ++i) {
            targets.add(createTarget(actionsJsonArray.getJSONObject(i)));
        }

    }

    private ActionTarget createTarget(JSONObject targetJSONObject) {
        switch (actionId) {
            case SERIAL:
                return new FcsSerialRingingTarget(targetJSONObject);
            case PARALLEL:
                return new FcsParallelRingingTarget(targetJSONObject);
            default:
                throw new FcsUnexpectedException("Action initialise with wrong id" + actionId);
        }
    }
}
