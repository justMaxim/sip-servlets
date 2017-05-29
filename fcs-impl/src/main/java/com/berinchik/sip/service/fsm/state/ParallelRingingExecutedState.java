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
public class ParallelRingingExecutedState extends BaseState {

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

        if (context.getCallContext().noRequestsLeft()) {
            SipServiceState nextState = null;
            Action nextAction = null;

            //All the parallel requests cancelled
            context.cancelAllTimers();

            if (context.isFlexible()) {
                //if flexible distribution, try to perform next action
                nextAction = context.getNextAction();
                if (nextAction != null) {
                    nextState = performAction(nextAction, context);
                }
                else {
                    context.doRejectInvite(resp.getStatus(), resp.getReasonPhrase());
                    nextState = new InviteCanceledState();
                }
            }
            context.setState(nextState);
            resp.createAck().send();
        }
        //If there are steal unfinished requests, then wait for timers or for responses.
        //No state change needed.
    }

    @Override
    public void doProvisionalResponse(SipServletResponse resp, SipServiceContext context)
            throws IOException {
        if (resp.getStatus() == SC_RINGING) {
            context.sendRingingToCaller();
            context.startRingingTimer();
        }
    }

    @Override
    public void doSuccessResponse(SipServletResponse resp, SipServiceContext context)
            throws IOException, SdpException {
        context.cancelAllTimers();
        context.getCallContext().cancelAllInitialOutgoing();
        context.sendSuccess(resp);
        context.setState(new SentOkForInitialInvite());
    }

    @Override
    public void doTimeout(ServletTimer timer, SipServiceContext context)
            throws IOException, SQLException, ServletParseException {
        SipServiceState nextState;

        if(context.isNotReachableTimer(timer)) {
            context.getCallContext().cancelAllInitialOutgoing();
            nextState = doParallel(context, SC_TEMPORARILY_UNAVAILABLE, "Temporarily unavailable");
        }
        else if(context.isRingingTimer(timer)) {
            context.getCallContext().cancelAllOutgoing();
            nextState = doParallel(context, SC_REQUEST_TIMEOUT, "Temporarily unavailable");
        }
        else {
            throw new FcsUnexpectedException("Unrecognised timer: " + timer);
        }

        context.setState(nextState);
    }

    private SipServiceState doParallel(SipServiceContext context ,int rejectStatus, String rejectMessage)
            throws IOException, SQLException, ServletParseException {
        SipServiceState nextState = null;
        Action nextAction = null;

        if (context.isFlexible()) {
            //if flexible distribution, try to perform next action
            nextAction = context.getNextAction();

            if (nextAction != null) {
                context.getInitialRequest().createResponse(SC_TRYING, "Trying");
                nextState = performAction(nextAction, context);
            }
            else {
                context.doRejectInvite(rejectStatus, rejectMessage);
                nextState = new InviteCanceledState();
            }
        }

        return nextState;
    }
}
