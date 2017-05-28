package com.berinchik.sip.service.registrar.database;

import com.berinchik.sip.service.registrar.database.util.UserBinding;
import com.berinchik.sip.util.CommonUtils;

import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//import org.postgresql.ds.PGConnectionPoolDataSource;

import org.apache.log4j.Logger;
import com.berinchik.sip.service.registrar.database.util.Binding;
import org.postgresql.ds.PGConnectionPoolDataSource;

/**
 * Created by Maksim on 24.05.2017.
 */
public class SimpleDatabaseAccessor implements DatabaseAccessor {
    //Database parameters and queries
    private static final String TB_TABLE_PRIMARY_USERS= "users";

    private static final String PU_COL_PRIMARY_USER = "user_name";
    private static final String PU_COL_ID = "id";
    private static final String PU_COL_SETTINGS = "settings";

    private static final String PU_GET_ALL_ABOUT_USER
            = String.format("SELECT * FROM %s WHERE %s = ?", TB_TABLE_PRIMARY_USERS, PU_COL_PRIMARY_USER);

    private static final String PU_GET_SERVICE_SETTINGS_BY_USER_NAME
            = String.format("SELECT * FROM %s WHERE %s = ?", TB_TABLE_PRIMARY_USERS, PU_COL_PRIMARY_USER);

    private static final String TB_TABLE_BINDINGS= "bindings";

    private static final String BI_COL_BINDING = "binding";
    private static final String BI_COL_USER_NAME = "user_name";
    private static final String BI_COL_EXPIRES = "expires";


    private static final String BI_GET_USER_NAME_BY_BINDING
            = String.format("SELECT %s FROM %s WHERE %s = ?", BI_COL_USER_NAME, TB_TABLE_BINDINGS, BI_COL_BINDING);

    private static final String BI_GET_BINDINGS_BY_USER_NAME
            = String.format("SELECT * FROM %s WHERE %s = ? AND %s > ?",
            TB_TABLE_BINDINGS,
            BI_COL_USER_NAME,
            BI_COL_EXPIRES);

    private static final String BI_DELETE_BINDING_BY_BINDING_NAME
            = String.format("DELETE FROM %s WHERE %s = ? AND %s = ?",
            TB_TABLE_BINDINGS, BI_COL_USER_NAME, BI_COL_BINDING);

    private static final String BI_ADD_BINDING_TO_USER_NAME
            = String.format("INSERT INTO %s(%s, %s, %s) VALUES(?, ?, ?)",
            TB_TABLE_BINDINGS, BI_COL_USER_NAME, BI_COL_BINDING, BI_COL_EXPIRES);

    private static final String BI_DELETE_ALL_BINDINGS_TO_USER
            = String.format("DELETE FROM %s WHERE %s = ?",
            TB_TABLE_BINDINGS, BI_COL_USER_NAME);
    //-------------------------

    //Fields
    private static Logger logger = Logger.getLogger(DatabaseAccessor.class);
    private PGConnectionPoolDataSource dataSource;

