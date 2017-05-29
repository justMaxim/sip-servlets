package com.berinchik.sip.service.fsm;

import com.berinchik.sip.util.CommonUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.sip.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Maksim on 27.05.2017.
 */
public class FcsCallContext implements CallContext {

    private static Log logger = LogFactory.getLog(FcsCallContext.class);


    private SipServletRequest initialRequest;
    private List<SipServletRequest> currentRequests;
    private SipServletRequest successfulRequest;
    private SipServletResponse successResponse;
    private SipServletRequest byeRequest;
    private boolean isRingingSent = false;

    public FcsCallContext(SipServletRequest initialRequest) {
        this.initialRequest = initialRequest;
        currentRequests = new ArrayList<>();
    }

    @Override
    public SipApplicationSession getApplicationSession() {
        return initialRequest.getApplicationSession();
    }

    @Override
    public SipServletRequest getInitialRequest() {
        return initialRequest;
    }

    @Override
    public void setInitialRequest(SipServletRequest request) {
        initialRequest = request;
    }

    @Override
    public List<SipServletRequest> getAllCurrentRequests() {
        return currentRequests;
    }

    @Override
    public void addRequest(SipServletRequest req) {
        currentRequests.add(req);

    }

    @Override
    public void removeRequest(SipServletRequest request) {
        currentRequests.remove(request);
    }

    @Override
    public void removeRequests(List<SipServletRequest> requests) {
        currentRequests.removeAll(requests);
    }

    @Override
    public void removeAllRequestsExceptOne(SipServletRequest request) {
        currentRequests.clear();
        currentRequests.add(request);
    }

    @Override
    public SipServletRequest createRequest(String Method, URI toURI, SipServiceContext serviceContext)
            throws IOException {

        SipServletRequest newRequest
                = serviceContext.getSipFactory().createRequest(
                        initialRequest.getApplicationSession(), Method, initialRequest.getFrom().getURI(), toURI);

        newRequest.setContent(initialRequest.getContent(), initialRequest.getContentType());

        addRequest(newRequest);

        return newRequest;
    }

    @Override
    public SipServletRequest createByeToCallee(SipServiceContext context) {
        this.byeRequest = successfulRequest.getSession().createRequest("BYE");
        return byeRequest;
    }

    @Override
    public SipServletRequest createByeToCaller(SipServiceContext context) throws ServletParseException {
        this.byeRequest = initialRequest.getSession().createRequest("BYE");
        return byeRequest;
    }

    @Override
    public boolean noRequestsLeft() {
        return currentRequests.isEmpty();
    }

    @Override
    public void setSuccessfulResponse(SipServletResponse resp) {
        successResponse = resp;
    }

    @Override
    public SipServletResponse getSuccessfulResponse() {
        return successResponse;
    }

    @Override
    public void setByeRequest(SipServletRequest byeRequest) {
        this.byeRequest = byeRequest;
    }

    @Override
    public SipServletRequest getByeRequest() {
        return byeRequest;
    }

    @Override
    public boolean isRingingSent() {
        return this.isRingingSent;
    }

    @Override
    public void setRingingSent() {
        isRingingSent = true;
    }

    @Override
    public void cancelAllOutgoing() throws IOException {
        for (SipServletRequest request:
             currentRequests) {
             request.createCancel().send();
        }
        currentRequests.clear();
    }

    @Override
    public void cancelAllInitialOutgoing() throws IOException {
        for (SipServletRequest request:
                currentRequests) {
            if(request.getSession().getState() == SipSession.State.INITIAL) {
                request.createCancel().send();
                removeRequest(request);
            }
        }
    }

    public void setSuccessfulRequest(SipServletRequest req) {
        successfulRequest = req;
    }

    public SipServletRequest getSuccessfulRequest() {
        return successfulRequest;
    }


}
