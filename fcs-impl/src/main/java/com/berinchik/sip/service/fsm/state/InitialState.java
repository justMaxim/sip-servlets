package com.berinchik.sip.service.fsm.state;

import com.berinchik.sip.config.ServiceConfig;
import com.berinchik.sip.config.action.Action;
import com.berinchik.sip.config.action.ActionSet;
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
public class InitialState extends BaseState  {

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
    public void doInvite(SipServletRequest req, SipServiceContext context)
            throws SQLException, IOException, ServletParseException {

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
            logger.debug("User " + reqUriString + "is primary.\nTrying to get user settings");
            userSettingsJSON = registrar.getServiceConfig(reqUriString);

            if (userSettingsJSON != null) {
                //check conditions and find action
                logger.debug("User has following settings set:\n" + userSettingsJSON);

                context.setUserSettingsJSON(userSettingsJSON);
                ServiceConfig userSettings = context.getUserSettings();
                List<Rule> rulesList = userSettings.getRuleSet().getRules();

                //fixme:maybe it should be checked on lower level(UserSettings's methods etc.)
                if (rulesList == null) {
                    logger.debug("rule-set is empty");
                    throw new FcsUnexpectedException("rule set is empty for: " + reqUriString);
                }
                else if(rulesList.isEmpty()) {
                    logger.debug("rule-set is empty");
                    throw new FcsUnexpectedException("rule set is empty for: " + reqUriString);
                }

                matchedRule = matchRulesAtInitialInvite(rulesList);

                if (matchedRule == null) {
                    logger.debug("No rules matched");
                    if (context.sendInvite(primaryUserIdentity)) {
                        nextState = new NoRulesMatchState();
                    }
                }
                else {
                    logger.debug("Rules matched at initial invite");
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
