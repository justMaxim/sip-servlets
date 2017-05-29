package com.berinchik.sip.config.action;

import static com.berinchik.sip.config.FcsServiceConfig.*;

import com.berinchik.sip.error.FcsUnexpectedException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Maksim on 27.05.2017.
 */
public class FcsActionSet implements ActionSet {

    private ActionSetId actionSetId;
    private List<Action> actionSet;
    private JSONArray actionJSONArray;
    private int currentAction = 0;

    public FcsActionSet(JSONArray actionJSONArray) {
        this.actionJSONArray = actionJSONArray;
        initialiseActionSetId();
        initialiseActionSet();
    }

    private void initialiseActionSet() {
        actionSet = new ArrayList<>();
        for(int i = 0; i < actionJSONArray.length(); ++i) {
            actionSet.add(new FcsAction(actionJSONArray.getJSONObject(i)));
        }
    }

    private void initialiseActionSetId() {
        if(actionJSONArray.length() > 1) {
            actionSetId = ActionSetId.FLEXIBLE_RINGING;
        }
        else if(actionJSONArray.length() == 1)  {
            String actionType = actionJSONArray.getJSONObject(0).getString(SC_ACTION_TYPE);
            initialiseActionSetId(actionType);
        }
    }

    private void initialiseActionSetId(String s) {
        if(SC_ACTION_PARALLEL.equals(s)) {
            actionSetId = ActionSetId.PARALLEL_RINGING;
        }
        else if (SC_ACTION_SERIAL.equals(s)) {
            actionSetId = ActionSetId.SERIAL_RINGING;
        }
        else {
            throw new FcsUnexpectedException("unsupported action type: " + s);
        }
    }

    @Override
    public ActionSetId getActionSetId() {
        return actionSetId;
    }

    @Override
    public List<Action> getActions() {
        return actionSet;
    }

    @Override
    public Action getNextAction() {
        try {
            return actionSet.get(currentAction++);
        }
        catch(IndexOutOfBoundsException ex) {
            return null;
        }
    }
}
