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
package org.jboss.hal.config;

import java.util.EnumMap;
import java.util.Map;
import javax.inject.Inject;

import org.jetbrains.annotations.NonNls;

/**
 * @author Harald Pehl
 */
public class Settings {

    public enum Key {
        COLLECT_USER_DATA("collect-user-data", true),
        LOCALE("locale", true),
        PAGE_LENGTH("page-length", true),
        RUN_AS("run-as", false);

        private final String key;
        private final boolean persistent;

        Key(@NonNls final String key, final boolean persistent) {

            this.key = key;
            this.persistent = persistent;
        }

        private String key() {
            return key;
        }

        private boolean persistent() {
            return persistent;
        }
    }


    public static class Value {

        private static final Value EMPTY = new Value(null);

        private final String value;

        private Value(final String value) {this.value = value;}

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

        public String value() {
            return value;
        }
    }


    @Inject
    public static Settings INSTANCE;
    public static final String DEFAULT_LOCALE = "en";
    public static final int DEFAULT_PAGE_LENGTH = 10;

    private final Map<Key, Value> values;

    public Settings() {
        values = new EnumMap<>(Key.class);
    }

    public <T> void load(Key key, T defaultValue) {
        String value = null; // TODO Read from cookie
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
        values.put(key, new Value(String.valueOf(value)));
        // TODO Write (persistent) cookie
    }
}
