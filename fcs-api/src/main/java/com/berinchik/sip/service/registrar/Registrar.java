package com.berinchik.sip.service.registrar;

import org.json.JSONObject;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Maksim on 24.05.2017.
 */
public interface Registrar {



    public JSONObject getServiceConfig(String primaryUserURI) throws SQLException;


    /**
     * Gets all binded contact of specified primary user
     *
     * @param primaryUserURI clean URI of primary user
     * @return List of binded users or null
     */
    public List<String> getBindings(String primaryUserURI) throws SQLException;

    /**
     *
     * @param primaryUserURI
     * @param binding
     * @param expires
     * @return
     */
    public RegistrationStatus registerUser(String primaryUserURI, String binding, long expires);

    /**
     * Removes specified binding to the primary user form database
     *
     * @param primaryUserURI clean URI of primary user
     * @param binding clean URI of binding contact
     * @return RegistrationStatus enumeration
     */
    public RegistrationStatus deregisterUser(String primaryUserURI, String binding);

    /**
     * Removes all contacts binded to this URI
     * @param primaryUserURI clean URI of primary user
     * @return RegistrationStatus enumeration
     */
    public RegistrationStatus deregisterUser(String primaryUserURI);

    public boolean registerUser(String primaryUserURI, List<String> binding, long expires);

    /**
     * Returns true, if primaryUserURI is URI of primary user in database
     *
     * @param primaryUserURI clean URI of primary user
     */
    public boolean isPrimary(String primaryUserURI) throws SQLException;

    /**
     * Returns URI of primary user to whom bindingAddress is registered,
     * or null if not registered.
     *
     * @param bindingAddress  clean URI of contact in register message
     * @return URI of primary user
     * @throws SQLException error while database access
     */
    public String isRegistered(String bindingAddress) throws SQLException;

}
