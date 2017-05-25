package com.berinchik.sip.service.registrar.database.util;

import javax.servlet.sip.Address;
import javax.servlet.sip.URI;

/**
 * Created by Maksim on 24.05.2017.
 */
public interface Binding {
    String getPrimaryUserURI();
    void setPrimaryUserURI(String bindingURI);

    String getBindingURI();
    void setBindingURI(String bindingURI);

    /**
     * Returns expires time (not duration of registration).
     * To get duration, needs to
     *
     * @return
     */
    long getExpires();
    void setExpires(long expires);

    long getDuration();
}
