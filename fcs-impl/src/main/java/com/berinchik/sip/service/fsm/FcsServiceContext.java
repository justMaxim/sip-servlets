package com.berinchik.sip.service.fsm;

import com.berinchik.sip.config.FcsServiceConfig;
import com.berinchik.sip.config.ServiceConfig;
import com.berinchik.sip.config.action.Action;
import com.berinchik.sip.config.action.ActionSet;
import com.berinchik.sip.config.rule.Rule;
import com.berinchik.sip.config.target.ActionTarget;
import com.berinchik.sip.service.fsm.state.FcsInitialState;
import com.berinchik.sip.service.fsm.state.SipServiceState;
import com.berinchik.sip.service.registrar.Registrar;
import com.berinchik.sip.service.registrar.database.util.Binding;
import com.berinchik.sip.util.CommonUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import javax.servlet.sip.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static javax.servlet.sip.SipServletRequest.*;
import static javax.servlet.sip.SipServletResponse.*;

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
    private ServletTimer currentTimer;

    private int defaultRinginPeriod = 10;
    private int defaultNotReacheableTimer = 10;

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
    public void doInvite(SipServletRequest req) throws SQLException, IOException, ServletParseException {
        if (req.isInitial()) {
            callContext = new FcsCallContext(req);
            serviceState = new FcsInitialState();
        }
        messageAndStateInfo(req);
        serviceState.doInvite(req, this);
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
    public void doParallel() throws IOException, ServletParseException {
        long ringingPeriod = currentAction.getPeriod();
        List<URI> uris = getTargetAddresses(currentAction.getTargets());
        for (URI uri :
                uris) {
            callContext.createRequest("INVITE", uri, this).send();
        }
        //Set timer
        currentTimer
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
        currentTimer
                = CommonUtils.getTimerService().createTimer(
                callContext.getApplicationSession(),
                ringingPeriod * 1000,
                false,
                null);
    }

    @Override
    public void doRejectInvite(int code, String message) throws IOException {

        SipServletResponse rejectInviteResponse
                = callContext.getInitialRequest().createResponse(code, message);

        rejectInviteResponse.send();

    }

    @Override
    public void doForwardInvite(String primaryUser) throws SQLException, ServletParseException, IOException {
        Registrar registrar = CommonUtils.getRegistrarHelper(getInitialRequest());
        List<Binding> bindings = registrar.getBindings(primaryUser);
        URI requestUri = getSipFactory().createURI(bindings.get(0).getBindingURI());

        callContext.createRequest("INVITE", requestUri, this).send();

        currentTimer
                = CommonUtils.getTimerService().createTimer(
                callContext.getApplicationSession(),
                defaultRinginPeriod * 1000,
                false,
                null);
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
    public void noAckReceived(SipErrorEvent sipErrorEvent) {
        serviceState.noAckReceived(sipErrorEvent, this);
    }

    @Override
    public ServiceConfig getServiceConfig() {
        return serviceConfig;
    }

    @Override
    public void doTimeout(ServletTimer timer) {
        serviceState.doTimeout(timer, this);
    }

    void messageAndStateInfo(SipServletMessage message) {
        logger.info("Received message: \n" + message
                + "\nIn state: " + serviceState.getClass().getSimpleName());
    }
}
