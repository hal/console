/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.ballroom;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat;
import org.jboss.hal.resources.Constants;

public final class Format {

    private static final DateTimeFormat DATE_TIME_SHORT = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT);
    private static final DateTimeFormat DATE_TIME_MEDIUM = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM);
    private static final DateTimeFormat TIME_MEDIUM = DateTimeFormat.getFormat(PredefinedFormat.TIME_MEDIUM);
    private static final DateTimeFormat TIMESTAMP = DateTimeFormat.getFormat("HH:mm:ss.SSS");
    private static final NumberFormat SIZE_FORMAT = NumberFormat.getFormat("#,##0.#");
    private static final Constants CONSTANTS = GWT.create(Constants.class);
    private static final String SPACE = " ";


    public static String timestamp(Date date) {
        return TIMESTAMP.format(date);
    }

    public static String time(Date date) {
        return TIME_MEDIUM.format(date);
    }

    public static String shortDateTime(Date date) {
        return DATE_TIME_SHORT.format(date);
    }

    public static String mediumDateTime(Date date) {
        return DATE_TIME_MEDIUM.format(date);
    }

    public static String humanReadableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        String[] units = new String[]{"Bytes", "KB", "MB", "GB", "TB"}; //NON-NLS
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return SIZE_FORMAT.format(size / Math.pow(1024, digitGroups)) + SPACE + units[digitGroups];
    }

    /**
     * Formats the elapsed time (in milliseconds) to a human readable format: example 1 minute, 16 seconds.
     *
     * @param duration in milliseconds
     *
     * @return The string representation of the human readable format.
     */
    public static String humanReadableDuration(long duration) {
        if (duration < 1000) {
            return duration + " ms"; //NON-NLS
        }

        duration = Math.round(duration / 1000.0);

        int sec = (int) duration % 60;
        duration = Math.round(duration / 60.0);

        int min = (int) duration % 60;
        duration = Math.round(duration / 60.0);

        int hour = (int) duration % 24;
        duration = Math.round(duration / 24.0);

        int day = (int) duration;

        String str = "";
        if (day > 0) {
            if (day > 1) {
                str += day + SPACE + CONSTANTS.days() + ", ";
            } else {
                str += day + SPACE + CONSTANTS.day() + ", ";
            }
        }
        // prints 0 hour in case days exists. Otherwise prints 2 days, 34 min, sounds weird.
        if (hour > 0 || (day > 0)) {
            if (hour > 1) {
                str += hour + SPACE + CONSTANTS.hours() + ", ";
            } else {
                str += hour + SPACE + CONSTANTS.hour() + ", ";
            }
        }
        if (min > 0) {
            if (min > 1) {
                str += min + SPACE + CONSTANTS.minutes() + ", ";
            } else {
                str += min + SPACE + CONSTANTS.minute() + ", ";
            }
        }
        if (sec > 0) {
            if (sec > 1) {
                str += sec + SPACE + CONSTANTS.seconds();
            } else {
                str += sec + SPACE + CONSTANTS.second();
            }
        }
        return str;
    }

    /**
     * Formats the elapsed time (in nanoseconds) to a human readable format: example 1 minute, 16 seconds.
     *
     * @param duration in nanoseconds
     *
     * @return The string representation of the human readable format.
     */
    public static String humanReadableDurationNanoseconds(long duration) {
        long l = TimeUnit.NANOSECONDS.toMillis(duration);
        if (l > 0) {
            return humanReadableDuration(l);
        } else {
            return duration + " ns ";
        }
    }


    private Format() {
    }
}
