package com.berinchik.sip.util;

import com.berinchik.sip.config.condition.Condition;
import com.berinchik.sip.config.condition.ConditionId;
import com.berinchik.sip.error.FcsUnexpectedException;
import com.berinchik.sip.service.fsm.SipServiceContext;
import com.berinchik.sip.service.registrar.Registrar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;

import javax.servlet.sip.*;

/**
 * Created by Maksim on 26.05.2017.
 */
public class CommonUtils {

    private CommonUtils() { }

    private static Log logger = LogFactory.getLog(CommonUtils.class);

    public static final String SC_REGISTER_HELPER = "RegisterHelper";
    public static final String SC_FLEX_COMM_SERVICE_CONTEXT = "FcsServiceContext";
    public static final String SC_SIP_FACTORY = "SipFactory";
    public static final String SC_TIMER_SERVICE = "TimerService";

    public static final String SC_CONTACT_HEADER = "Contact";
    public static final String SC_TO_HEADER = "To";
    public static final String SC_EXPIRES_HEADER = "Expires";

    private static TimerService timerService;

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

    public static void setTimerService(TimerService tService) {
        timerService = tService;
    }

    public static TimerService getTimerService() {
        return timerService;
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

    public static boolean matchPeriodCondition(Condition condition) {
        if (condition.getConditionId() != ConditionId.VALID_PERIODS) {
            throw new FcsUnexpectedException("condition id is not valid-periods"
                    + condition.getConditionId());
        }

        JSONArray conditionPeriods
                = condition.getConditionValue().getJSONArray(Condition.SC_PERIODS_ARRAY);

        for(int i = 0; i < conditionPeriods.length(); ++i) {
            if (!periodConditionMatch(conditionPeriods.getJSONObject(i)))
                return false;
        }

        return true;
    }

    private static boolean periodConditionMatch(JSONObject periodCondition) {
        logger.debug("matching period condition");
        String periodConditionType = periodCondition.getString(Condition.SC_CONDITION_VP_PERIOD_TYPE);
        JSONArray periodValues = periodCondition.getJSONArray(Condition.SC_CONDITION_PERIOD_TYPE_VALUES);
        logger.trace("condition type: " + periodConditionType
                + "\nvalues: " + periodValues);

        switch(periodConditionType) {
            case Condition.SC_CONDITION_PERIOD_TYPE_VALID_DAYS:
                return matchDays(periodValues);
            case Condition.SC_CONDITION_PERIOD_TYPE_VALID_TIMES:
                return matchTimes(periodValues);
            default:
                logger.error("Wrong period type in condition: " + periodConditionType);
        }
        return false;
    }

    private static boolean matchTimes(JSONArray validTimes) {
        for(int i = 0; i < validTimes.length(); ++i) {
            if(matchTime(validTimes.getJSONObject(i))) {
                logger.trace("time match");
                return true;
            }
        }
        return false;
    }

    private static boolean matchTime(JSONObject time) {
        Calendar c = Calendar.getInstance();

        int hours = c.get(Calendar.HOUR_OF_DAY);
        int minutes = c.get(Calendar.MINUTE);

        String from = time.getString(Condition.SC_CONDITION_PERIOD_TIME_FROM);
        String to = time.getString(Condition.SC_CONDITION_PERIOD_TIME_TO);

        String[] fromS = from.split(":");
        String[] toS = to.split(":");

        int fromH = Integer.parseInt(fromS[0]);
        int fromM = Integer.parseInt(fromS[1]);

        int toH = Integer.parseInt(toS[0]);
        int toM = Integer.parseInt(toS[1]);

        logger.trace("matching times:\n"
                + "from: " + fromH + ":" + fromM
                + " to: " + toH + ":" + toM
                + "\nwith " + hours + ":" + minutes);

        int minutesOfDay = hours * 60 + minutes;

        return fromH * 60 + fromM <= minutesOfDay
                && toH * 60 + toM >= minutesOfDay;

    }

    private static boolean matchDays(JSONArray validDays) {
        Calendar calendar = Calendar.getInstance();
        String currentDay = getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK));

        logger.trace("Current day is: " + currentDay);

        for (int i = 0; i < validDays.length(); ++i) {
            logger.trace("Matching with " + validDays.getString(i));
            if (validDays.getString(i).equals(currentDay)){
                logger.debug("Match");
                return true;
            }
            logger.trace("Not match");

        }
        return false;
    }

    private static String getDayOfWeek(int value) {
        String day = "";
        switch (value) {
            case 1:
                day = "Sunday";
                break;
            case 2:
                day = "Monday";
                break;
            case 3:
                day = "Tuesday";
                break;
            case 4:
                day = "Wednesday";
                break;
            case 5:
                day = "Thursday";
                break;
            case 6:
                day = "Friday";
                break;
            case 7:
                day = "Saturday";
                break;
            default:
                throw new FcsUnexpectedException("day of week can't be " + value);
        }
        return day;
    }
}
