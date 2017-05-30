package com.berinchik.sip.service.fsm.state;

import com.berinchik.sip.config.action.Action;
import com.berinchik.sip.error.FcsUnexpectedException;
import com.berinchik.sip.service.fsm.SipServiceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.media.server.io.sdp.SdpException;

import javax.servlet.sip.*;
import java.io.IOException;
import java.sql.SQLException;
import static javax.servlet.sip.SipServletResponse.*;

/**
 * Created by Maksim on 27.05.2017.
 */
public class SerialRingingExecutedState extends BaseState {

    private static Log logger = LogFactory.getLog(SerialRingingExecutedState.class);

    @Override
    public void doErrorResponse(SipServletResponse resp, SipServiceContext context)
            throws IOException, SQLException, ServletParseException {

        logger.debug("Received error response in serial ringing mode");
        context.getCallContext().removeRequest(resp.getRequest());

        SipServiceState nextState = null;
        Action nextAction = null;
        context.cancelAllTimers();

        logger.debug("Trying to invite next serial target");
        if (!context.doSerial()) {
            logger.debug("No more serial targets");
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
            context.setState(nextState);
        }
        //next serial target called, no state change
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
            context.getCallContext().cancelAllOutgoing();
            nextState = executeTimeoutAction(context, SC_TEMPORARILY_UNAVAILABLE, "Temporarily unavailable");
        }
        else if(context.isRingingTimer(timer)) {
            context.getCallContext().cancelAllOutgoing();
            nextState = executeTimeoutAction(context, SC_REQUEST_TIMEOUT, "Request timeout");
        }
        else {
            throw new FcsUnexpectedException("Unrecognised timer: " + timer);
        }

        context.setState(nextState);
    }

    private SipServiceState executeTimeoutAction(SipServiceContext context,
                                                 int rejectStatus,
                                                 String rejectMessage)
            throws IOException, ServletParseException, SQLException {
        SipServiceState nextState = null;
        if (!context.doSerial()) {
            logger.debug("No more serial targets");
            //if there are no more targets in this serial action
            Action nextAction = null;
            if (context.isFlexible()) {
                logger.debug("Trying to get next flexible action");
                nextAction = context.getNextAction();
                if (nextAction != null) {
                    logger.debug("Next action is: " + nextAction.getActionId());
                    nextState = performAction(nextAction, context);
                }
            }
            if (nextState == null) {
                logger.debug("No more flexible actions");
                context.doRejectInvite(rejectStatus, rejectMessage);
                nextState = new InviteCanceledState();
            }
        }
        else {
            //next serial target called, not changing state;
            nextState = this;
        }
        return nextState;
    }

}
