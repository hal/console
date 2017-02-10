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
package org.jboss.hal.core.expression;

import com.google.common.base.Strings;

/**
 * @author Heiko Braun
 * @date 10/4/11
 */
public class Expression {

    private final String key;
    private final String defaultValue;

    private Expression(String key, String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public static Expression of(String value) {
        if (!Strings.isNullOrEmpty(value) && value.trim().length() != 0) {
            if (value.startsWith("${") && value.endsWith("}")) {
                String token = value.substring(2, value.length() - 1);
                int idx = token.indexOf(":");
                if (idx != -1) {
                    return new Expression(token.substring(0, idx), token.substring(idx + 1, token.length()));
                } else {
                    return new Expression(token, null);
                }
            } else {
                throw new IllegalArgumentException(
                        "Illegal expression \"" + value + "\": Please use the pattern ${key[:default-value]}");
            }
        }
        throw new IllegalArgumentException("Empty expression: Please use the pattern ${key[:default-value]}");
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) { return true; }
        if (!(o instanceof Expression)) { return false; }

        Expression that = (Expression) o;

        if (!key.equals(that.key)) { return false; }
        return defaultValue != null ? defaultValue.equals(that.defaultValue) : that.defaultValue == null;
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
        return result;
    }

    /**
     * @return the expression as {@code ${key:default-value}} or {@code ${key}} if there's no default value.
     */
    @Override
    public String toString() {
        // Do not change implementation!
        StringBuilder builder = new StringBuilder();
        builder.append("${").append(key);
        if (defaultValue != null) {
            builder.append(':').append(defaultValue);
        }
        builder.append('}');
        return builder.toString();
    }
}
