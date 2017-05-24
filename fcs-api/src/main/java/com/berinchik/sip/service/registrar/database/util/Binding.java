package com.berinchik.sip.service.registrar.database.util;

import javax.servlet.sip.Address;
import javax.servlet.sip.URI;

/**
 * Created by Maksim on 24.05.2017.
 */
public interface Binding {
    Address getBindingAddress();
    URI getPrimaryUserURI();

}
