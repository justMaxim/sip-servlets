package com.berinchik.sip.service.fsm;

import javax.servlet.sip.SipServletRequest;
import java.util.List;

/**
 * Created by Maksim on 26.05.2017.
 */
public interface CallContext {
    SipServletRequest getInitialRequest();
    List<SipServletRequest> getAllCurrentRequests();
    void addRequest();
    void removeRequest(SipServletRequest request);
    void removeRequests(List<SipServletRequest> requests);
    void removeAllRequestsExceptOne(SipServletRequest request);
}
