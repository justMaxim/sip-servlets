package com.berinchik.sip.service.fsm;

import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.URI;
import java.util.List;

/**
 * Created by Maksim on 26.05.2017.
 */
public interface CallContext {

    SipApplicationSession getApplicationSession();
    SipServletRequest getInitialRequest();
    void setInitialRequest(SipServletRequest request);
    List<SipServletRequest> getAllCurrentRequests();
    void addRequest(SipServletRequest req);
    void removeRequest(SipServletRequest request);
    void removeRequests(List<SipServletRequest> requests);
    void removeAllRequestsExceptOne(SipServletRequest request);
    SipServletRequest createRequest(String Method, URI uri, SipServiceContext serviceContext);
}