    public void setDataSource(PGConnectionPoolDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public SimpleDatabaseAccessor() throws SQLException {
        dataSource = new PGConnectionPoolDataSource();
        dataSource.setServerName("localhost");
        dataSource.setDatabaseName("register");
        dataSource.setUser("db_user");
        dataSource.setPassword("1111");

    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    private PreparedStatement getNewPreparedStatement(String query) throws SQLException{

        Connection connection = null;

        try {
            connection = dataSource.getConnection();
            return connection.prepareStatement(query);
        }
        catch(SQLException ex) {
            closeSqlResource(connection);
            throw ex;
        }
    }

    private void closeSqlResource(AutoCloseable resource) {
        if(resource != null) {
            try {
                resource.close();
            }
            catch (Exception closeEx) {
                logger.error("Exception during sql resource closing", closeEx);
            }
        }
    }

    @Override
    public JSONObject getServiceConfigJsonObject(String primaryUserURI) throws SQLException {

        JSONObject serviceSettings = null;

        PreparedStatement getServiceConfigStatement = null;
        ResultSet userInfoResultSet = null;

        try {
            if (isUserRegistered(primaryUserURI) != null) {
                getServiceConfigStatement = getNewPreparedStatement(PU_GET_ALL_ABOUT_USER);
                getServiceConfigStatement.setString(1, primaryUserURI);
                userInfoResultSet = getServiceConfigStatement.executeQuery();
                if (userInfoResultSet.next()) {
                    String stringJsonSettings = userInfoResultSet.getString(PU_COL_SETTINGS);
                    if(stringJsonSettings != null){
                        serviceSettings = new JSONObject(stringJsonSettings);
                    }
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
    public String isUserRegistered(String UserURI) throws SQLException {
        PreparedStatement statement = null;
        ResultSet userInfoResultSet = null;
        String primaryUserURI = null;

        try {
            statement = getNewPreparedStatement(BI_GET_BINDINGS_BY_USER_NAME);
            statement.setString(1, UserURI);
            statement.setInt(2,(int)CommonUtils.getCurrentTimestampInSeconds());
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
    public boolean userIsPrimary(String primaryUserURI) throws SQLException {
        PreparedStatement statement = null;
        ResultSet userInfoResultSet = null;

        boolean isPrimary = false;

        try {
            statement = getNewPreparedStatement(PU_GET_ALL_ABOUT_USER);
            statement.setString(1, primaryUserURI);
            logger.info(statement.toString());
            userInfoResultSet = statement.executeQuery();

            if (userInfoResultSet.next()) {
                logger.info("User is primary:\n"
                        + userInfoResultSet.getString(PU_COL_PRIMARY_USER)
                        +"\n" + userInfoResultSet.getString(PU_COL_SETTINGS));
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
    public boolean deleteBinding(String primaryUserURI, String binding) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = getNewPreparedStatement(BI_DELETE_BINDING_BY_BINDING_NAME);
            statement.setString(1, primaryUserURI);
            statement.setString(2, binding);

            logger.info(statement.toString());

            statement.executeUpdate();

            logger.info("Removed binding of: " + binding
                    + " to " + primaryUserURI);
        }
        finally {
            closeSqlResource(statement);
        }

        return true;
    }

    @Override
    public boolean addBinding(String primaryUserURI, String binding, long expires) throws SQLException {
        PreparedStatement statement = null;
        logger.info("Adding binding " + binding
                + "\n");
        try {
            statement = getNewPreparedStatement(BI_ADD_BINDING_TO_USER_NAME);
            statement.setString(1, primaryUserURI);
            statement.setString(2, binding);
            statement.setLong(3, expires);

            statement.executeUpdate();
        }
        finally {
            closeSqlResource(statement);
        }

        return false;
    }

    @Override
    public boolean deleteAllBindings(String primaryUserURI) {
        return false;
    }

    @Override
    public List<Binding> getUserBindings(String primaryUserURI) throws SQLException {
        PreparedStatement statement = null;
        ResultSet userInfoResultSet = null;

        List<Binding> bindings = new ArrayList<>();

        try {
            logger.info("Trying to get bindings of the user " + primaryUserURI);
            statement = getNewPreparedStatement(BI_GET_BINDINGS_BY_USER_NAME);
            statement.setString(1, primaryUserURI);
            statement.setInt(2, (int)CommonUtils.getCurrentTimestampInSeconds());
            logger.info(statement.toString());
            userInfoResultSet = statement.executeQuery();

            while (userInfoResultSet.next()) {
                String contact;
                long expires = 0;

                contact = userInfoResultSet.getString(BI_COL_BINDING);
                expires = userInfoResultSet.getLong(BI_COL_EXPIRES);

                logger.info("\n\tgot binding: " + contact);

                bindings.add(new UserBinding(contact, primaryUserURI, expires));
            }

        }
        finally {
            closeSqlResource(statement);
            closeSqlResource(userInfoResultSet);
        }

        return bindings;
    }

}
