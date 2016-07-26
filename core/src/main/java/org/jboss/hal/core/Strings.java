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
package org.jboss.hal.core;

/**
 * @author Harald Pehl
 */
public final class Strings {

    private static final String ELLIPSIS = "...";
    private static final String EMPTY = "";
    private static final int INDEX_NOT_FOUND = -1;

    public static String abbreviateMiddle(String string, int maxLength) {
        if (string == null || maxLength >= string.length()) {
            return string;
        }

        final int targetSting = maxLength - ELLIPSIS.length();
        final int startOffset = targetSting / 2 + targetSting % 2;
        final int endOffset = string.length() - targetSting / 2;

        return string.substring(0, startOffset) + ELLIPSIS + string.substring(endOffset);
    }

    public static String substringAfterLast(final String str, final String separator) {
        if (com.google.common.base.Strings.isNullOrEmpty(str)) {
            return str;
        }
        if (com.google.common.base.Strings.isNullOrEmpty(separator)) {
            return EMPTY;
        }
        final int pos = str.lastIndexOf(separator);
        if (pos == INDEX_NOT_FOUND || pos == str.length() - separator.length()) {
            return EMPTY;
        }
        return str.substring(pos + separator.length());
    }

    private Strings() {
    }
}
