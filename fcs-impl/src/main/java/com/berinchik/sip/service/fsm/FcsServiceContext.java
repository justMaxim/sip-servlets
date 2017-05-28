package com.berinchik.sip.service.fsm;

import com.berinchik.sip.config.FcsServiceConfig;
import com.berinchik.sip.config.ServiceConfig;
import com.berinchik.sip.config.action.Action;
import com.berinchik.sip.config.action.ActionSet;
import com.berinchik.sip.config.rule.Rule;
import com.berinchik.sip.config.target.ActionTarget;
import com.berinchik.sip.service.fsm.state.InitialState;
import com.berinchik.sip.service.fsm.state.SipServiceState;
import com.berinchik.sip.service.registrar.Registrar;
import com.berinchik.sip.service.registrar.database.util.Binding;
import com.berinchik.sip.util.CommonUtils;
import com.berinchik.sip.util.SdpUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.mobicents.media.server.io.sdp.SdpException;

import javax.servlet.sip.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static javax.servlet.sip.SipServletResponse.SC_OK;

/**
 * Created by Maksim on 26.05.2017.
 */
public class FcsServiceContext implements SipServiceContext {

    private static Log logger = LogFactory.getLog(FcsServiceContext.class);

    private SipFactory sipFactory;
    private CallContext callContext;
    private SipServiceState serviceState;
    private ServiceConfig serviceConfig;
    private Rule matchedRule;
    private ActionSet actionSet;
    private Action currentAction;
    private ServletTimer notReachableTimer;
    private ServletTimer ringingTimer;

    private int defaultRinginPeriod = 10;
    private int defaultNotReachablePeriod = 10;

    public FcsServiceContext(SipFactory sipFactory) {
        this.sipFactory = sipFactory;
    }

    @Override
    public synchronized void doAck(SipServletRequest req) throws IOException {
        messageAndStateInfo(req);
        serviceState.doAck(req, this);
    }

    @Override
    public synchronized void doBye(SipServletRequest req)
            throws IOException, ServletParseException {
        messageAndStateInfo(req);
        serviceState.doBye(req, this);
    }

    @Override
    public synchronized void doCancel(SipServletRequest req) throws IOException {
        messageAndStateInfo(req);
        serviceState.doCancel(req, this);
    }

    @Override
    public synchronized void doErrorResponse(SipServletResponse resp) throws IOException {
        messageAndStateInfo(resp);
        serviceState.doErrorResponse(resp, this);
    }

    @Override
    public synchronized void doInvite(SipServletRequest req) throws SQLException, IOException, ServletParseException {
        if (req.isInitial()) {
            callContext = new FcsCallContext(req);
            serviceState = new InitialState();
        }
        messageAndStateInfo(req);
        serviceState.doInvite(req, this);
    }

    @Override
    public synchronized void doProvisionalResponse(SipServletResponse resp) throws IOException {
        messageAndStateInfo(resp);
        serviceState.doProvisionalResponse(resp, this);
    }

    @Override
    public synchronized void doRedirectResponse(SipServletResponse resp) {
        messageAndStateInfo(resp);
    }

    @Override
    public synchronized void doSubscribe(SipServletRequest req) {
        messageAndStateInfo(req);
    }

    @Override
    public synchronized void doSuccessResponse(SipServletResponse resp) throws IOException, SdpException {
        messageAndStateInfo(resp);
        cancelRingingTimer();
        serviceState.doSuccessResponse(resp, this);
    }

