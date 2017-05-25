package com.berinchik.sip.service.registrar.database.datasource;

import org.postgresql.ds.PGConnectionPoolDataSource;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * Created by Maksim on 25.05.2017.
 */
public class PostgreSqlPullingDataSourceProxy implements DataSource {

    PGConnectionPoolDataSource dataSource;

    public void setUrl(String url) {
        dataSource.setUrl(url);
    }
    public String getUrl() {
        return dataSource.getUrl();
    }

    public void setUsername(String user) {
        dataSource.setUser(user);
    }
    public String getUsername() {
        return dataSource.getUser();
    }

    public void setPassword(String password) {
        dataSource.setPassword(password);
    }
    public String getPassword() {
        return dataSource.getPassword();
    }

    public void setDriverClassName(String driverClassName) {
        return;
    }
    public String getDriverClassName() {
        return null;
    }

    @Override
    public Connection getConnection() throws SQLException {

        return dataSource.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return dataSource.getConnection(username, password);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return dataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        dataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        dataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return dataSource.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return dataSource.getParentLogger();
    }
}
