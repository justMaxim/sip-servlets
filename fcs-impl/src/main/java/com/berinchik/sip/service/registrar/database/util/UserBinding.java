package com.berinchik.sip.service.registrar.database.util;

import javax.servlet.sip.Address;
import javax.servlet.sip.URI;

/**
 * Created by Maksim on 24.05.2017.
 */
public class UserBinding implements Binding {

    Address bindingAddress;
    URI primaryUserURI;

    UserBinding(Address bindingAddress, URI primaryUserURI) {
        this.bindingAddress = bindingAddress;
        this.primaryUserURI = primaryUserURI;
    }

    @Override
    public Address getBindingAddress() {
        return bindingAddress;
    }

    @Override
    public URI getPrimaryUserURI() {
        return primaryUserURI;
    }
}
