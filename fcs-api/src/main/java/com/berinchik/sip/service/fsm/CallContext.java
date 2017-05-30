package com.berinchik.sip.service.fsm;

import javax.servlet.sip.*;
import java.io.IOException;
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
    SipServletRequest createRequest(String Method, URI uri, SipServiceContext serviceContext) throws IOException;

    SipServletRequest createByeToCallee(SipServiceContext context);

    SipServletRequest createByeToCaller(SipServiceContext context) throws ServletParseException;

    boolean hasEarlyOutgoingDialogs();

    void cancelAllOutgoing() throws IOException;

    void cancelAllInitialOutgoing() throws IOException;

    void setSuccessfulRequest(SipServletRequest req);
    SipServletRequest getSuccessfulRequest();

    boolean noRequestsLeft();

    void setSuccessfulResponse(SipServletResponse resp);
    SipServletResponse getSuccessfulResponse();
    public void setByeRequest(SipServletRequest byeRequest);
    public SipServletRequest getByeRequest();
    public boolean isRingingSent();

    void setRingingSent();
}
