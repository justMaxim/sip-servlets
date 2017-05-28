package com.berinchik.sip.service.fsm.state;

import com.berinchik.sip.config.ServiceConfig;
import com.berinchik.sip.config.action.Action;
import com.berinchik.sip.config.action.ActionSet;
import com.berinchik.sip.config.condition.Condition;
import com.berinchik.sip.error.FcsUnexpectedException;
import com.berinchik.sip.config.rule.Rule;
import com.berinchik.sip.service.fsm.SipServiceContext;
import com.berinchik.sip.service.registrar.Registrar;
import com.berinchik.sip.util.CommonUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.JSONObject;

import javax.servlet.sip.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static javax.servlet.sip.SipServletResponse.*;


/**
 * Created by Maksim on 26.05.2017.
 */
public class InitialState implements SipServiceState  {

    private static Log logger = LogFactory.getLog(InitialState.class);

    @Override
    public void doAck(SipServletRequest req, SipServiceContext context) {
    }

    @Override
    public void doBye(SipServletRequest req, SipServiceContext context) {

    }

    @Override
    public void doCancel(SipServletRequest req, SipServiceContext context) {

    }

    @Override
    public void doErrorResponse(SipServletResponse resp, SipServiceContext context) {

    }

    @Override
    public void doInvite(SipServletRequest req, SipServiceContext context) throws SQLException, IOException, ServletParseException {

        //Check if the user is primary
        String reqUriString = req.getRequestURI().toString();
        Registrar registrar = CommonUtils.getRegistrarHelper(req);

        JSONObject userSettingsJSON = null;
        ActionSet serviceActionSet = null;
        Rule matchedRule = null;
        SipServiceState nextState = null;

        String primaryUserIdentity = registrar.getPrimaryUserId(reqUriString);

        if (primaryUserIdentity == null) {
            //reject invite
            context.doRejectInvite(SC_NOT_FOUND, "User not found");
            nextState = new InviteCanceledState();
        }
        else {
            logger.info("User " + reqUriString + "is primary.\nTrying to get user settings");
            userSettingsJSON = registrar.getServiceConfig(reqUriString);

            if (userSettingsJSON != null) {
                logger.info("User has following settings set:\n" + userSettingsJSON);
                //найти сматченный экшэн
                context.setUserSettings(userSettingsJSON);
                ServiceConfig userSettings = context.getUserSettings();
                List<Rule> rulesList = userSettings.getRuleSet().getRules();

                if (rulesList.isEmpty()) {
                    logger.info("rule-set is empty");
                    throw new FcsUnexpectedException("rule set is empty for: " + reqUriString);
                }

                matchedRule = matchRulesAtInitialInvite(rulesList);

                if (matchedRule == null) {
                    if (context.sendInvite(primaryUserIdentity)) {
                        nextState = new NoRulesMatchState();
                    }
                }
                else {
                    context.setMatchedRule(matchedRule);
                    serviceActionSet = matchedRule.getActionSet();
                    context.setActionSet(serviceActionSet);

                    Action currentAction = context.getCurrentAction();
                    nextState = performAction(currentAction, context);
                }

            }
            else {
                logger.info("No service config found for user: " + reqUriString);
                if(context.sendInvite(primaryUserIdentity)){
                    nextState = new InviteForwardedAtNoSettingsState();
                }
                else {
                    context.doRejectInvite(SC_TEMPORARILY_UNAVAILABLE, "Temporarily unavailable");
                    nextState = new InviteCanceledState();
                }
            }
        }
        context.setState(nextState);

    }

    private SipServiceState performAction(Action action, SipServiceContext context)
            throws IOException, ServletParseException, SQLException {
        switch(action.getActionId()) {
            case PARALLEL:
                context.doParallel();
                return new ParallelRingingExecutedState();
            case SERIAL:
                context.doSerial();
                return new SerialRingingExecutedState();
        }
        throw new FcsUnexpectedException("Action id: " + action.getActionId() + " is not supported;");
    }

    private Rule matchRulesAtInitialInvite(List<Rule> rulesList) {

        for (Rule rule : rulesList) {
            List<Condition> conditions = rule.getConditions();
            if (conditionsMatchAtInitialInvite(conditions)) {
                return rule;
            }
        }
        return null;
    }

    private boolean conditionsMatchAtInitialInvite(List<Condition> conditions) {
        for (Condition condition :
                conditions) {
            switch (condition.getConditionId()) {
                case BUSY:
                    return false;
                case NO_ANSWER:
                    return false;
                case NOT_REACHABLE:
                    return false;
                case VALID_PERIODS:
                    if (!CommonUtils.matchDateCondition(condition))
                        return false;
                    break;
                default:
                    throw new FcsUnexpectedException("Unexpected condition: " + condition);
            }
        }
        return true;
    }

    @Override
    public void doProvisionalResponse(SipServletResponse resp, SipServiceContext context) {

    }

    @Override
    public void doRedirectResponse(SipServletResponse resp, SipServiceContext context) {

    }

    @Override
    public void doSubscribe(SipServletRequest req, SipServiceContext context) {

    }

    @Override
    public void doSuccessResponse(SipServletResponse resp, SipServiceContext context) throws IOException {

    }

    @Override
    public void doUpdate(SipServletRequest req, SipServiceContext context) {

    }

    @Override
    public void noAckReceived(SipErrorEvent sipErrorEvent, SipServiceContext context) {

    }

    @Override
    public void doTimeout(ServletTimer timer, SipServiceContext context) {

    }
}
