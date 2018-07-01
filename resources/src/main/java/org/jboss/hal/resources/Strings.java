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
package org.jboss.hal.resources;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.Math.max;

/** Collection of string helper methods. */
public final class Strings {

    private static final int INDEX_NOT_FOUND = -1;
    private static final String ELLIPSIS = "...";
    private static final String EMPTY = "";
    private static final String SCHEME_HOST_SEPARATOR = "://";
    private static final String FQ_CLASS_NAME = "\\B\\w+(\\.[a-z])";

    public static String abbreviate(String str, int offset, int maxWidth) {
        if (str == null) {
            return null;
        }
        if (maxWidth < 4) {
            throw new IllegalArgumentException("Minimum abbreviation width is 4");
        }
        if (str.length() <= maxWidth) {
            return str;
        }
        if (offset > str.length()) {
            offset = str.length();
        }
        if (str.length() - offset < maxWidth - 3) {
            offset = str.length() - (maxWidth - 3);
        }
        String abrevMarker = "...";
        if (offset <= 4) {
            return str.substring(0, maxWidth - 3) + abrevMarker;
        }
        if (maxWidth < 7) {
            throw new IllegalArgumentException("Minimum abbreviation width with offset is 7");
        }
        if (offset + maxWidth - 3 < str.length()) {
            return abrevMarker + abbreviate(str.substring(offset), 0, maxWidth - 3);
        }
        return abrevMarker + str.substring(str.length() - (maxWidth - 3));
    }

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
        return fqcn.replaceAll(FQ_CLASS_NAME, "$1");
    }

    public static String capitalize(String str) {
        if (str != null && str.length() > 0) {
            return Character.toUpperCase(str.charAt(0)) + str.substring(1);
        }
        return str;
    }

    public static String substringAfterLast(String str, String separator) {
        if (isNullOrEmpty(str)) {
            return str;
        }
        if (isNullOrEmpty(separator)) {
            return EMPTY;
        }
        int pos = str.lastIndexOf(separator);
        if (pos == INDEX_NOT_FOUND || pos == str.length() - separator.length()) {
            return EMPTY;
        }
        return str.substring(pos + separator.length());
    }

    /**
     * <p>Strips any of a set of characters from the start and end of a String.
     * This is similar to {@link String#trim()} but allows the characters
     * to be stripped to be controlled.</p>
     *
     * <p>A {@code null} input String returns {@code null}.
     * An empty string ("") input returns the empty string.</p>
     *
     * <pre>
     * StringUtils.strip(null, *)          = null
     * StringUtils.strip("", *)            = ""
     * StringUtils.strip("abc", null)      = "abc"
     * StringUtils.strip("  abc", null)    = "abc"
     * StringUtils.strip("abc  ", null)    = "abc"
     * StringUtils.strip(" abc ", null)    = "abc"
     * StringUtils.strip("  abcyx", "xyz") = "  abc"
     * </pre>
     *
     * @param str        the String to remove characters from, may be null
     * @param stripChars the characters to remove, null treated as whitespace
     *
     * @return the stripped String, {@code null} if null String input
     */
    public static String strip(String str, String stripChars) {
        if (isNullOrEmpty(str)) {
            return str;
        }
        str = stripStart(str, stripChars);
        return stripEnd(str, stripChars);
    }

    /**
     * <p>Strips any of a set of characters from the start of a String.</p>
     *
     * <p>A {@code null} input String returns {@code null}.
     * An empty string ("") input returns the empty string.</p>
     *
     * <p>If the stripChars String is {@code null}, whitespace is
     * stripped as defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <pre>
     * StringUtils.stripStart(null, *)          = null
     * StringUtils.stripStart("", *)            = ""
     * StringUtils.stripStart("abc", "")        = "abc"
     * StringUtils.stripStart("abc", null)      = "abc"
     * StringUtils.stripStart("  abc", null)    = "abc"
     * StringUtils.stripStart("abc  ", null)    = "abc  "
     * StringUtils.stripStart(" abc ", null)    = "abc "
     * StringUtils.stripStart("yxabc  ", "xyz") = "abc  "
     * </pre>
     *
     * @param str        the String to remove characters from, may be null
     * @param stripChars the characters to remove, null treated as whitespace
     *
     * @return the stripped String, {@code null} if null String input
     */
    public static String stripStart(String str, String stripChars) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        int start = 0;
        if (stripChars == null) {
            while (start != strLen && Character.isWhitespace(str.charAt(start))) {
                start++;
            }
        } else if (stripChars.isEmpty()) {
            return str;
        } else {
            while (start != strLen && stripChars.indexOf(str.charAt(start)) != INDEX_NOT_FOUND) {
                start++;
            }
        }
        return str.substring(start);
    }

    /**
     * <p>Strips any of a set of characters from the end of a String.</p>
     *
     * <p>A {@code null} input String returns {@code null}.
     * An empty string ("") input returns the empty string.</p>
     *
     * <p>If the stripChars String is {@code null}, whitespace is
     * stripped as defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <pre>
     * StringUtils.stripEnd(null, *)          = null
     * StringUtils.stripEnd("", *)            = ""
     * StringUtils.stripEnd("abc", "")        = "abc"
     * StringUtils.stripEnd("abc", null)      = "abc"
     * StringUtils.stripEnd("  abc", null)    = "  abc"
     * StringUtils.stripEnd("abc  ", null)    = "abc"
     * StringUtils.stripEnd(" abc ", null)    = " abc"
     * StringUtils.stripEnd("  abcyx", "xyz") = "  abc"
     * StringUtils.stripEnd("120.00", ".0")   = "12"
     * </pre>
     *
     * @param str        the String to remove characters from, may be null
     * @param stripChars the set of characters to remove, null treated as whitespace
     *
     * @return the stripped String, {@code null} if null String input
     */
    public static String stripEnd(String str, String stripChars) {
        int end;
        if (str == null || (end = str.length()) == 0) {
            return str;
        }

        if (stripChars == null) {
            while (end != 0 && Character.isWhitespace(str.charAt(end - 1))) {
                end--;
            }
        } else if (stripChars.isEmpty()) {
            return str;
        } else {
            while (end != 0 && stripChars.indexOf(str.charAt(end - 1)) != INDEX_NOT_FOUND) {
                end--;
            }
        }
        return str.substring(0, end);
    }

    public static String getParent(String path) {
        String result = path;
        if (!isNullOrEmpty(path)) {
            if (!"/".equals(path)) {
                int lastSlash = path.lastIndexOf('/', path.length());
                if (lastSlash != INDEX_NOT_FOUND) {
                    if (lastSlash == 0) {
                        result = path.substring(0, 1);
                    } else {
                        result = path.substring(0, lastSlash);
                    }
                } else {
                    result = null;
                }
            }
        }
        return result;
    }

    public static String getDomain(String url) {
        String result = url;
        if (!isNullOrEmpty(url)) {
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
