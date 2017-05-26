package com.berinchik.sip.service.fsm;

import com.berinchik.sip.config.ServiceConfig;
import com.berinchik.sip.service.fsm.state.SipServiceState;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.sip.*;

/**
 * Created by Maksim on 26.05.2017.
 */
public class FcsServiceContext implements SipServiceContext {

    private static Log logger = LogFactory.getLog(FcsServiceContext.class);
    private SipFactory sipFactory;
    private CallContext callContext;
    private SipServiceState serviceState;
    private ServiceConfig serviceConfig;

    public FcsServiceContext(SipFactory sipFactory) {
        this.sipFactory = sipFactory;
    }

    @Override
    public void doAck(SipServletRequest req) {
        messageAndStateInfo(req);
    }

    @Override
    public void doBye(SipServletRequest req) {
        messageAndStateInfo(req);
    }

    @Override
    public void doCancel(SipServletRequest req) {
        messageAndStateInfo(req);
    }

    @Override
    public void doErrorResponse(SipServletResponse resp) {
        messageAndStateInfo(resp);
    }

    @Override
    public void doInvite(SipServletRequest req) {
        messageAndStateInfo(req);
    }

    @Override
    public void doProvisionalResponse(SipServletResponse resp) {
        messageAndStateInfo(resp);
    }

    @Override
    public void doRedirectResponse(SipServletResponse resp) {
        messageAndStateInfo(resp);
    }

    @Override
    public void doSubscribe(SipServletRequest req) {
        messageAndStateInfo(req);
    }

    @Override
    public void doSuccessResponse(SipServletResponse resp) {
        messageAndStateInfo(resp);
    }

    @Override
    public void doUpdate(SipServletRequest req){
        messageAndStateInfo(req);
    }

    @Override
    public CallContext getCallContext() {
        return this.callContext;
    }

    @Override
    public void setCallContext(CallContext callContext) {
        this.callContext = callContext;
    }

    @Override
    public SipServletRequest getInitialRequest() {
        return callContext.getInitialRequest();
    }

    @Override
    public void setState(SipServiceState state) {
        logger.info("\nstate changes from " + serviceState.getClass().getSimpleName()
                + "\nto " + state.getClass().getSimpleName());
        this.serviceState = state;
    }

    @Override
    public SipFactory getSipFactory(){
        return this.sipFactory;
    }

    @Override
    public void noAckReceived(SipErrorEvent sipErrorEvent) {
        serviceState.noAckReceived(sipErrorEvent, this);
    }

    @Override
    public ServiceConfig getServiceConfig() {
        return serviceConfig;
    }

    void messageAndStateInfo(SipServletMessage message) {
        logger.info("Received message: \n" + message
                + "\nIn state: " + serviceState.getClass().getSimpleName());
    }
}
