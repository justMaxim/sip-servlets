package com.berinchik.sip.service.fsm.state;

import com.berinchik.sip.service.fsm.SipServiceContext;
import com.berinchik.sip.util.SdpUtils;

import javax.servlet.ServletException;
import javax.servlet.sip.SipErrorEvent;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import java.io.IOException;

import static javax.servlet.sip.SipServletResponse.*;

/**
 * Created by Maksim on 26.05.2017.
 */
public class InitialInviteForwardedState implements SipServiceState  {
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
        //TODO: Implement reinvite;
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
    public void doSuccessResponse(SipServletResponse resp, SipServiceContext context) throws IOException {
        SipServletRequest initialRequest = context.getInitialRequest();

        if (SdpUtils.porformSdpNegotiation(initialRequest, resp)) {
            SipServletResponse successResponse = initialRequest.createResponse(SC_OK);
            successResponse.setContent(resp.getContent(), "");
            successResponse.send();
            context.setState(new SentOkForInitialInvite());
            return;
        }
        else {
            resp.getRequest().createCancel();
        }
    }

    @Override
    public void doUpdate(SipServletRequest req, SipServiceContext context) {
        //TODO: Do update
        /*
        UPDATE allows a client(caller) to update parameters of a session
        (such as the set of media streams and their codecs)
        but has no impact on the state of a dialog.
        In that sense, it is like a re-INVITE, but unlike re-INVITE, it can be sent before
        the initial INVITE has been completed.  This makes it very useful for
        updating session parameters within early dialogs.
         */
    }

    @Override
    public void noAckReceived(SipErrorEvent sipErrorEvent, SipServiceContext context) {
    }

    @Override
    public void doTimeout() {
        //TODO: Not recheable
    }
}
