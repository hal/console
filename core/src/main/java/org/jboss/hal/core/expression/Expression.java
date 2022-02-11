/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.core.expression;

import java.util.Objects;

import com.google.common.base.Strings;

public class Expression {

    private static final String EXPRESSION_START = "${";
    private static final String EXPRESSION_END = "}";

    public static boolean isExpression(String value) {
        return !Strings.isNullOrEmpty(value) && value.trim().length() != 0 &&
                value.contains(EXPRESSION_START) && value.indexOf(EXPRESSION_END) > 1;
    }

    public static Expression of(String value) {
        if (!Strings.isNullOrEmpty(value) && value.trim().length() != 0) {
            if (value.contains(EXPRESSION_START) && value.indexOf(EXPRESSION_END) > 1) {
                int init = value.indexOf(EXPRESSION_START);
                int end = value.indexOf(EXPRESSION_END);
                String token = value.substring(init + 2, end);
                String prefix = null;
                String suffix = null;
                if (init > 0) {
                    prefix = value.substring(0, init);
                }
                if (end < value.length() - 1) {
                    suffix = value.substring(end + 1);
                }
                int idx = token.indexOf(":");
                String defaultValue = null;
                if (idx != -1) {
                    defaultValue = token.substring(idx + 1, token.length());
                    token = token.substring(0, idx);
                }
                return new Expression(prefix, token, defaultValue, suffix);
            } else {
                throw new IllegalArgumentException(
                        "Illegal expression \"" + value + "\": Please use the pattern ${key[:default-value]}");
            }
        }
        throw new IllegalArgumentException("Empty expression: Please use the pattern ${key[:default-value]}");
    }

    private String prefix;
    private String suffix;
    private String key;
    private String defaultValue;

    private Expression(String prefix, String key, String defaultValue, String suffix) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public String getKey() {
        return key;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Expression that = (Expression) o;
        return Objects.equals(prefix, that.prefix) &&
                Objects.equals(suffix, that.suffix) &&
                Objects.equals(key, that.key) &&
                Objects.equals(defaultValue, that.defaultValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefix, suffix, key, defaultValue);
    }

    /**
     * @return the expression as {@code ${key:default-value}} or {@code ${key}} if there's no default value.
     */
    @Override
    public String toString() {
        // Do not change implementation!
        StringBuilder builder = new StringBuilder();
        if (prefix != null) {
            builder.append(prefix);
        }
        builder.append(EXPRESSION_START).append(key);
        if (defaultValue != null) {
            builder.append(':').append(defaultValue);
        }
        builder.append(EXPRESSION_END);
        if (suffix != null) {
            builder.append(suffix);
        }
        return builder.toString();
    }
}
