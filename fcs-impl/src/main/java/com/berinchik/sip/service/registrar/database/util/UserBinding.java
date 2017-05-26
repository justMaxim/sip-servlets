package com.berinchik.sip.service.registrar.database.util;

import com.berinchik.sip.util.CommonUtils;

import java.util.Date;

/**
 * Created by Maksim on 24.05.2017.
 */
public class UserBinding implements Binding {

    String bindingURI;
    String primaryUserURI;
    long expires;

    public UserBinding(String bindingAddress, String primaryUserURI, long expires) {
        this.bindingURI = bindingAddress;
        this.primaryUserURI = primaryUserURI;
        this.expires = expires;
    }

    @Override
    public String toString() {
        return "Binding URI: " + bindingURI
                + "\n Primary user: " + primaryUserURI
                + "\n Expures: " + getDuration();
    }

    @Override
    public String getPrimaryUserURI() {
        return this.primaryUserURI;
    }

    @Override
    public void setPrimaryUserURI(String primaryUserURI) {
        this.primaryUserURI = primaryUserURI;
    }

    @Override
    public String getBindingURI() {
        return this.bindingURI;
    }

    @Override
    public void setBindingURI(String bindingURI) {
        this.bindingURI = bindingURI;
    }

    @Override
    public long getExpires() {
        return expires;
    }

    @Override
    public void setExpires(long expires) {
        this.expires = expires;
    }

    @Override
    public long getDuration() {
        return expires - CommonUtils.getCurrentTimestampInSeconds();
    }
}
