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

import com.google.gwt.i18n.client.DateTimeFormat;

import java.util.Date;

import static com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat.DATE_TIME_LONG;

public class Message {

    public enum Level {
        ERROR, WARNING, INFO, SUCCESS
    }


    public static Message error(final String message) {
        return error(message, null, false);
    }

    public static Message error(final String message, String details) {
        return error(message, details, false);
    }

    public static Message error(final String message, final String details, boolean sticky) {
        return new Message(Level.ERROR, message, details, sticky);
    }

    public static Message warning(final String message) {
        return warning(message, null, false);
    }

    public static Message warning(final String message, String details) {
        return warning(message, details, false);
    }

    public static Message warning(final String message, final String details, boolean sticky) {
        return new Message(Level.WARNING, message, details, sticky);
    }

    public static Message info(final String message) {
        return info(message, null, false);
    }

    public static Message info(final String message, final String details) {
        return info(message, details, false);
    }

    public static Message info(final String message, final String details, boolean sticky) {
        return new Message(Level.INFO, message, details, sticky);
    }


    public static Message success(final String message) {
        return success(message, null, false);
    }

    public static Message success(final String message, final String details) {
        return success(message, details, false);
    }

    public static Message success(final String message, final String details, boolean sticky) {
        return new Message(Level.SUCCESS, message, details, sticky);
    }


    private final long id;
    private final String timestamp;
    private final Level level;
    private final String message;
    private final String details;
    private final boolean sticky;

    private Message(final Level level, final String message, final String details, final boolean sticky) {
        this.id = System.currentTimeMillis();
        this.timestamp = DateTimeFormat.getFormat(DATE_TIME_LONG).format(new Date());
        this.level = level;
        this.message = message;
        this.details = details;
        this.sticky = sticky;
    }

    public long getId() {
        return id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Level getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
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
