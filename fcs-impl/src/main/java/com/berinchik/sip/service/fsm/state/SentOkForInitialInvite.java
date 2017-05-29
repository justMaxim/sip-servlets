package com.berinchik.sip.service.fsm.state;

import com.berinchik.sip.service.fsm.SipServiceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.sip.*;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by Maksim on 26.05.2017.
 */
public class SentOkForInitialInvite extends BaseState {

    private static Log logger = LogFactory.getLog(SentOkForInitialInvite.class);

    @Override
    public void doAck(SipServletRequest req, SipServiceContext context) throws IOException {
        logger.info("ACK received: dialog success");
        SipServletRequest ack = context.getCallContext().getSuccessfulResponse().createAck();
        if (req.getContent() != null) {
            ack.setContent(req.getContent(), req.getContentType());
        }
        ack.send();
        context.cancelRingingTimer();
        context.setState(new DialogInstalledState());
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
    public void doInvite(SipServletRequest req, SipServiceContext context) throws SQLException, IOException, ServletParseException {

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
