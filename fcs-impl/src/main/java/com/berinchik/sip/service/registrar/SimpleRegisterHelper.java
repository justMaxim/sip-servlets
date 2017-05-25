package com.berinchik.sip.service.registrar;

import com.berinchik.sip.service.registrar.database.DatabaseAccessor;

import com.berinchik.sip.service.registrar.database.SimpleDatabaseAccessor;

import com.berinchik.sip.service.registrar.database.util.Binding;
import org.json.JSONObject;

import java.util.Date;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    public RegistrationStatus registerUser(String primaryUserURI, String binding, long expires) {

        try {
            if (dbAccessor.userIsPrimary(primaryUserURI)) {
                dbAccessor.deleteBinding(primaryUserURI, binding);
                long expiresTime = (new Date().getTime() / 1000) + expires;
                dbAccessor.addBinding(primaryUserURI, binding, expiresTime);
                return RegistrationStatus.OK;
            } else {
                return RegistrationStatus.USER_NOT_FOUND;
            }
        } catch (SQLException e) {
            logger.error("SQL exception during registration", e);
            return RegistrationStatus.ERROR;
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
    public RegistrationStatus deregisterUser(String primaryUserURI, String binding) {
        return null;
    }

    @Override
    public RegistrationStatus deregisterUser(String primaryUserURI) {
        return null;
    }
}