    @Override
    public synchronized void doUpdate(SipServletRequest req){
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
    public void setUserSettings(JSONObject settings) {
        serviceConfig = new FcsServiceConfig(settings);
    }

    @Override
    public ServiceConfig getUserSettings() {
        return serviceConfig;
    }

    @Override
    public void setMatchedRule(Rule rule) {
        matchedRule = rule;
    }

    @Override
    public Rule getMatchedRule() {
        return matchedRule;
    }

    @Override
    public SipFactory getSipFactory(){
        return this.sipFactory;
    }

    @Override
    public void setActionSet(ActionSet actionSet) {
        this.actionSet = actionSet;
        currentAction = actionSet.getNextAction();
    }

    @Override
    public void doParallel() throws IOException, ServletParseException, SQLException {
        long ringingPeriod = currentAction.getPeriod();
        List<URI> uris = getTargetAddresses(currentAction.getTargets());
        boolean atLeastOneRequestSent = false;
        for (URI uri :
                uris) {
            List<Binding> bindings
                    = CommonUtils.getRegistrarHelper(getInitialRequest()).getBindings(uri.toString());
            if (bindings != null) {
                callContext.createRequest("INVITE", uri, this).send();
            }


        }

        //Set timer
        notReachableTimer
                = CommonUtils.getTimerService().createTimer(
                callContext.getApplicationSession(),
                ringingPeriod * 1000,
                false,
                null);
    }

    @Override
    public void doSerial() throws ServletParseException, IOException {
        ActionTarget target = currentAction.getNextTarget();

        int ringingPeriod = target.getRingingPeriod();

        if (ringingPeriod != -1) {
            ringingPeriod = currentAction.getPeriod();
        }

        URI uri = getTargetAddress(target);

        callContext.createRequest("INVITE", uri, this).send();

        //Set timer
        notReachableTimer
                = CommonUtils.getTimerService().createTimer(
                callContext.getApplicationSession(),
                ringingPeriod * 1000,
                false,
                null);
    }

    @Override
    public boolean isRingingTimer(ServletTimer timer) {
        if (timer.equals(ringingTimer)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isNotReachableTimer(ServletTimer timer) {
        if (timer.equals(notReachableTimer)) {
            return true;
        }
        return false;
    }

    @Override
    public void doRejectInvite(int code, String message) throws IOException {

        SipServletResponse rejectInviteResponse
                = callContext.getInitialRequest().createResponse(code, message);

        rejectInviteResponse.send();
    }

    @Override
    public boolean sendInvite(String primaryUser) throws SQLException, ServletParseException, IOException {
        Registrar registrar = CommonUtils.getRegistrarHelper(getInitialRequest());
        List<Binding> bindings = registrar.getBindings(primaryUser);

        if (bindings == null) {
            logger.info("No bindings");
            return false;
        }

        URI requestUri = getSipFactory().createURI(bindings.get(0).getBindingURI());

        callContext.createRequest("INVITE", requestUri, this).send();

        notReachableTimer
                = CommonUtils.getTimerService().createTimer(
                callContext.getApplicationSession(),
                defaultNotReachablePeriod * 1000,
                false,
                null);

        return true;
    }

    @Override
    public Action getCurrentAction() {
        return currentAction;
    }

    @Override
    public Action getNextAction() {
        currentAction = actionSet.getNextAction();
        return currentAction;
    }

    @Override
    public synchronized void noAckReceived(SipErrorEvent sipErrorEvent) {
        serviceState.noAckReceived(sipErrorEvent, this);
    }

    @Override
    public ServiceConfig getServiceConfig() {
        return serviceConfig;
    }

    @Override
    public void cancelNotReachableTimer() {
        if (notReachableTimer != null) {
            notReachableTimer.cancel();
        }
        else {
            logger.warn("Trying to cancel not reachable timer, which is NULL");
        }

    }

    @Override
    public void startRingingTimer() {
        ringingTimer
                = CommonUtils.getTimerService().createTimer(
                callContext.getApplicationSession(),
                defaultRinginPeriod * 1000,
                false,
                null);
    }

    @Override
    public boolean isRingingSent() {
        return callContext.isRingingSent();
    }

    @Override
    public void cancelRingingTimer() {
        if (ringingTimer != null){
            ringingTimer.cancel();
        }
        else {
            logger.warn("Trying to cancel not ringing timer, which is NULL");
        }
    }

    @Override
    public synchronized void doTimeout(ServletTimer timer) throws IOException {
        serviceState.doTimeout(timer, this);
    }

    @Override
    public void sendSuccess(SipServletResponse resp) throws IOException, SdpException {
        SipServletResponse response = getInitialRequest().createResponse(SC_OK, "Ok");
        response.setContent(resp.getRawContent(), resp.getContentType());
        SdpUtils.performSdpNegotiation(getInitialRequest(), response);

        response.send();

        getCallContext().setSuccessfulRequest(resp.getRequest());
        getCallContext().setSuccessfulResponse(resp);
    }

    @Override
    public void forwardBye(SipServletRequest receivedByeRequest)
            throws IOException, ServletParseException {

        logger.info("forwarding bye request\n"
                + "initial request = \n"
                + getInitialRequest() );

        SipServletRequest byeReq = null;
        if (receivedByeRequest.getSession() == getInitialRequest().getSession()) {
            //bye received from caller
            byeReq = callContext.createByeToCallee(this);
        }
        else {
            //bye received from callee
            byeReq = callContext.createByeToCaller(this);
        }
        byeReq.send();
    }

    public List<URI> getTargetAddresses(List<ActionTarget> targets) throws ServletParseException {

        List<String> targetAddresses = serviceConfig.getTargetAddresses(targets);
        List<URI> targetURIs = new ArrayList<>();

        for (String address :
                targetAddresses) {
            targetURIs.add(getSipFactory().createURI(address));
        }
        return targetURIs;
    }

    public URI getTargetAddress(ActionTarget target) throws ServletParseException {
        return getSipFactory().createURI(serviceConfig.getTargetAddressByName(target.getName()));
    }

    void messageAndStateInfo(SipServletMessage message) {
        logger.info("Received message: \n" + message
                + "\nIn state: " + serviceState.getClass().getSimpleName());
    }
}
