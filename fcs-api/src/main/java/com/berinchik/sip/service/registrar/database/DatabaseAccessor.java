package com.berinchik.sip.service.registrar.database;

import com.berinchik.sip.service.registrar.database.util.Binding;
import org.json.JSONObject;
import org.postgresql.ds.PGConnectionPoolDataSource;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Maksim on 24.05.2017.
 */
public interface DatabaseAccessor {
    JSONObject getServiceConfigJsonObject(String primaryUserURI) throws SQLException;
    List<Binding> getUserBindings(String primaryUserURI) throws SQLException;
    public String isUserRegistered(String binding) throws SQLException;
    public boolean userIsPrimary(String primaryUserURI) throws SQLException;
    public boolean deleteBinding(String primaryUserURI, String binding) throws SQLException;
    public boolean addBinding(String primaryUserURI, String binding, long expires) throws SQLException;
    public void setDataSource(PGConnectionPoolDataSource dataSource);

}
