package com.berinchik.sip.service.registrar.database;

import org.json.JSONObject;

import javax.servlet.sip.Address;
import javax.servlet.sip.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Maksim on 24.05.2017.
 */
public interface DatabaseAccessor {
    JSONObject getServiceConfigJsonObject(URI primaryUserURI) throws SQLException;
    List<Address> getUserBindings(URI primaryUserURI);

    /**
     * checks if userURI is binded to any primary user
     * @param userURI
     * @return
     */
    public String isUserRegistered(URI userURI) throws SQLException;
    public boolean userIsPrimary(URI primaryUserURI) throws SQLException;
    public boolean deleteBinding(URI primaryUserURI, Address binding) throws SQLException;
    public boolean addBinding(URI primaryUserURI, Address binding, long expires);

}
