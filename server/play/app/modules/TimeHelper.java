/**
 * Copyright 2018, FleetTLC. All rights reserved
 */
package modules;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Date;
import java.util.Calendar;

import play.Logger;

public class TimeHelper {

    public static final String DATE_FORMAT = "MM/dd/yyyy";
    public static final String TIME_FORMAT = "kk:mm z";
    public static final String DATE_TIME_FORMAT = "MM/dd/yyyy kk:mm z";

    public TimeHelper() {
    }

    public String getDateTime(Date entry_time, String time_zone) {
        if (entry_time == null) {
            return "";
        }
        return getFormat(DATE_TIME_FORMAT, time_zone).format(entry_time);
    }

    public String getDate(Date entry_time, String time_zone) {
        if (entry_time == null) {
            return "";
        }
        return getFormat(DATE_FORMAT, time_zone).format(entry_time);
    }

    public String getTime(Date entry_time, String time_zone) {
        if (entry_time == null) {
            return "";
        }
        return getFormat(TIME_FORMAT, time_zone).format(entry_time);
    }

    private SimpleDateFormat getFormat(String the_format, String time_zone) {
        SimpleDateFormat format = new SimpleDateFormat(the_format);
        return format;
    }

    private TimeZone getTimeZone(String time_zone) {
        if (time_zone != null) {
            if (time_zone.startsWith("-") || time_zone.startsWith("+")) {
                return TimeZone.getTimeZone("GMT" + time_zone);
            } else if (time_zone.equals("CDT") || time_zone.equals("Central Daylight Time")) {
                return TimeZone.getTimeZone("GMT-5:00");
            } else if (time_zone.equals("EDT")) {
                return TimeZone.getTimeZone("GMT-4:00");
            } else {
                return TimeZone.getTimeZone(time_zone);
            }
        }
        return null;
    }


}
