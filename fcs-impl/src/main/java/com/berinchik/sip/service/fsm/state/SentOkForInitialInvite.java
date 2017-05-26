package com.berinchik.sip.service.fsm.state;

import com.berinchik.sip.service.fsm.SipServiceContext;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import java.io.IOException;

/**
 * Created by Maksim on 26.05.2017.
 */
public class SentOkForInitialInvite implements SipServiceState {
    @Override
    public void doAck(SipServletRequest req, SipServiceContext context) {

    }

    @Override
    public void doBye(SipServletRequest req, SipServiceContext context) {

    }

    @Override
    public void doCancel(SipServletRequest req, SipServiceContext context) {

    }

    @Override
    public void doErrorResponse(SipServletResponse resp, SipServiceContext context) {

    }

    @Override
    public void doInvite(SipServletRequest req, SipServiceContext context) {

    }

    @Override
    public void doProvisionalResponse(SipServletResponse resp, SipServiceContext context) {

    }

    @Override
    public void doRedirectResponse(SipServletResponse resp, SipServiceContext context) {

    }

    @Override
    public void doSubscribe(SipServletRequest req, SipServiceContext context) {

    }

    @Override
    public void doSuccessResponse(SipServletResponse resp, SipServiceContext context) {

    }

    @Override
    public void doUpdate(SipServletRequest req, SipServiceContext context) {

    }

    @Override
    public void doTimeout() {

    }
}
