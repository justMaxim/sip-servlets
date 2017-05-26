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
}