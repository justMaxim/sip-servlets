package com.berinchik.sip.service.fsm;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import java.io.IOException;

/**
 * Created by Maksim on 26.05.2017.
 */
public interface SipServiceContext {

    void doAck(SipServletRequest req) throws ServletException, IOException;

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
