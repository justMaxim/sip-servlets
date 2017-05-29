package com.berinchik.sip.service.fsm.state;

import com.berinchik.sip.config.action.Action;
import com.berinchik.sip.config.condition.Condition;
import com.berinchik.sip.config.rule.Rule;
import com.berinchik.sip.error.FcsUnexpectedException;
import com.berinchik.sip.service.fsm.SipServiceContext;
import com.berinchik.sip.util.CommonUtils;
import org.mobicents.media.server.io.sdp.SdpException;

import javax.servlet.sip.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static javax.servlet.sip.SipServletResponse.SC_BUSY_EVERYWHERE;
import static javax.servlet.sip.SipServletResponse.SC_BUSY_HERE;

/**
 * Created by Maksim on 29.05.2017.
 */
public class BaseState implements SipServiceState {

    private static final String SC_ERROR_MESSAGE = "Attempt to process request in BaseState's method";

    @Override
    public void doAck(SipServletRequest req, SipServiceContext context) throws IOException {
        throw new IllegalStateException(SC_ERROR_MESSAGE);
    }

    @Override
    public void doBye(SipServletRequest req, SipServiceContext context)
            throws IOException, ServletParseException {
        throw new IllegalStateException(SC_ERROR_MESSAGE);
    }

    @Override
    public void doCancel(SipServletRequest req, SipServiceContext context)
            throws IOException {
        throw new IllegalStateException(SC_ERROR_MESSAGE);
    }

    @Override
    public void doErrorResponse(SipServletResponse resp, SipServiceContext context)
            throws IOException, SQLException, ServletParseException {
        throw new IllegalStateException(SC_ERROR_MESSAGE);
    }

    @Override
    public void doInvite(SipServletRequest req, SipServiceContext context)
            throws SQLException, IOException, ServletParseException {
        throw new IllegalStateException(SC_ERROR_MESSAGE);
    }

    @Override
    public void doProvisionalResponse(SipServletResponse resp, SipServiceContext context)
            throws IOException {
        throw new IllegalStateException(SC_ERROR_MESSAGE);
    }

    @Override
    public void doRedirectResponse(SipServletResponse resp, SipServiceContext context) {
        throw new IllegalStateException(SC_ERROR_MESSAGE);
    }

    @Override
    public void doSubscribe(SipServletRequest req, SipServiceContext context) {
        throw new IllegalStateException(SC_ERROR_MESSAGE);
    }

    @Override
    public void doSuccessResponse(SipServletResponse resp, SipServiceContext context)
            throws IOException, SdpException {
        throw new IllegalStateException(SC_ERROR_MESSAGE);
    }

    @Override
    public void doUpdate(SipServletRequest req, SipServiceContext context) {
        throw new IllegalStateException(SC_ERROR_MESSAGE);
    }

    @Override
    public void noAckReceived(SipErrorEvent sipErrorEvent, SipServiceContext context) {
        throw new IllegalStateException(SC_ERROR_MESSAGE);
    }

    @Override
    public void doTimeout(ServletTimer timer, SipServiceContext context)
            throws IOException, ServletParseException, SQLException {
        throw new IllegalStateException(SC_ERROR_MESSAGE);
    }

    protected SipServiceState performAction(Action action, SipServiceContext context)
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

    protected Rule matchRuleAtErrorResponse(List<Rule> rulesList, SipServletResponse resp) {
        for (Rule rule : rulesList) {
            List<Condition> conditions = rule.getConditions();
            if (conditionsMatchAtErrorResponse(conditions, resp)) {
                return rule;
            }
        }
        return null;
    }

    protected Rule matchRulesAtInitialInvite(List<Rule> rulesList) {

        for (Rule rule : rulesList) {
            List<Condition> conditions = rule.getConditions();
            if (conditionsMatchAtInitialInvite(conditions)) {
                return rule;
            }
        }
        return null;
    }

    protected Rule matchRulesAtNotReachableTimeout(List<Rule> rulesList) {

        for (Rule rule : rulesList) {
            List<Condition> conditions = rule.getConditions();
            if (conditionsMatchAtNotReachableTimeout(conditions)) {
                return rule;
            }
        }
        return null;
    }

    protected Rule matchRulesAtRingingTimeout(List<Rule> rulesList) {

        for (Rule rule : rulesList) {
            List<Condition> conditions = rule.getConditions();
            if (conditionsMatchAtRingingTimeout(conditions)) {
                return rule;
            }
        }
        return null;
    }

    protected boolean conditionsMatchAtErrorResponse(List<Condition> conditions, SipServletResponse resp) {
        for (Condition condition :
                conditions) {
            switch (condition.getConditionId()) {
                case BUSY:
                    if (resp.getStatus() != SC_BUSY_HERE &&
                            resp.getStatus() != SC_BUSY_EVERYWHERE){
                        return false;
                    }
                    break;
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

    protected boolean conditionsMatchAtInitialInvite(List<Condition> conditions) {
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

    protected boolean conditionsMatchAtNotReachableTimeout(List<Condition> conditions) {
        for (Condition condition :
                conditions) {
            switch (condition.getConditionId()) {
                case BUSY:
                case NO_ANSWER:
                    return false;
                case NOT_REACHABLE:
                    return true;
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

    protected boolean conditionsMatchAtRingingTimeout(List<Condition> conditions) {
        for (Condition condition :
                conditions) {
            switch (condition.getConditionId()) {
                case BUSY:
                case NOT_REACHABLE:
                    return false;
                case NO_ANSWER:
                    return true;
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
}
