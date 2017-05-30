package com.berinchik.sip.service.fsm.state;

import com.berinchik.sip.service.fsm.SipServiceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.sip.*;
import java.io.IOException;
import java.sql.SQLException;

import static javax.servlet.sip.SipServletResponse.*;

/**
 * Created by Maksim on 27.05.2017.
 */
public class DialogInstalledState extends BaseState {

    private static Log logger = LogFactory.getLog(DialogInstalledState.class);

    @Override
    public void doAck(SipServletRequest req, SipServiceContext context) {

    }

    @Override
    public void doBye(SipServletRequest req, SipServiceContext context) throws IOException, ServletParseException {
        req.createResponse(SC_OK, "Ok").send();
        context.forwardBye(req);
        context.setState(new ByeSentState());
    }

    @Override
    public void doErrorResponse(SipServletResponse resp, SipServiceContext context) {

    }

    @Override
    public void doProvisionalResponse(SipServletResponse resp, SipServiceContext context) {

    }

    @Override
    public void doSuccessResponse(SipServletResponse resp, SipServiceContext context) throws IOException {

    }
}
