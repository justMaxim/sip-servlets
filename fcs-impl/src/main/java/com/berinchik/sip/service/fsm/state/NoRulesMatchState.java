package com.berinchik.sip.service.fsm.state;

import com.berinchik.sip.config.action.Action;
import com.berinchik.sip.config.action.ActionSet;
import com.berinchik.sip.config.rule.Rule;
import com.berinchik.sip.error.FcsUnexpectedException;
import com.berinchik.sip.service.fsm.SipServiceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.sip.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static javax.servlet.sip.SipServletResponse.*;

/**
 * Created by Maksim on 27.05.2017.
 */
public class NoRulesMatchState extends InviteForwardedAtNoSettingsState {

    private static Log logger = LogFactory.getLog(NoRulesMatchState.class);

    @Override
    public void doErrorResponse(SipServletResponse resp, SipServiceContext context)
            throws IOException, SQLException, ServletParseException {
        logger.info("Processing error response: " + resp.getStatus() + " " + resp.getReasonPhrase());
        context.cancelAllTimers();
        logger.trace("all timers canceled");
        List<Rule> rulesList
                = context.getUserSettings().getRuleSet().getRules();

        context.getCallContext().removeRequest(resp.getRequest());

        logger.trace("matching rules");
        Rule matchedRule = matchRuleAtErrorResponse(rulesList, resp);
        SipServiceState nextState = null;
        ActionSet serviceActionSet = null;

        if (matchedRule != null) {
            logger.trace("rule matched:\n" + matchedRule.getId());
            context.setMatchedRule(matchedRule);

            serviceActionSet = matchedRule.getActionSet();
            context.setActionSet(serviceActionSet);

            Action currentAction = context.getCurrentAction();
            logger.trace("performing action");
            nextState = performAction(currentAction, context);
        }
        else {
            context.doRejectInvite(resp.getStatus(), resp.getReasonPhrase());
            nextState = new InviteCanceledState();
        }
        context.setState(nextState);
    }

    /*@Override
    public void doProvisionalResponse(SipServletResponse resp, SipServiceContext context) throws IOException {
        logger.info("Processing provisional response: " + resp.getStatus() + " " + resp.getReasonPhrase());
        if (resp.getStatus() == SC_RINGING) {
            context.cancelNotReachableTimer();
            context.sendRingingToCaller();
        }
        context.startRingingTimer();
    }*/

    @Override
    public void noAckReceived(SipErrorEvent sipErrorEvent, SipServiceContext context) {

    }

    @Override
    public void doTimeout(ServletTimer timer, SipServiceContext context)
            throws ServletParseException, SQLException, IOException {

        List<Rule> rulesList
                = context.getUserSettings().getRuleSet().getRules();
        Rule matchedRule = null;
        SipServiceState nextState = null;
        context.getCallContext().cancelAllOutgoing();

        if(context.isNotReachableTimer(timer)) {

            matchedRule = super.matchRulesAtNotReachableTimeout(rulesList);

            if (matchedRule == null) {
                context.doRejectInvite(SC_TEMPORARILY_UNAVAILABLE, "Not reachable");
                context.getCallContext().cancelAllOutgoing();
                nextState = new InviteCanceledState();
            }
        }
        else if(context.isRingingTimer(timer)) {
            matchedRule = super.matchRulesAtRingingTimeout(rulesList);

            if (matchedRule == null) {
                context.doRejectInvite(SC_REQUEST_TIMEOUT, "No answer too long ");
                context.getCallContext().cancelAllOutgoing();
                nextState = new InviteCanceledState();
            }
        }
        else {
            throw new FcsUnexpectedException("Unrecognised timer: " + timer);
        }
        if (matchedRule != null) {
            context.setMatchedRule(matchedRule);
            ActionSet serviceActionSet = matchedRule.getActionSet();
            context.setActionSet(serviceActionSet);

            Action currentAction = context.getCurrentAction();
            nextState = performAction(currentAction, context);
        }

        context.setState(nextState);
    }
}
