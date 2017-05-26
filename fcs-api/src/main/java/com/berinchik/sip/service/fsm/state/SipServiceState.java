package com.berinchik.sip.service.fsm.state;

import com.berinchik.sip.service.fsm.SipServiceContext;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import java.io.IOException;

/**
 * Created by Maksim on 26.05.2017.
 */
public interface SipServiceState {
    void doAck(SipServletRequest req, SipServiceContext context) throws ServletException, IOException;

    void doBye(SipServletRequest req) throws ServletException, IOException;

    void doCancel(SipServletRequest req) throws ServletException, IOException;

    void doErrorResponse(SipServletResponse resp) throws ServletException, IOException;

    void doInvite(SipServletRequest req) throws ServletException, IOException;

    void doProvisionalResponse(SipServletResponse resp) throws ServletException, IOException;

    void doRedirectResponse(SipServletResponse resp) throws ServletException, IOException ;

    void doSubscribe(SipServletRequest req) throws ServletException, IOException;

    void doSuccessResponse(SipServletResponse resp) throws ServletException, IOException;

    void doUpdate(SipServletRequest req) throws ServletException, IOException;
}
