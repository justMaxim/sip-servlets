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
public class ParallelRingingExecutedState extends BaseState {

    private static Log logger = LogFactory.getLog(ParallelRingingExecutedState.class);

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

        logger.debug("check if there are no more ongoing requests");
        if (context.getCallContext().noRequestsLeft()) {
            logger.debug("there are no more ongoing requests");
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
            return;
        }
        logger.debug("There are steal ongoing requests: "
                + context.getCallContext().getAllCurrentRequests().size());
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
        SipServiceState nextState = null;

        //fixme: fix this method
        if(context.isNotReachableTimer(timer)) {
            logger.debug("Canceling initial dialogs");
            context.getCallContext().cancelAllInitialOutgoing();
            if (!context.getCallContext().hasEarlyOutgoingDialogs()) {
                //Если нет ранних диалогов
                nextState = executeTimeoutAction(context, SC_TEMPORARILY_UNAVAILABLE, "Temporarily unavailable");
            }
            //Если есть ранние диалоги, то звонок продолжается в том же состоянии
        }
        else if(context.isRingingTimer(timer)) {
            if (!context.getCallContext().hasEarlyOutgoingDialogs()) {
                //Если нет ранних диалогов
            }
            context.getCallContext().cancelAllOutgoing();
            nextState = executeTimeoutAction(context, SC_REQUEST_TIMEOUT, "Ringing timeout");
        }
        else {
            throw new FcsUnexpectedException("Unrecognised timer: " + timer);
        }
        //fixme: nextState stays null, bad logic
        context.setState(nextState);
    }

    private SipServiceState executeTimeoutAction(SipServiceContext context , int rejectStatus, String rejectMessage)
            throws IOException, SQLException, ServletParseException {

        //fixme: bad logic
        SipServiceState nextState = null;
        Action nextAction = null;

        if (context.isFlexible()) {
            //Если flexible, попытаться выполнить следующее действие
            nextAction = context.getNextAction();
            logger.debug("Flexible communication");
            if (nextAction != null) {
                logger.debug("next action != null");
                nextState = performAction(nextAction, context);
            }
            else {
                logger.debug("next action == null");
                context.doRejectInvite(rejectStatus, rejectMessage);
                nextState = new InviteCanceledState();
            }
        }
        else {
            logger.debug("not flexible");
            context.doRejectInvite(rejectStatus, rejectMessage);
            nextState = new InviteCanceledState();
        }

        return nextState;
    }
}
