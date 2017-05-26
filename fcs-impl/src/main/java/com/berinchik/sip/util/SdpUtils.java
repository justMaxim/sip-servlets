package com.berinchik.sip.util;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

/**
 * Created by Maksim on 26.05.2017.
 */
public class SdpUtils {

    /**
     * Modifies successResponse's message body to perform SDP negotiation
     *
     * @param initialRequest
     * @param successResponse
     * @return True if SDP is modified correctly, false if error occured
     */
    public static boolean porformSdpNegotiation(SipServletRequest initialRequest, SipServletResponse successResponse) {
        if (!"application/sdp".equals(successResponse.getContentType())) {
            return false;
        }
        else if (successResponse.getContentLength() == 0) {
            return false;
        }
        return true;
    }
}
