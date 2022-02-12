/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.spi;

import java.util.Date;

import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;

import static com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat.DATE_SHORT;
import static com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat.DATE_TIME_LONG;
import static com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat.TIME_MEDIUM;
import static java.lang.System.currentTimeMillis;

public class Message {

    public enum Level {
        ERROR, WARNING, INFO, SUCCESS
    }

    // ------------------------------------------------------ error

    public static Message error(SafeHtml message) {
        return error(currentTimeMillis(), message, null, false);
    }

    public static Message error(long id, SafeHtml message) {
        return error(id, message, null, false);
    }

    public static Message error(SafeHtml message, boolean sticky) {
        return error(currentTimeMillis(), message, null, sticky);
    }

    public static Message error(long id, SafeHtml message, boolean sticky) {
        return error(id, message, null, sticky);
    }

    public static Message error(SafeHtml message, String details) {
        return error(currentTimeMillis(), message, details, false);
    }

    public static Message error(long id, SafeHtml message, String details) {
        return error(id, message, details, false);
    }

    public static Message error(SafeHtml message, String details, boolean sticky) {
        return new Message(currentTimeMillis(), Level.ERROR, message, details, null, null, sticky);
    }

    public static Message error(long id, SafeHtml message, String details, boolean sticky) {
        return new Message(id, Level.ERROR, message, details, null, null, sticky);
    }

    public static Message error(SafeHtml message, String actionTitle, Callback callback) {
        return error(currentTimeMillis(), message, actionTitle, callback, false);
    }

    public static Message error(long id, SafeHtml message, String actionTitle, Callback callback) {
        return error(id, message, actionTitle, callback, false);
    }

    public static Message error(SafeHtml message, String actionTitle, Callback callback, boolean sticky) {
        return new Message(currentTimeMillis(), Level.ERROR, message, null, actionTitle, callback, sticky);
    }

    public static Message error(long id, SafeHtml message, String actionTitle,
            Callback callback, boolean sticky) {
        return new Message(id, Level.ERROR, message, null, actionTitle, callback, sticky);
    }

    // ------------------------------------------------------ warning

    public static Message warning(SafeHtml message) {
        return warning(currentTimeMillis(), message, null, false);
    }

    public static Message warning(long id, SafeHtml message) {
        return warning(id, message, null, false);
    }

    public static Message warning(SafeHtml message, boolean sticky) {
        return warning(currentTimeMillis(), message, null, sticky);
    }

    public static Message warning(long id, SafeHtml message, boolean sticky) {
        return warning(id, message, null, sticky);
    }

    public static Message warning(SafeHtml message, String details) {
        return warning(currentTimeMillis(), message, details, false);
    }

    public static Message warning(long id, SafeHtml message, String details) {
        return warning(id, message, details, false);
    }

    public static Message warning(SafeHtml message, String details, boolean sticky) {
        return new Message(currentTimeMillis(), Level.WARNING, message, details, null, null, sticky);
    }

    public static Message warning(long id, SafeHtml message, String details, boolean sticky) {
        return new Message(id, Level.WARNING, message, details, null, null, sticky);
    }

    public static Message warning(SafeHtml message, String actionTitle, Callback callback) {
        return warning(currentTimeMillis(), message, actionTitle, callback, false);
    }

    public static Message warning(long id, SafeHtml message, String actionTitle, Callback callback) {
        return warning(id, message, actionTitle, callback, false);
    }

    public static Message warning(SafeHtml message, String actionTitle, Callback callback, boolean sticky) {
        return new Message(currentTimeMillis(), Level.WARNING, message, null, actionTitle, callback, sticky);
    }

    public static Message warning(long id, SafeHtml message, String actionTitle, Callback callback, boolean sticky) {
        return new Message(id, Level.WARNING, message, null, actionTitle, callback, sticky);
    }

    // ------------------------------------------------------ info

    public static Message info(SafeHtml message) {
        return info(currentTimeMillis(), message, null, false);
    }

    public static Message info(long id, SafeHtml message) {
        return info(id, message, null, false);
    }

    public static Message info(SafeHtml message, boolean sticky) {
        return info(currentTimeMillis(), message, null, sticky);
    }

    public static Message info(long id, SafeHtml message, boolean sticky) {
        return info(id, message, null, sticky);
    }

