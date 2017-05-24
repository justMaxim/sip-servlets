package com.berinchik.sip.service.registrar.database;

import org.json.JSONObject;

import javax.servlet.sip.Address;
import javax.servlet.sip.URI;

import java.sql.*;
import java.util.List;

import org.postgresql.ds.PGConnectionPoolDataSource;

import org.apache.log4j.Logger;

/**
 * Created by Maksim on 24.05.2017.
 */
public class SimpleDatabaseAccessor implements DatabaseAccessor {

    private static Logger logger = Logger.getLogger(DatabaseAccessor.class);

    private PGConnectionPoolDataSource dataSource;

    private static final String TB_TABLE_PRIMARY_USERS= "primary_users";

    private static final String PU_COL_PRIMARY_USER = "pk_user_name";
    private static final String PU_COL_ID = "id";
    private static final String PU_COL_SETTINGS = "settings";

    private static final String PU_GET_ALL_ABOUT_USER
            = String.format("SELECT * FROM %s WHERE %s = ?", TB_TABLE_PRIMARY_USERS, PU_COL_PRIMARY_USER);

    private static final String PU_GET_SERVICE_SETTINGS_BY_USER_NAME
            = String.format("SELECT * FROM %s WHERE %s = ?", TB_TABLE_PRIMARY_USERS, PU_COL_PRIMARY_USER);

    private static final String TB_TABLE_BINDINGS= "bindings";

    private static final String BI_COL_BINDING = "binding";
    private static final String BI_COL_USER_NAME = "fk_user_name";
    private static final String BI_COL_EXPIRES = "expires";


    private static final String BI_GET_USER_NAME_BY_BINDING
            = String.format("SELECT %s FROM %s WHERE %s = ?", BI_COL_USER_NAME, TB_TABLE_BINDINGS, BI_COL_BINDING);

    private static final String BI_GET_BINDINGS_BY_USER_NAME
            = String.format("SELECT %s FROM %s WHERE %s = ? AND %s < ?",
            BI_COL_BINDING,
            TB_TABLE_BINDINGS,
            PU_COL_PRIMARY_USER,
            BI_COL_EXPIRES);

    private static final String BI_DELETE_BINDING_BY_BINDING_NAME
            = String.format("DELETE %s WHERE %s = ? AND %s = ?", TB_TABLE_BINDINGS, BI_COL_USER_NAME, BI_COL_BINDING);

    private static final String BI_ADD_BINDING_TO_USER_NAME
            = String.format("INSERT %s WHERE %s = ? AND %s = ?", TB_TABLE_BINDINGS, BI_COL_USER_NAME, BI_COL_BINDING);



    public SimpleDatabaseAccessor() throws SQLException {
        dataSource = new PGConnectionPoolDataSource();
        dataSource.setServerName("localhost");
        dataSource.setDatabaseName("test_database");
        dataSource.setUser("test_user");
        dataSource.setPassword("qwerty");

    }

    private PreparedStatement getNewPreparedStatement(String query) throws SQLException{
        Connection connection = dataSource.getConnection();
        return connection.prepareStatement(query);
    }

    private void closeSqlResource(AutoCloseable resourse) {
        if(resourse != null) {
            try {
                resourse.close();
            }
            catch (Exception closeEx) {
                logger.error("Exception during sql resource closing", closeEx);
            }
        }
    }

    @Override
    public JSONObject getServiceConfigJsonObject(URI primaryUserURI) throws SQLException {

        String userName = primaryUserURI.toString();
        JSONObject serviceSettings = null;

        PreparedStatement getServiceConfigStatement = null;
        ResultSet userInfoResultSet = null;

        try {
            if (isUserRegistered(primaryUserURI) != null) {
                getServiceConfigStatement = getNewPreparedStatement(PU_GET_ALL_ABOUT_USER);
                getServiceConfigStatement.setString(1, userName);
                userInfoResultSet = getServiceConfigStatement.executeQuery();
                if (userInfoResultSet.next()) {
                    serviceSettings = new JSONObject(userInfoResultSet.getString(PU_COL_PRIMARY_USER));
                }
            }
        }
        finally {
            closeSqlResource(getServiceConfigStatement);
            closeSqlResource(userInfoResultSet);
        }

        return serviceSettings;
    }

    @Override
    public String isUserRegistered(URI UserURI) throws SQLException {
        PreparedStatement statement = null;
        ResultSet userInfoResultSet = null;
        String primaryUserURI = null;

        try {
            statement = getNewPreparedStatement(BI_GET_BINDINGS_BY_USER_NAME);
            statement.setString(1, UserURI.toString());
            userInfoResultSet = statement.executeQuery();

            if (userInfoResultSet.next()) {
                primaryUserURI = userInfoResultSet.getString(PU_COL_PRIMARY_USER);
            }

        }
        finally {
            closeSqlResource(statement);
            closeSqlResource(userInfoResultSet);
        }

        return primaryUserURI;
    }

    @Override
    public boolean userIsPrimary(URI primaryUserURI) throws SQLException {
        PreparedStatement statement = null;
        ResultSet userInfoResultSet = null;

        boolean isPrimary = false;

        try {
            statement = getNewPreparedStatement(PU_GET_ALL_ABOUT_USER);
            statement.setString(1, primaryUserURI.toString());
            userInfoResultSet = statement.executeQuery();

            if (userInfoResultSet.next()) {
                isPrimary = true;
            }

        }
        finally {
            closeSqlResource(statement);
            closeSqlResource(userInfoResultSet);
        }

        return isPrimary;

    }

    @Override
    public boolean deleteBinding(URI primaryUserURI, Address binding) throws SQLException {
        PreparedStatement statement = null;
        ResultSet userInfoResultSet = null;

        try {
            statement = getNewPreparedStatement(BI_DELETE_BINDING_BY_BINDING_NAME);
            statement.setString(1, primaryUserURI.toString());
            statement.setString(2, binding.toString());

            statement.executeUpdate();
        }
        finally {
            closeSqlResource(statement);
            closeSqlResource(userInfoResultSet);
        }

        return true;
    }

    @Override
    public boolean addBinding(URI primaryUserURI, Address binding, long expires) {

        return false;
    }

    @Override
    public List<Address> getUserBindings(URI primaryUserURI) {
        return null;
    }

}
