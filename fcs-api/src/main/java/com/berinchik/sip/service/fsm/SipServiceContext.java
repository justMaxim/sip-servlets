package com.berinchik.sip.service.fsm;

import com.berinchik.sip.config.ServiceConfig;
import com.berinchik.sip.config.action.Action;
import com.berinchik.sip.config.action.ActionSet;
import com.berinchik.sip.config.rule.Rule;
import com.berinchik.sip.config.target.ActionTarget;
import com.berinchik.sip.service.fsm.state.*;
import org.json.JSONObject;

import javax.servlet.sip.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;


/**
 * Created by Maksim on 26.05.2017.
 */
public interface SipServiceContext {

    void doAck(SipServletRequest req);

    void doBye(SipServletRequest req);

    void doCancel(SipServletRequest req);

    void doErrorResponse(SipServletResponse resp);

    void doInvite(SipServletRequest req) throws SQLException, IOException, ServletParseException;

    void doProvisionalResponse(SipServletResponse resp);

    void doRedirectResponse(SipServletResponse resp);

    void doSubscribe(SipServletRequest req);

    void doSuccessResponse(SipServletResponse resp);

    void doUpdate(SipServletRequest req);

    CallContext getCallContext();

    void setCallContext(CallContext callContext);

    SipServletRequest getInitialRequest();

    SipFactory getSipFactory();

    void setActionSet(ActionSet actionSet);

    Action getCurrentAction();

    Action getNextAction();

    void setState(SipServiceState state);

    void setUserSettings(JSONObject settings);

    ServiceConfig getUserSettings();

    void setMatchedRule(Rule rule);

    Rule getMatchedRule();

    void noAckReceived(SipErrorEvent sipErrorEvent);

    void doParallel() throws IOException, ServletParseException;

    void doSerial() throws ServletParseException, IOException;

    public void doRejectInvite(int code, String message) throws IOException;

    public void doForwardInvite(String primaryUser) throws SQLException, ServletParseException, IOException;

    ServiceConfig getServiceConfig() throws IOException ;

    void doTimeout(ServletTimer timer);

}
