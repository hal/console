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
package org.jboss.hal.spi;

import java.util.Date;

import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;

import static com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat.DATE_TIME_LONG;
import static com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat.DATE_SHORT;
import static com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat.TIME_MEDIUM;
import static java.lang.System.currentTimeMillis;

public class Message {

    public enum Level {
        ERROR, WARNING, INFO, SUCCESS
    }


    // ------------------------------------------------------ error

    public static Message error(final SafeHtml message) {
        return error(currentTimeMillis(), message, null, false);
    }

    public static Message error(final long id, final SafeHtml message) {
        return error(id, message, null, false);
    }

    public static Message error(final SafeHtml message, final boolean sticky) {
        return error(currentTimeMillis(), message, null, sticky);
    }

    public static Message error(final long id, final SafeHtml message, final boolean sticky) {
        return error(id, message, null, sticky);
    }

    public static Message error(final SafeHtml message, final String details) {
        return error(currentTimeMillis(), message, details, false);
    }

    public static Message error(final long id, final SafeHtml message, final String details) {
        return error(id, message, details, false);
    }

    public static Message error(final SafeHtml message, final String details, final boolean sticky) {
        return new Message(currentTimeMillis(), Level.ERROR, message, details, null, null, sticky);
    }

    public static Message error(final long id, final SafeHtml message, final String details, final boolean sticky) {
        return new Message(id, Level.ERROR, message, details, null, null, sticky);
    }

    public static Message error(final SafeHtml message, final String actionTitle, final Callback callback) {
        return error(currentTimeMillis(), message, actionTitle, callback, false);
    }

    public static Message error(final long id, final SafeHtml message, final String actionTitle,
            final Callback callback) {
        return error(id, message, actionTitle, callback, false);
    }

    public static Message error(final SafeHtml message, final String actionTitle, final Callback callback,
            final boolean sticky) {
        return new Message(currentTimeMillis(), Level.ERROR, message, null, actionTitle, callback, sticky);
    }

    public static Message error(final long id, final SafeHtml message, final String actionTitle,
            final Callback callback, final boolean sticky) {
        return new Message(id, Level.ERROR, message, null, actionTitle, callback, sticky);
    }


    // ------------------------------------------------------ warning

    public static Message warning(final SafeHtml message) {
        return warning(currentTimeMillis(), message, null, false);
    }

    public static Message warning(final long id, final SafeHtml message) {
        return warning(id, message, null, false);
    }

    public static Message warning(final SafeHtml message, final boolean sticky) {
        return warning(currentTimeMillis(), message, null, sticky);
    }

    public static Message warning(final long id, final SafeHtml message, final boolean sticky) {
        return warning(id, message, null, sticky);
    }

    public static Message warning(final SafeHtml message, final String details) {
        return warning(currentTimeMillis(), message, details, false);
    }

    public static Message warning(final long id, final SafeHtml message, final String details) {
        return warning(id, message, details, false);
    }

    public static Message warning(final SafeHtml message, final String details, final boolean sticky) {
        return new Message(currentTimeMillis(), Level.WARNING, message, details, null, null, sticky);
    }

    public static Message warning(final long id, final SafeHtml message, final String details, final boolean sticky) {
        return new Message(id, Level.WARNING, message, details, null, null, sticky);
    }

    public static Message warning(final SafeHtml message, final String actionTitle, final Callback callback) {
        return warning(currentTimeMillis(), message, actionTitle, callback, false);
    }

    public static Message warning(final long id, final SafeHtml message, final String actionTitle, final Callback callback) {
        return warning(id, message, actionTitle, callback, false);
    }

    public static Message warning(final SafeHtml message, final String actionTitle, final Callback callback,
            boolean sticky) {
        return new Message(currentTimeMillis(), Level.WARNING, message, null, actionTitle, callback, sticky);
    }

    public static Message warning(final long id, final SafeHtml message, final String actionTitle, final Callback callback,
            boolean sticky) {
        return new Message(id, Level.WARNING, message, null, actionTitle, callback, sticky);
    }


    // ------------------------------------------------------ info

    public static Message info(final SafeHtml message) {
        return info(currentTimeMillis(), message, null, false);
    }

    public static Message info(final long id, final SafeHtml message) {
        return info(id, message, null, false);
    }

    public static Message info(final SafeHtml message, final boolean sticky) {
        return info(currentTimeMillis(), message, null, sticky);
    }

    public static Message info(final long id, final SafeHtml message, final boolean sticky) {
        return info(id, message, null, sticky);
    }

    public static Message info(final SafeHtml message, final String details) {
        return info(currentTimeMillis(), message, details, false);
    }

    public static Message info(final long id, final SafeHtml message, final String details) {
        return info(id, message, details, false);
    }

    public static Message info(final SafeHtml message, final String details, final boolean sticky) {
        return new Message(currentTimeMillis(), Level.INFO, message, details, null, null, sticky);
    }

    public static Message info(final long id, final SafeHtml message, final String details, final boolean sticky) {
        return new Message(id, Level.INFO, message, details, null, null, sticky);
    }

    public static Message info(final SafeHtml message, final String actionTitle, final Callback callback) {
        return info(currentTimeMillis(), message, actionTitle, callback, false);
    }

    public static Message info(final long id, final SafeHtml message, final String actionTitle, final Callback callback) {
        return info(id, message, actionTitle, callback, false);
    }

    public static Message info(final SafeHtml message, final String actionTitle, final Callback callback,
            final boolean sticky) {
        return new Message(currentTimeMillis(), Level.INFO, message, null, actionTitle, callback, sticky);
    }

    public static Message info(final long id, final SafeHtml message, final String actionTitle, final Callback callback,
            final boolean sticky) {
        return new Message(id, Level.INFO, message, null, actionTitle, callback, sticky);
    }


    // ------------------------------------------------------ success

    public static Message success(final SafeHtml message) {
        return success(message, null, false);
    }

    public static Message success(final long id, final SafeHtml message) {
        return success(id, message, null, false);
    }

    public static Message success(final SafeHtml message, final boolean sticky) {
        return success(currentTimeMillis(), message, null, sticky);
    }

    public static Message success(final long id, final SafeHtml message, final boolean sticky) {
        return success(id, message, null, sticky);
    }

    public static Message success(final SafeHtml message, final String details) {
        return success(currentTimeMillis(), message, details, false);
    }

    public static Message success(final long id, final SafeHtml message, final String details) {
        return success(id, message, details, false);
    }

    public static Message success(final SafeHtml message, final String details, final boolean sticky) {
        return new Message(currentTimeMillis(), Level.SUCCESS, message, details, null, null, sticky);
    }

    public static Message success(final long id, final SafeHtml message, final String details, final boolean sticky) {
        return new Message(id, Level.SUCCESS, message, details, null, null, sticky);
    }

    public static Message success(final SafeHtml message, final String actionTitle, final Callback callback) {
        return success(currentTimeMillis(), message, actionTitle, callback, false);
    }

    public static Message success(final long id, final SafeHtml message, final String actionTitle, final Callback callback) {
        return success(id, message, actionTitle, callback, false);
    }

    public static Message success(final SafeHtml message, final String actionTitle, final Callback callback,
            final boolean sticky) {
        return new Message(currentTimeMillis(), Level.SUCCESS, message, null, actionTitle, callback, sticky);
    }

    public static Message success(final long id, final SafeHtml message, final String actionTitle, final Callback callback,
            final boolean sticky) {
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

    private Message(final long id, final Level level, final SafeHtml message, final String details,
            final String actionTitle, final Callback callback, final boolean sticky) {
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
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof Message)) { return false; }

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
