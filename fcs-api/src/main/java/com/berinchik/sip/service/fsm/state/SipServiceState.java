package com.berinchik.sip.service.fsm.state;

import com.berinchik.sip.service.fsm.SipServiceContext;

import javax.servlet.sip.*;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by Maksim on 26.05.2017.
 */
public interface SipServiceState {
    void doAck(SipServletRequest req, SipServiceContext context);

    void doBye(SipServletRequest req, SipServiceContext context);

    void doCancel(SipServletRequest req, SipServiceContext context);

    void doErrorResponse(SipServletResponse resp, SipServiceContext context);

    void doInvite(SipServletRequest req, SipServiceContext context) throws SQLException, IOException, ServletParseException;

    void doProvisionalResponse(SipServletResponse resp, SipServiceContext context);

    void doRedirectResponse(SipServletResponse resp, SipServiceContext context) ;

    void doSubscribe(SipServletRequest req, SipServiceContext context);

    void doSuccessResponse(SipServletResponse resp, SipServiceContext context) throws IOException;

    void doUpdate(SipServletRequest req, SipServiceContext context);

    void noAckReceived(SipErrorEvent sipErrorEvent, SipServiceContext context);

    void doTimeout(ServletTimer timer, SipServiceContext context);
}
