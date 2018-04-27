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
package org.jboss.hal.processor.mbui;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ExpressionParser {

    private static final Pattern PATTERN = Pattern.compile("\\$\\{(.*?)\\}");

    private ExpressionParser() {
    }

    static Map<String, String> parse(String input) {
        if (input != null) {
            Map<String, String> matches = new HashMap<>();
            Matcher matcher = PATTERN.matcher(input);
            while (matcher.find()) {
                String match = matcher.group();
                validate(match);
                matches.put(match, stripExpression(match));
            }
            return matches;
        }
        return Collections.emptyMap();
    }

    @SuppressWarnings("DuplicateStringLiteralInspection")
    private static void validate(final String pattern) {
        if (!isExpression(pattern)) {
            throw new IllegalArgumentException("Invalid expression: " + pattern);
        }
        if (pattern.lastIndexOf("${") != 0 || pattern.indexOf("}") != pattern.length() - 1) {
            throw new IllegalArgumentException("Invalid expression: " + pattern);
        }
    }


    static boolean isExpression(String value) {
        return value != null && value.contains("${") && value.indexOf("}") > 1;
    }

    static String stripExpression(String pattern) {
        if (isExpression(pattern)) {
            int start = "${".length();
            int end = pattern.length() - "}".length();
            return pattern.substring(start, end);
        }
        return pattern;
    }

    static String templateSafeValue(final String value) {
        if (value != null) {
            return isExpression(value) ? stripExpression(value) : "\"" + value + "\"";
        }
        return null;
    }
}
