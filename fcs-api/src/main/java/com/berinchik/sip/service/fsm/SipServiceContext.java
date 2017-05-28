package com.berinchik.sip.service.fsm;

import com.berinchik.sip.config.ServiceConfig;
import com.berinchik.sip.config.action.Action;
import com.berinchik.sip.config.action.ActionSet;
import com.berinchik.sip.config.rule.Rule;
import com.berinchik.sip.config.target.ActionTarget;
import com.berinchik.sip.service.fsm.state.*;
import org.json.JSONObject;
import org.mobicents.media.server.io.sdp.SdpException;

import javax.servlet.sip.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;


/**
 * Created by Maksim on 26.05.2017.
 */
public interface SipServiceContext {

    void doAck(SipServletRequest req) throws IOException;

    void doBye(SipServletRequest req) throws IOException, ServletParseException;

    void doCancel(SipServletRequest req) throws IOException;

    void doErrorResponse(SipServletResponse resp) throws IOException;

    void doInvite(SipServletRequest req) throws SQLException, IOException, ServletParseException;

    void doProvisionalResponse(SipServletResponse resp) throws IOException;

    void doRedirectResponse(SipServletResponse resp);

    void doSubscribe(SipServletRequest req);

    void doSuccessResponse(SipServletResponse resp) throws IOException, SdpException;

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

    void cancelNotReachableTimer();

    void startRingingTimer();

    void cancelRingingTimer();

    void doTimeout(ServletTimer timer);

    void sendSuccess(SipServletResponse resp) throws IOException, SdpException;

    void forwardBye(SipServletRequest receivedByeRequest) throws IOException, ServletParseException;
}
