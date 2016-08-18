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

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;

import static com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat.DATE_TIME_LONG;

public class Message {

    public enum Level {
        ERROR, WARNING, INFO, SUCCESS
    }


    @FunctionalInterface
    public interface Action {

        void execute();
    }


    // ------------------------------------------------------ error

    public static Message error(final SafeHtml message) {
        return error(message, null, false);
    }

    public static Message error(final SafeHtml message, String details) {
        return error(message, details, false);
    }

    public static Message error(final SafeHtml message, final String details, boolean sticky) {
        return new Message(Level.ERROR, message, details, null, null, sticky);
    }

    public static Message error(final SafeHtml message, final String actionTitle, final Action action) {
        return error(message, actionTitle, action, false);
    }

    public static Message error(final SafeHtml message, final String actionTitle, final Action action, boolean sticky) {
        return new Message(Level.ERROR, message, null, actionTitle, action, sticky);
    }


    // ------------------------------------------------------ warning

    public static Message warning(final SafeHtml message) {
        return warning(message, null, false);
    }

    public static Message warning(final SafeHtml message, String details) {
        return warning(message, details, false);
    }

    public static Message warning(final SafeHtml message, final String details, boolean sticky) {
        return new Message(Level.WARNING, message, details, null, null, sticky);
    }

    public static Message warning(final SafeHtml message, final String actionTitle, final Action action) {
        return warning(message, actionTitle, action, false);
    }

    public static Message warning(final SafeHtml message, final String actionTitle, final Action action,
            boolean sticky) {
        return new Message(Level.WARNING, message, null, actionTitle, action, sticky);
    }


    // ------------------------------------------------------ info

    public static Message info(final SafeHtml message) {
        return info(message, null, false);
    }

    public static Message info(final SafeHtml message, final String details) {
        return info(message, details, false);
    }

    public static Message info(final SafeHtml message, final String details, boolean sticky) {
        return new Message(Level.INFO, message, details, null, null, sticky);
    }

    public static Message info(final SafeHtml message, final String actionTitle, final Action action) {
        return info(message, actionTitle, action, false);
    }

    public static Message info(final SafeHtml message, final String actionTitle, final Action action, boolean sticky) {
        return new Message(Level.INFO, message, null, actionTitle, action, sticky);
    }


    // ------------------------------------------------------ success

    public static Message success(final SafeHtml message) {
        return success(message, null, false);
    }

    public static Message success(final SafeHtml message, final String details) {
        return success(message, details, false);
    }

    public static Message success(final SafeHtml message, final String details, boolean sticky) {
        return new Message(Level.SUCCESS, message, details, null, null, sticky);
    }

    public static Message success(final SafeHtml message, final String actionTitle, final Action action) {
        return success(message, actionTitle, action, false);
    }

    public static Message success(final SafeHtml message, final String actionTitle, final Action action,
            boolean sticky) {
        return new Message(Level.SUCCESS, message, null, actionTitle, action, sticky);
    }


    // ------------------------------------------------------ message instance

    private final long id;
    private final String timestamp;
    private final Level level;
    private final SafeHtml message;
    private final String details;
    private final String actionTitle;
    private final Action action;
    private final boolean sticky;

    private Message(final Level level, final SafeHtml message, final String details,
            final String actionTitle, final Action action, final boolean sticky) {
        this.id = System.currentTimeMillis();
        this.timestamp = DateTimeFormat.getFormat(DATE_TIME_LONG).format(new Date());
        this.level = level;
        this.message = message;
        this.details = details;
        this.actionTitle = actionTitle;
        this.action = action;
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

    public SafeHtml getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }

    public boolean hasAction() {
        return actionTitle != null && action != null;
    }

    public String getActionTitle() {
        return actionTitle;
    }

    public Action getAction() {
        return action;
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
