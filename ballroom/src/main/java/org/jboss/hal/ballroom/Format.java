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

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat;
import org.jboss.hal.resources.Constants;


/**
 * @author Harald Pehl
 */
public final class Format {

    private static final DateTimeFormat DATE_TIME_SHORT = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT);
    private static final DateTimeFormat DATE_TIME_MEDIUM = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM);
    private static final DateTimeFormat TIME_MEDIUM = DateTimeFormat.getFormat(PredefinedFormat.TIME_MEDIUM);
    private static final NumberFormat SIZE_FORMAT = NumberFormat.getFormat("#,##0.#");
    private static final Constants CONSTANTS = GWT.create(Constants.class);

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
        if (size <= 0) { return "0"; }
        final String[] units = new String[]{"Bytes", "KB", "MB", "GB", "TB"}; //NON-NLS
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return SIZE_FORMAT.format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static String humanReadableDuration(long duration) {
        if (duration < 1000) {
            return duration + " ms"; //NON-NLS
        }

        duration = duration / 1000;

        int sec = (int) duration % 60;
        duration /= 60;

        int min = (int) duration % 60;
        duration /= 60;

        int hour = (int) duration % 24;
        duration /= 24;

        int day = (int) duration;

        String str = "";
        if (day > 0) {
            if (day > 1) {
                str += day + " " + CONSTANTS.days() + ", ";
            } else {
                str += day + " " + CONSTANTS.day() + ", ";
            }
        }
        // prints 0 hour in case days exists. Otherwise prints 2 days, 34 min, sounds weird.
        if (hour > 0 || (day > 0)) {
            if (hour > 1) {
                str += hour + " " + CONSTANTS.hours() + ", ";
            } else {
                str += hour + " " + CONSTANTS.hour() + ", ";
            }
        }
        if (min > 0) {
            if (min > 1) {
                str += min + " " + CONSTANTS.minutes() + ", ";
            } else {
                str += min + " " + CONSTANTS.minute() + ", ";
            }
        }
        if (sec > 0) {
            if (sec > 1) {
                str += sec + " " + CONSTANTS.seconds();
            } else {
                str += sec + " " + CONSTANTS.second();
            }
        }
        return str;
    }

    private Format() {
    }
}