    public static Message info(SafeHtml message, String details) {
        return info(currentTimeMillis(), message, details, false);
    }

    public static Message info(long id, SafeHtml message, String details) {
        return info(id, message, details, false);
    }

    public static Message info(SafeHtml message, String details, boolean sticky) {
        return new Message(currentTimeMillis(), Level.INFO, message, details, null, null, sticky);
    }

    public static Message info(long id, SafeHtml message, String details, boolean sticky) {
        return new Message(id, Level.INFO, message, details, null, null, sticky);
    }

    public static Message info(SafeHtml message, String actionTitle, Callback callback) {
        return info(currentTimeMillis(), message, actionTitle, callback, false);
    }

    public static Message info(long id, SafeHtml message, String actionTitle, Callback callback) {
        return info(id, message, actionTitle, callback, false);
    }

    public static Message info(SafeHtml message, String actionTitle, Callback callback, boolean sticky) {
        return new Message(currentTimeMillis(), Level.INFO, message, null, actionTitle, callback, sticky);
    }

    public static Message info(long id, SafeHtml message, String actionTitle, Callback callback,
            boolean sticky) {
        return new Message(id, Level.INFO, message, null, actionTitle, callback, sticky);
    }

    // ------------------------------------------------------ success

    public static Message success(SafeHtml message) {
        return success(message, null, false);
    }

    public static Message success(long id, SafeHtml message) {
        return success(id, message, null, false);
    }

    public static Message success(SafeHtml message, boolean sticky) {
        return success(currentTimeMillis(), message, null, sticky);
    }

    public static Message success(long id, SafeHtml message, boolean sticky) {
        return success(id, message, null, sticky);
    }

    public static Message success(SafeHtml message, String details) {
        return success(currentTimeMillis(), message, details, false);
    }

    public static Message success(long id, SafeHtml message, String details) {
        return success(id, message, details, false);
    }

    public static Message success(SafeHtml message, String details, boolean sticky) {
        return new Message(currentTimeMillis(), Level.SUCCESS, message, details, null, null, sticky);
    }

    public static Message success(long id, SafeHtml message, String details, boolean sticky) {
        return new Message(id, Level.SUCCESS, message, details, null, null, sticky);
    }

    public static Message success(SafeHtml message, String actionTitle, Callback callback) {
        return success(currentTimeMillis(), message, actionTitle, callback, false);
    }

    public static Message success(long id, SafeHtml message, String actionTitle, Callback callback) {
        return success(id, message, actionTitle, callback, false);
    }

    public static Message success(SafeHtml message, String actionTitle, Callback callback, boolean sticky) {
        return new Message(currentTimeMillis(), Level.SUCCESS, message, null, actionTitle, callback, sticky);
    }

    public static Message success(long id, SafeHtml message, String actionTitle,
            Callback callback,
            boolean sticky) {
        return new Message(id, Level.SUCCESS, message, null, actionTitle, callback, sticky);
    }

    // ------------------------------------------------------ message instance

    private final long id;
    private final String date;
    private final String time;
    private final String timestamp;
    private final Level level;
    private final SafeHtml message;
    private final String details;
    private final String actionTitle;
    private final Callback callback;
    private final boolean sticky;

    private Message(long id, Level level, SafeHtml message, String details, String actionTitle, Callback callback,
            boolean sticky) {
        Date now = new Date();

        this.id = id;
        this.date = DateTimeFormat.getFormat(DATE_SHORT).format(now);
        this.time = DateTimeFormat.getFormat(TIME_MEDIUM).format(now);
        this.timestamp = DateTimeFormat.getFormat(DATE_TIME_LONG).format(now);
        this.level = level;
        this.message = message;
        this.details = details;
        this.actionTitle = actionTitle;
        this.callback = callback;
        this.sticky = sticky;
    }

    public long getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Level getLevel() {
        return level;
    }

    public SafeHtml getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }

    public boolean hasAction() {
        return actionTitle != null && callback != null;
    }

    public String getActionTitle() {
        return actionTitle;
    }

    public Callback getCallback() {
        return callback;
    }

    public boolean isSticky() {
        return sticky;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Message)) {
            return false;
        }

        Message message = (Message) o;

        return id == message.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "Message(" + level + ": " + message + (sticky ? ", sticky)" : ")");
    }
}
