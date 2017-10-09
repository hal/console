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
package org.jboss.hal.config.semver;

public class Versions {

    private static final int INDEX_NOT_FOUND = -1;

    public static Version parseVersion(String versionString) {
        Version version = null;
        try {
            int defaultIndex = ordinalIndexOf(versionString, ".", 3, false);
            if (INDEX_NOT_FOUND == defaultIndex) { defaultIndex = versionString.length(); }

            version = Version.valueOf(versionString.substring(0, defaultIndex));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(versionString);
        }
        return version;
    }

    private static int ordinalIndexOf(final CharSequence str, final CharSequence searchStr, final int ordinal,
            final boolean lastIndex) {
        if (str == null || searchStr == null || ordinal <= 0) {
            return INDEX_NOT_FOUND;
        }
        if (searchStr.length() == 0) {
            return lastIndex ? str.length() : 0;
        }
        int found = 0;
        int index = lastIndex ? str.length() : INDEX_NOT_FOUND;
        do {
            if (lastIndex) {
                index = lastIndexOf(str, searchStr, index - 1);
            } else {
                index = indexOf(str, searchStr, index + 1);
            }
            if (index < 0) {
                return index;
            }
            found++;
        } while (found < ordinal);
        return index;
    }

    private static int lastIndexOf(final CharSequence cs, final CharSequence searchChar, final int start) {
        return cs.toString().lastIndexOf(searchChar.toString(), start);
    }

    private static int indexOf(final CharSequence cs, final CharSequence searchChar, final int start) {
        return cs.toString().indexOf(searchChar.toString(), start);
    }
}
