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

import static java.lang.Math.max;

/** Collection of string helper methods. */
public final class Strings {

    private static final int INDEX_NOT_FOUND = -1;
    private static final String ELLIPSIS = "...";
    private static final String EMPTY = "";
    private static final String SCHEME_HOST_SEPARATOR = "://";
    private static final String FQ_CLASS_NAME = "\\B\\w+(\\.[a-z])";

    public static String abbreviateMiddle(String string, int maxLength) {
        if (string == null || maxLength >= string.length()) {
            return string;
        }
        if (maxLength == 0) {
            return "";
        }
        if (maxLength <= ELLIPSIS.length()) {
            return string.substring(0, maxLength);
        }

        int targetSting = max(1, maxLength - ELLIPSIS.length());
        int startOffset = targetSting / 2 + targetSting % 2;
        int endOffset = string.length() - targetSting / 2;

        return string.substring(0, startOffset) + ELLIPSIS + string.substring(endOffset);
    }

    public static String abbreviateFqClassName(String fqcn) {
        return fqcn.replaceAll(FQ_CLASS_NAME,"$1");

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

    public static String getParent(String path) {
        String result = path;
        if (!com.google.common.base.Strings.isNullOrEmpty(path)) {
            if (!"/".equals(path)) {
                int lastSlash = path.lastIndexOf('/', path.length());
                if (lastSlash != INDEX_NOT_FOUND) {
                    if (lastSlash == 0) {
                        result = path.substring(0, 1);
                    } else {
                        result = path.substring(0, lastSlash);
                    }
                }
            }
        }
        return result;
    }

    public static String getDomain(String url) {
        String result = url;
        if (!com.google.common.base.Strings.isNullOrEmpty(url)) {
            int index = url.indexOf(SCHEME_HOST_SEPARATOR);
            if (index != INDEX_NOT_FOUND) {
                int slash = url.substring(index + SCHEME_HOST_SEPARATOR.length()).indexOf('/');
                if (slash != INDEX_NOT_FOUND) {
                    result = url.substring(0, index + slash + SCHEME_HOST_SEPARATOR.length());
                }
                int questionMark = url.substring(index + SCHEME_HOST_SEPARATOR.length()).indexOf('?');
                if (questionMark != INDEX_NOT_FOUND) {
                    result = url.substring(0, index + questionMark + SCHEME_HOST_SEPARATOR.length());
                }
            }
        }
        return result;
    }

    private Strings() {
    }
}
