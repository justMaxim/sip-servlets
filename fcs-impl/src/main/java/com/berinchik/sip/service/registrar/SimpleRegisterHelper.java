package com.berinchik.sip.service.registrar;

import com.berinchik.sip.config.FlexibleServiceConfigureation;
import com.berinchik.sip.service.registrar.database.DatabaseAccessor;
import com.berinchik.sip.service.registrar.database.SimpleDatabaseAccessor;
import com.berinchik.sip.service.registrar.database.util.UserInfo;
import com.berinchik.sip.config.ServiceConfig;
import org.json.JSONObject;

import javax.servlet.sip.Address;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.URI;
import java.util.Date;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Maksim on 24.05.2017.
 */
public class SimpleRegisterHelper implements Retistrar{

    private DatabaseAccessor dbAccessor;

    private SipFactory sipFactory;

    public SimpleRegisterHelper(SipFactory sipFactory) throws SQLException {
        this.sipFactory = sipFactory;
        this.dbAccessor = new SimpleDatabaseAccessor();
    }

    @Override
    public ServiceConfig getServiceConfig(URI primaryUserURI) throws SQLException {

        JSONObject serviceConfigJSON = dbAccessor.getServiceConfigJsonObject(primaryUserURI);

        return new FlexibleServiceConfigureation(serviceConfigJSON);
    }

    @Override
    public boolean registerUser(URI primaryUserURI, Address binding, long expires) {

        if (dbAccessor.userIsPrimary(primaryUserURI)) {
            dbAccessor.deleteBinding(primaryUserURI, binding);
            long expiresTime = (new Date().getTime() / 1000) + expires;
            dbAccessor.addBinding(primaryUserURI, binding, expiresTime);
        }
        return false;
    }

    @Override
    public boolean registerUser(URI primaryUserURI, List<Address> bindings, long expires) {
        return false;
    }

    @Override
    public boolean isRegistered(URI primaryUserURI) {
        return dbAccessor.userRegistered(primaryUserURI) != null;
    }

    @Override
    public List<Address> getBindings(URI primaryUserURI) {

        return dbAccessor.getUserBindings(primaryUserURI);
    }
}
