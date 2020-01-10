/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.config;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import org.jboss.hal.resources.Ids;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

public class Settings {

    @Inject public static Settings INSTANCE; // use only if no DI is available!
    public static final String DEFAULT_LOCALE = "en";
    public static final int DEFAULT_PAGE_SIZE = 10;
    // keep in sync with the poll-time attribute of settings.dmr
    public static final int DEFAULT_POLL_TIME = 10;
    public static final int[] PAGE_SIZE_VALUES = new int[]{10, 20, 50};
    private static final int EXPIRES = 365; // days

    private final Map<Key, Value> values;

    public Settings() {
        values = new EnumMap<>(Key.class);
    }

    public <T> void load(Key key, T defaultValue) {
        String value = Cookies.get(cookieName(key));
        if (value == null) {
            if (defaultValue != null) {
                value = String.valueOf(defaultValue);
            }
        }
        values.put(key, new Value(value));
    }

    public Value get(Key key) {
        return values.getOrDefault(key, Value.EMPTY);
    }

    public <T> void set(Key key, T value) {
        values.put(key, new Value(value != null ? String.valueOf(value) : null));
        if (value == null) {
            Cookies.remove(cookieName(key));
        } else {
            if (key.persistent) {
                Cookies.set(cookieName(key), String.valueOf(value), EXPIRES);
            } else {
                Cookies.set(cookieName(key), String.valueOf(value));
            }
        }
    }

    @Override
    public String toString() {
        return Joiner.on(", ").withKeyValueSeparator('=').join(values);
    }

    private String cookieName(Key key) {
        return Ids.build(Ids.COOKIE, key.key);
    }


    @SuppressWarnings("DuplicateStringLiteralInspection")
    public enum Key {
        TITLE("title", true),
        COLLECT_USER_DATA("collect-user-data", true),
        LOCALE("locale", true),
        PAGE_SIZE("page-size", true),
        POLL("poll", true),
        POLL_TIME("poll-time", true),
        RUN_AS("run-as", false); // can contain multiple roles separated by ","

        public static Key from(String key) {
            switch (key) {
                case "title":
                    return TITLE;
                case "collect-user-data":
                    return COLLECT_USER_DATA;
                case "locale":
                    return LOCALE;
                case "page-size":
                    return PAGE_SIZE;
                case "poll":
                    return POLL;
                case "poll-time":
                    return POLL_TIME;
                case "run-as":
                    return RUN_AS;
                default:
                    return null;
            }
        }

        private final String key;
        private final boolean persistent;

        Key(String key, boolean persistent) {
            this.key = key;
            this.persistent = persistent;
        }

        public String key() {
            return key;
        }
    }


    public static class Value {

        private static final Value EMPTY = new Value(null);
        private static final String SEPARATOR = "|";

        private final String value;

        private Value(String value) {
            this.value = value;
        }

        public boolean asBoolean() {
            return Boolean.parseBoolean(value);
        }

        public int asInt(int defaultValue) {
            if (value != null) {
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    return defaultValue;
                }
            }
            return defaultValue;
        }

        public Set<String> asSet() {
            return asSet(SEPARATOR);
        }

        Set<String> asSet(String separator) {
            if (value != null) {
                return stream(Splitter.on(separator).omitEmptyStrings().trimResults().split(value).spliterator(), false)
                        .collect(toSet());
            }
            return Collections.emptySet();
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
