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
public class NoSettingsState extends BaseState {

    private static Log logger = LogFactory.getLog(NoSettingsState.class);


    @Override
    public void doErrorResponse(SipServletResponse resp, SipServiceContext context) throws IOException, SQLException, ServletParseException {
        logger.info("Processing error response: " + resp.getStatus() + " " + resp.getReasonPhrase());
        context.getInitialRequest().createResponse(resp.getStatus(), resp.getReasonPhrase()).send();
        context.setState(new InviteCanceledState());
    }

    @Override
    public void doProvisionalResponse(SipServletResponse resp, SipServiceContext context) throws IOException {
        logger.info("Processing provisional response: " + resp.getStatus() + " " + resp.getReasonPhrase());
        if(resp.getStatus() == SipServletResponse.SC_RINGING) {
            context.sendRingingToCaller();
            logger.debug("Sending ringing to the initial request");
            context.cancelNotReachableTimer();
            context.startRingingTimer();
        }
    }

    @Override
    public void doSuccessResponse(SipServletResponse resp, SipServiceContext context)
            throws IOException, SdpException {
        context.sendSuccess(resp);
        context.setState(new SentOkForInitialInvite());
    }

    @Override
    public void doTimeout(ServletTimer timer, SipServiceContext context) throws IOException, ServletParseException, SQLException {
        logger.debug("timeout received " + timer);
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
