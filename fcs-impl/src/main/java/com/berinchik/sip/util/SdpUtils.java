package com.berinchik.sip.util;

import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.media.server.io.sdp.*;
import org.mobicents.media.server.io.sdp.ice.attributes.CandidateAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Maksim on 26.05.2017.
 */
public class SdpUtils {

    private static Log logger = LogFactory.getLog(SdpUtils.class);

    /**
     * Modifies successResponse's message body to perform SDP negotiation
     *
     * @param initialRequest
     * @param successResponse
     * @return True if SDP is modified correctly, false if error occured
     */
    public static boolean performSdpNegotiation(SipServletRequest initialRequest,
                                                SipServletResponse successResponse)
            throws IOException, SdpException {

        String responseSdp = getSdpString(successResponse);
        String requestSdp = getSdpString(initialRequest);

        String[] strings = responseSdp.split("\n");
        StringBuilder negotiatedSdp = new StringBuilder();

        for (String sdpLine:
             strings) {
            logger.trace("sdpLine: " + sdpLine);
            if (sdpLine.indexOf("a=rtpmap:") != -1) {
                if (sdpLine.contains("8 PCMA/8000")) {
                    negotiatedSdp.append(sdpLine + "\n");
                }
            }
            else if (sdpLine.indexOf("m=audio") != -1) {
                negotiatedSdp.append("m=audio 5062 RTP/AVP 8\n");
            }
            else {
                negotiatedSdp.append(sdpLine + "\n");
            }
        }

        successResponse.setContent(negotiatedSdp, successResponse.getContentType());

        return true;
    }

    static String getSdpString(SipServletMessage message) throws IOException {

        String charset = null;
        String contentType = message.getContentType();

        if (contentType != null)
            charset = message.getCharacterEncoding();
        if (charset == null)
            charset = "UTF-8";

        return new String(
                message.getRawContent(),
                charset);
    }
}
