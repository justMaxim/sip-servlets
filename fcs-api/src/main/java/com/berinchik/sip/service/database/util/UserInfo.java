package com.berinchik.sip.service.database.util;

import org.json.JSONObject;

import javax.servlet.sip.URI;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Maksim on 24.05.2017.
 */
public interface UserInfo {
    URI getURI();
    JSONObject getSettings();
    List<Binding> getBindings() throws SQLException;
}
