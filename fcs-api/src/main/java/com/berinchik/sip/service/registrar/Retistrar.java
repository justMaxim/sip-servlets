package com.berinchik.sip.service.registrar;

import com.berinchik.sip.service.registrar.database.util.UserInfo;
import com.berinchik.sip.config.ServiceConfig;

import javax.servlet.sip.Address;
import javax.servlet.sip.URI;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Maksim on 24.05.2017.
 */
public interface Retistrar {
    public ServiceConfig getServiceConfig(URI primaryUserURI) throws SQLException;
    public boolean registerUser(URI primaryUserURI, Address binding, long expires);
    public boolean registerUser(URI primaryUserURI, List<Address> binding, long expires);
    public boolean isRegistered(URI primaryUserURI);
    public List<Address> getBindings(URI primaryUserURI);
}
