package com.berinchik.sip.util;

import com.berinchik.sip.service.fsm.SipServiceContext;
import com.berinchik.sip.service.registrar.Registrar;

import javax.servlet.sip.Address;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletMessage;

/**
 * Created by Maksim on 26.05.2017.
 */
public class CommonUtils {
    private CommonUtils() { }

    public static final String SC_REGISTER_HELPER = "RegisterHelper";
    public static final String SC_FLEX_COMM_SERVICE_CONTEXT = "FcsServiceContext";
    public static final String SC_SIP_FACTORY = "SipFactory";

    public static final String SC_CONTACT_HEADER = "Contact";
    public static final String SC_EXPIRES_HEADER = "Expires";

    public static long getCurrentTimestampInSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    public static Registrar getRegistrarHelper(SipServletMessage message) {
        return getRegistrarHelper(message.getApplicationSession());
    }

    public static Registrar getRegistrarHelper(SipApplicationSession appSession) {
        return (Registrar) appSession.getAttribute(SC_REGISTER_HELPER);
    }

    public static SipServiceContext getSipServiceContext(SipServletMessage message) {
        return getSipServiceContext(message.getApplicationSession());
    }

    public static SipServiceContext getSipServiceContext(SipApplicationSession appSession) {
        return (SipServiceContext) appSession.getAttribute(SC_FLEX_COMM_SERVICE_CONTEXT);
    }

    public static SipFactory getSipFactory(SipServletMessage message) {
        return getSipFactory(message.getApplicationSession());
    }

    public static SipFactory getSipFactory(SipApplicationSession appSession) {
        return (SipFactory) appSession.getAttribute(SC_SIP_FACTORY);
    }

    public static int getExpires(Address contact, SipServletMessage message) {
        int expires = contact.getExpires();
        if (expires < 0) {
            expires = message.getExpires();
        }

        return expires;
    }

    public static SipApplicationSession getAppSession(SipServletMessage message1, SipServletMessage message2) {

        SipApplicationSession appSession = null;

        if (message1 != null)
            appSession = message1.getApplicationSession();
        else if (message2 != null)
            appSession = message2.getApplicationSession();

        return appSession;
    }

    public static SipApplicationSession getAppSession(SipServletMessage message) {

        SipApplicationSession appSession = null;

        if (message != null)
            appSession = message.getApplicationSession();

        return appSession;
    }
}
