package com.berinchik.sip.service.fsm;

import com.berinchik.sip.config.FcsServiceConfig;
import com.berinchik.sip.config.ServiceConfig;
import com.berinchik.sip.config.action.Action;
import com.berinchik.sip.config.action.ActionSet;
import com.berinchik.sip.config.action.ActionSetId;
import com.berinchik.sip.config.rule.Rule;
import com.berinchik.sip.config.target.ActionTarget;
import com.berinchik.sip.service.fsm.state.InitialState;
import com.berinchik.sip.service.fsm.state.SipServiceState;
import com.berinchik.sip.service.registrar.Registrar;
import com.berinchik.sip.service.database.util.Binding;
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
    private ServletTimer notReachableTimer;
    private ServletTimer ringingTimer;
    private SipServletResponse bestResponse;

    private int defaultRingingPeriod = 11;
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
    public synchronized void doErrorResponse(SipServletResponse resp)
            throws IOException, SQLException, ServletParseException {
        messageAndStateInfo(resp);
        if (resp.getStatus() != SC_REQUEST_TERMINATED){
            setBestResponse(resp);
            serviceState.doErrorResponse(resp, this);
        }
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
    public synchronized CallContext getCallContext() {
        return this.callContext;
    }

    @Override
    public void setCallContext(CallContext callContext) {
        this.callContext = callContext;
    }

    @Override
    public synchronized SipServletRequest getInitialRequest() {
        return callContext.getInitialRequest();
    }

    @Override
    public synchronized void setState(SipServiceState state) {
        logger.info("\nstate changes from " + serviceState.getClass().getSimpleName()
                + "\nto " + state.getClass().getSimpleName());
        this.serviceState = state;
    }

    @Override
    public synchronized void setUserSettingsJSON(JSONObject settings) {
        serviceConfig = new FcsServiceConfig(settings);
        defaultNotReachablePeriod = serviceConfig.getNotReachableTimer();
        defaultRingingPeriod = serviceConfig.getDefaultPeriod();
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
    public synchronized void doParallel() throws IOException, ServletParseException, SQLException {

        List<URI> uris = getTargetAddresses(currentAction.getTargets());
        StringBuilder targets = new StringBuilder();
        for (URI uri :
                uris) {
            targets.append(uri.toString() + "\n");
            callContext.createRequest("INVITE", uri, this).send();
        }
        logger.trace("do parallel ringing to targets:\n"
                + targets);

        cancelAllTimers();

        //Set timer
        notReachableTimer
                = CommonUtils.getTimerService().createTimer(
                callContext.getApplicationSession(),
                defaultNotReachablePeriod * 1000,
                false,
                null);
    }

    @Override
    public synchronized boolean doSerial()
            throws ServletParseException, IOException {
        ActionTarget target = currentAction.getNextTarget();

        if (target == null) {
            return false;
        }

        URI uri = getTargetAddress(target);
        callContext.createRequest("INVITE", uri, this).send();

        cancelAllTimers();

        //Set timer
        notReachableTimer
                = CommonUtils.getTimerService().createTimer(
                callContext.getApplicationSession(),
                defaultNotReachablePeriod * 1000,
                false,
                null);

        return true;
    }

    @Override
    public boolean sendRingingToCaller() throws IOException {
        if (isRingingSent()) {
            return false;
        }
        else {
            getInitialRequest().createResponse(SC_RINGING, "Ringing").send();
            getCallContext().setRingingSent();
            return true;
        }
    }

    @Override
    public boolean isRingingTimer(ServletTimer timer) {
        if (timer == ringingTimer) {
            logger.debug("it was ringing timer");
            return true;
        }
        return false;
    }

    @Override
    public boolean isNotReachableTimer(ServletTimer timer) {
        if (timer == notReachableTimer) {
            logger.debug("it was not-reachable timer");
            return true;
        }
        return false;
    }

    @Override
    public synchronized void doRejectInvite(int code, String message) throws IOException {

        if (bestResponse != null) {
            code = bestResponse.getStatus();
            message = bestResponse.getReasonPhrase();
        }
        SipServletResponse rejectInviteResponse
                = callContext.getInitialRequest().createResponse(code, message);

        rejectInviteResponse.send();
    }

    @Override
    public synchronized boolean sendInvite(String primaryUser) throws SQLException, ServletParseException, IOException {
        Registrar registrar = CommonUtils.getRegistrarHelper(getInitialRequest());
        List<Binding> bindings = registrar.getBindings(primaryUser);

        logger.debug("trying to send invite");
        if (bindings == null) {
            logger.debug("No bindings");
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
    public boolean isFlexible() {
        return actionSet.getActionSetId() == ActionSetId.FLEXIBLE_RINGING;
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
    public boolean cancelNotReachableTimer() {
        logger.debug("Cancelling not reachable timer timer");
        if (notReachableTimer != null) {
            notReachableTimer.cancel();
            return true;
        }

        logger.warn("Trying to cancel not reachable timer, which is NULL");

        return false;

    }

    @Override
    public void startRingingTimer() {
        ringingTimer
                = CommonUtils.getTimerService().createTimer(
                callContext.getApplicationSession(),
                defaultRingingPeriod * 1000,
                false,
                null);
    }

    @Override
    public boolean isRingingSent() {
        return callContext.isRingingSent();
    }

    @Override
    public boolean cancelRingingTimer() {
        logger.debug("Cancelling ringing timer");
        if (ringingTimer != null){
            ringingTimer.cancel();
            return true;
        }

        logger.warn("Trying to cancel not ringing timer, which is NULL");
        return false;
    }

    @Override
    public boolean cancelAllTimers() {
        boolean anyTimerCancelled = false;

        logger.debug("cancelling all timers");

        if (cancelNotReachableTimer()) {
            anyTimerCancelled = true;
        }
        if (cancelRingingTimer()) {
            anyTimerCancelled = true;
        }

        return anyTimerCancelled;
    }

    @Override
    public synchronized void doTimeout(ServletTimer timer) throws IOException, SQLException, ServletParseException {
        logger.debug("Received timer: " + timer
                + "\nin state " + serviceState.getClass().getSimpleName());
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

        logger.debug("forwarding bye request\n"
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

    @Override
    public void setBestResponse(SipServletResponse response) {
        logger.debug("Trying to set best response");
        if (response.getStatus() == SC_BUSY_HERE || response.getStatus() == SC_BUSY_EVERYWHERE) {
            logger.debug("setting best response: " + response);
            bestResponse = response;
        }
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
