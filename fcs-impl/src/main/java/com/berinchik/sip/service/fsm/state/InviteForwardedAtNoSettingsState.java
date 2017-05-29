package com.berinchik.sip.service.fsm.state;

import com.berinchik.sip.error.FcsUnexpectedException;
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
public class InviteForwardedAtNoSettingsState extends BaseState {

    private static Log logger = LogFactory.getLog(InviteForwardedAtNoSettingsState.class);

    @Override
    public void doAck(SipServletRequest req, SipServiceContext context) {
        throw new IllegalStateException("Bye received in early dialog state");
    }

    @Override
    public void doBye(SipServletRequest req, SipServiceContext context) {
        throw new IllegalStateException("Bye received in early dialog state");
    }

    @Override
    public void doCancel(SipServletRequest req, SipServiceContext context) throws IOException {
        context.getCallContext().cancelAllOutgoing();
        context.setState(new InviteCanceledState());
    }

    @Override
    public void doErrorResponse(SipServletResponse resp, SipServiceContext context) throws IOException, SQLException, ServletParseException {
        logger.info("Processing error response" + resp.getReasonPhrase());
        context.getInitialRequest().createResponse(resp.getStatus(), resp.getReasonPhrase()).send();
        context.setState(new InviteCanceledState());
    }

    @Override
    public void doInvite(SipServletRequest req, SipServiceContext context) throws SQLException, IOException, ServletParseException {
        //Re-invite not implemented
        throw new UnsupportedOperationException("Re-invite is not implemented");
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
        throw new UnsupportedOperationException("Redirect currently unsupported");
    }

    @Override
    public void doSubscribe(SipServletRequest req, SipServiceContext context) {
        throw new UnsupportedOperationException("Subscribe is not supported");
    }

    @Override
    public void doSuccessResponse(SipServletResponse resp, SipServiceContext context)
            throws IOException, SdpException {
        context.sendSuccess(resp);
        context.setState(new SentOkForInitialInvite());
    }

    @Override
    public void doUpdate(SipServletRequest req, SipServiceContext context) {
        throw new UnsupportedOperationException("Update is not supported");
    }

    @Override
    public void noAckReceived(SipErrorEvent sipErrorEvent, SipServiceContext context) {
        throw new UnsupportedOperationException("No ack received");
        //notify 200 Ok sender, that ack was not received
    }

    @Override
    public void doTimeout(ServletTimer timer, SipServiceContext context) throws IOException, ServletParseException, SQLException {
        logger.info("timeout received " + timer);
        context.getCallContext().cancelAllOutgoing();
        if (context.isRingingTimer(timer)) {
            context.doRejectInvite(SC_REQUEST_TIMEOUT, "Request timeout");

        }
        else if (context.isNotReachableTimer(timer)) {
            context.doRejectInvite(SC_TEMPORARILY_UNAVAILABLE, "Temporarily unavailable");
        }
        else {
            context.getCallContext().cancelAllOutgoing();
            context.doRejectInvite(SC_SERVER_INTERNAL_ERROR, "Server internal error");
            throw new FcsUnexpectedException("Timer fired, but no ringing and no unavailable");
        }
        context.setState(new InviteCanceledState());
        timer.cancel();
    }
}
