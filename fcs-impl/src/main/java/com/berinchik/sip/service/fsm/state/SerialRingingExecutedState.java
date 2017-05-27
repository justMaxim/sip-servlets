package com.berinchik.sip.service.fsm.state;

import com.berinchik.sip.service.fsm.SipServiceContext;

import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipErrorEvent;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by Maksim on 27.05.2017.
 */
public class SerialRingingExecutedState implements SipServiceState {
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
    public void doInvite(SipServletRequest req, SipServiceContext context) throws SQLException, IOException {

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
