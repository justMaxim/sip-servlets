package com.berinchik.sip.service.fsm.state;

import com.berinchik.sip.service.fsm.SipServiceContext;

import javax.servlet.sip.*;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.media.server.io.sdp.SdpException;

import static javax.servlet.sip.SipServletResponse.*;

/**
 * Created by Maksim on 26.05.2017.
 */
public class InviteForwardedAtNoSettingsState implements SipServiceState {

    private static Log logger = LogFactory.getLog(InviteForwardedAtNoSettingsState.class);

    @Override
    public void doAck(SipServletRequest req, SipServiceContext context) {

    }

    @Override
    public void doBye(SipServletRequest req, SipServiceContext context) {

    }

    @Override
    public void doCancel(SipServletRequest req, SipServiceContext context) throws IOException {
        context.getCallContext().cancelAll();
        context.setState(new InviteCanceledState());
    }

    @Override
    public void doErrorResponse(SipServletResponse resp, SipServiceContext context) throws  IOException {
        logger.info("Processing error response" + resp.getReasonPhrase());
        context.getInitialRequest().createResponse(resp.getStatus(), resp.getReasonPhrase()).send();
    }

    @Override
    public void doInvite(SipServletRequest req, SipServiceContext context) throws SQLException, IOException, ServletParseException {

    }

    @Override
    public void doProvisionalResponse(SipServletResponse resp, SipServiceContext context) throws IOException {
        logger.info("Processing provisional response: " + resp.getReasonPhrase());
        if(resp.getStatus() == SipServletResponse.SC_RINGING && !context.getCallContext().isRingingSent()) {
            context.getInitialRequest().createResponse(SC_RINGING, "Ringing").send();
            context.getCallContext().setRingingSent();
            logger.info("Sending ringing to the initial request");
            context.cancelNotReachableTimer();
            context.startRingingTimer();
        }
    }

    @Override
    public void doRedirectResponse(SipServletResponse resp, SipServiceContext context) {

    }

    @Override
    public void doSubscribe(SipServletRequest req, SipServiceContext context) {

    }

    @Override
    public void doSuccessResponse(SipServletResponse resp, SipServiceContext context)
            throws IOException, SdpException {
        context.sendSuccess(resp);
        context.setState(new SentOkForInitialInvite());
    }

    @Override
    public void doUpdate(SipServletRequest req, SipServiceContext context) {

    }

    @Override
    public void noAckReceived(SipErrorEvent sipErrorEvent, SipServiceContext context) {

    }

    @Override
    public void doTimeout(ServletTimer timer, SipServiceContext context) {

    }
}
