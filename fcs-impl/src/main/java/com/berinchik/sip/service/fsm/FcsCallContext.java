package com.berinchik.sip.service.fsm;

import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Maksim on 27.05.2017.
 */
public class FcsCallContext implements CallContext {

    SipServletRequest initialRequest;
    List<SipServletRequest> currentRequests;

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

    }

    @Override
    public void removeRequests(List<SipServletRequest> requests) {

    }

    @Override
    public void removeAllRequestsExceptOne(SipServletRequest request) {

    }

    @Override
    public SipServletRequest createRequest(String Method, URI toURI, SipServiceContext serviceContext) {

        SipServletRequest newRequest
                = serviceContext.getSipFactory().createRequest(
                        initialRequest.getApplicationSession(), Method, initialRequest.getFrom().getURI(), toURI);

        addRequest(newRequest);

        return newRequest;
    }
}
