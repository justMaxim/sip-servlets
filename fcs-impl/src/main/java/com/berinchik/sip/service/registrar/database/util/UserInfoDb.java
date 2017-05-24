package com.berinchik.sip.service.registrar.database.util;

import com.berinchik.sip.service.registrar.database.SimpleDatabaseAccessor;
import org.json.JSONObject;

import javax.servlet.sip.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Created by Maksim on 24.05.2017.
 */
public class UserInfoDb implements UserInfo {
    URI userURI;
    JSONObject settings;
    SimpleDatabaseAccessor accessor;
    Function

    BindedUserInfo(URI userURI, JSONObject settings, SimpleDatabaseAccessor accessor) {
        this.userURI = userURI;
        this.settings = settings;
        this.getBindingsStatement = getBindingsStatement;
    }

    private List<Binding> getBindings(ResultSet resultSet) throws SQLException {
        List<Binding> bindings = new ArrayList<Binding>();
        if (resultSet.next()){
            String address = resultSet.getString(SimpleDatabaseAccessor)
        }
        else return null;
    }


    @Override
    public URI getURI() {
        return userURI;
    }

    @Override
    public JSONObject getSettings() {
        return settings;
    }

    @Override
    public List<Binding> getBindings() throws SQLException{
        ResultSet getBindingsResult = getBindingsStatement.executeQuery();


        return getBindings(getBindingsResult);
    }
}
