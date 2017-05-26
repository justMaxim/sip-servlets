package com.berinchik.sip.service.fsm.state;

import com.berinchik.sip.service.fsm.SipServiceContext;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import java.io.IOException;

/**
 * Created by Maksim on 26.05.2017.
 */
public class FcsInitialState implements SipServiceState  {
    @Override
    public void doAck(SipServletRequest req, SipServiceContext context) throws ServletException, IOException {

    }

    @Override
    public void doBye(SipServletRequest req) throws ServletException, IOException {

    }

    @Override
    public void doCancel(SipServletRequest req) throws ServletException, IOException {

    }

    @Override
    public void doErrorResponse(SipServletResponse resp) throws ServletException, IOException {

    }

    @Override
    public void doInvite(SipServletRequest req) throws ServletException, IOException {

    }

    @Override
    public void doProvisionalResponse(SipServletResponse resp) throws ServletException, IOException {

    }

    @Override
    public void doRedirectResponse(SipServletResponse resp) throws ServletException, IOException {

    }

    @Override
    public void doSubscribe(SipServletRequest req) throws ServletException, IOException {

    }

    @Override
    public void doSuccessResponse(SipServletResponse resp) throws ServletException, IOException {

    }

    @Override
    public void doUpdate(SipServletRequest req) throws ServletException, IOException {

    }

    @Override
    public void doTimeout() {

    }
}
