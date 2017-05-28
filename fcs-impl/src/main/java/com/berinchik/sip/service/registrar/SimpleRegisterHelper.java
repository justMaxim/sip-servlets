package com.berinchik.sip.service.registrar;

import com.berinchik.sip.service.registrar.database.DatabaseAccessor;
import com.berinchik.sip.service.registrar.database.SimpleDatabaseAccessor;
import com.berinchik.sip.service.registrar.database.util.Binding;
import com.berinchik.sip.util.CommonUtils;

import org.json.JSONObject;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.sip.*;

import static javax.servlet.sip.SipServletResponse.SC_OK;

/**
 * Created by Maksim on 24.05.2017.
 */
public class SimpleRegisterHelper implements Registrar {

    private static Log logger = LogFactory.getLog(Registrar.class);

    private DatabaseAccessor dbAccessor;

    public SimpleRegisterHelper() throws SQLException {
        this.dbAccessor = new SimpleDatabaseAccessor();
    }

    @Override
    public JSONObject getServiceConfig(String primaryUserURI) throws SQLException {
        return dbAccessor.getServiceConfigJsonObject(primaryUserURI);
    }

    @Override
    public boolean registerUser(String primaryUserURI, String binding, long expires) throws SQLException {
        if (dbAccessor.userIsPrimary(primaryUserURI)) {
            dbAccessor.deleteBinding(primaryUserURI, binding);
            long expiresTime = CommonUtils.getCurrentTimestampInSeconds() + expires;
            dbAccessor.addBinding(primaryUserURI, binding, expiresTime);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean registerUser(String primaryUserURI, List<String> bindings, long expires) {
        return false;
    }

    @Override
    public boolean isPrimary(String primaryUserURI) throws SQLException {
        return dbAccessor.userIsPrimary(primaryUserURI);
    }

    @Override
    public String isRegistered(String bindingURI) throws SQLException {
        return dbAccessor.isUserRegistered(bindingURI);
    }

    @Override
    public List<Binding> getBindings(String primaryUserURI) throws SQLException{
        return dbAccessor.getUserBindings(primaryUserURI);
    }

    @Override
    public boolean deregisterUser(String primaryUserURI, String binding) throws SQLException {
        return dbAccessor.deleteBinding(primaryUserURI, binding);
    }

    @Override
    public boolean deregisterUser(String primaryUserURI) {
        return dbAccessor.deleteAllBindings(primaryUserURI);
    }

    @Override
    public SipServletResponse createRegisterSuccessResponse(SipServletRequest request)
            throws SQLException, ServletParseException {

        logger.info("Creating 200 Ok for success registration.");
        String cleanToUri = request.getTo().getURI().toString();

        SipServletResponse resp = request.createResponse(SC_OK, "Ok");
        addContactHeaders(resp, getBindings(cleanToUri), CommonUtils.getSipFactory(resp));
        resp.removeHeader(CommonUtils.SC_EXPIRES_HEADER);

        return resp;
    }

    @Override
    public String getPrimaryUserId(String request) throws SQLException {
        if(isPrimary(request)) {
            return request;
        }
        return isRegistered(request);
    }

    private void addContactHeaders(SipServletMessage message,
                                   List<Binding> bindings,
                                   SipFactory sipFactory) throws ServletParseException {
        logger.info("Adding contact headers to the message:\n"
                + message);
        for (Binding binding:
                bindings) {
            logger.info("Binding: \n" + binding);
            long expiresTime = binding.getDuration();
            String contact = binding.getBindingURI();

            if (sipFactory == null) {
                logger.info("sipFactory == null");
            }

            Address contactAddress = sipFactory.createAddress(contact);
            contactAddress.setExpires((int) expiresTime);
            message.setAddressHeader(CommonUtils.SC_CONTACT_HEADER, contactAddress);
        }
    }


}