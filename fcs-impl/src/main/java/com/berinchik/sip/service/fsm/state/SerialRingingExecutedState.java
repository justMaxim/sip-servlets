package com.berinchik.sip.service.fsm.state;

import com.berinchik.sip.config.action.Action;
import com.berinchik.sip.error.FcsUnexpectedException;
import com.berinchik.sip.service.fsm.SipServiceContext;
import org.mobicents.media.server.io.sdp.SdpException;

import javax.servlet.sip.*;
import java.io.IOException;
import java.sql.SQLException;
import static javax.servlet.sip.SipServletResponse.*;

/**
 * Created by Maksim on 27.05.2017.
 */
public class SerialRingingExecutedState extends BaseState {

    @Override
    public void doCancel(SipServletRequest req, SipServiceContext context)
            throws IOException {
        context.getCallContext().cancelAllOutgoing();
        context.cancelAllTimers();
        context.setState(new InviteCanceledState());
    }

    @Override
    public void doErrorResponse(SipServletResponse resp, SipServiceContext context)
            throws IOException, SQLException, ServletParseException {

        context.getCallContext().removeRequest(resp.getRequest());

        SipServiceState nextState = null;
        Action nextAction = null;

        //All the parallel requests cancelled
        context.cancelAllTimers();

        if (!context.doSerial()) {
            //if there are no more targets in this serial action
            if (context.isFlexible()) {
                nextAction = context.getNextAction();
                if (nextAction != null) {
                    nextState = performAction(nextAction, context);
                }
            }
            if (nextState == null) {
                context.doRejectInvite(resp.getStatus(), resp.getReasonPhrase());
                nextState = new InviteCanceledState();
            }
        }
        context.setState(nextState);
    }

    @Override
    public void doProvisionalResponse(SipServletResponse resp, SipServiceContext context)
            throws IOException {
        if (resp.getStatus() == SC_RINGING) {
            context.cancelNotReachableTimer();
            context.sendRingingToCaller();
            context.startRingingTimer();
        }
    }

    @Override
    public void doSuccessResponse(SipServletResponse resp, SipServiceContext context)
            throws IOException, SdpException {
        context.cancelAllTimers();
        context.getCallContext().removeAllRequestsExceptOne(resp.getRequest());
        context.sendSuccess(resp);
        context.setState(new SentOkForInitialInvite());
    }


    @Override
    public void doTimeout(ServletTimer timer, SipServiceContext context)
            throws IOException, SQLException, ServletParseException {
        SipServiceState nextState = null;

        if(context.isNotReachableTimer(timer)) {
            context.getCallContext().cancelAllInitialOutgoing();
            nextState = doSerial(context, SC_TEMPORARILY_UNAVAILABLE, "Temporarily unavailable");
        }
        else if(context.isRingingTimer(timer)) {
            context.getCallContext().cancelAllOutgoing();
            nextState = doSerial(context, SC_REQUEST_TIMEOUT, "Request timeout");
        }
        else {
            throw new FcsUnexpectedException("Unrecognised timer: " + timer);
        }

        context.setState(nextState);
    }

    private SipServiceState doSerial(SipServiceContext context ,int rejectStatus, String rejectMessage)
            throws IOException, ServletParseException, SQLException {
        SipServiceState nextState = null;
        Action nextAction = null;

        if (!context.doSerial()) {
            //if there are no more targets in this serial action
            if (context.isFlexible()) {
                nextAction = context.getNextAction();
                if (nextAction != null) {
                    nextState = performAction(nextAction, context);
                }
            }
            if (nextState == null) {
                context.doRejectInvite(rejectStatus, rejectMessage);
                nextState = new InviteCanceledState();
            }
        }
        return nextState;
    }

}
