package com.berinchik.sip.service.fsm;

import com.berinchik.sip.config.ServiceConfig;
import com.berinchik.sip.service.fsm.state.*;

import javax.servlet.sip.SipErrorEvent;
import javax.servlet.sip.SipFactory;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;


/**
 * Created by Maksim on 26.05.2017.
 */
public interface SipServiceContext {

    void doAck(SipServletRequest req);

    void doBye(SipServletRequest req);

    void doCancel(SipServletRequest req);

    void doErrorResponse(SipServletResponse resp);

    void doInvite(SipServletRequest req);

    void doProvisionalResponse(SipServletResponse resp);

    void doRedirectResponse(SipServletResponse resp);

    void doSubscribe(SipServletRequest req);

    void doSuccessResponse(SipServletResponse resp);

    void doUpdate(SipServletRequest req);

    CallContext getCallContext();

    void setCallContext(CallContext callContext);

    SipServletRequest getInitialRequest();

    SipFactory getSipFactory();

    void setState(SipServiceState state);

    void noAckReceived(SipErrorEvent sipErrorEvent);

    ServiceConfig getServiceConfig();

}
