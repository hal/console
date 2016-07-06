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
package org.jboss.hal.client.runtime.subsystem.logging;

import java.util.Date;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.i18n.shared.DateTimeFormat;
import org.jboss.hal.ballroom.Format;
import org.jboss.hal.dmr.ModelNode;

import static com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat.ISO_8601;

/**
 * @author Harald Pehl
 */
class LogFile extends ModelNode {

    private static final String FILE_NAME = "file-name";
    private static final String FILE_SIZE = "file-size";
    private static final String LAST_MODIFIED_DATE = "last-modified-date";
    private static final String LAST_MODIFIED_TIMESTAMP = "last-modified-timestamp";

    private static final DateTimeFormat DATE_TIME_FORMAT = DateTimeFormat.getFormat(ISO_8601);
    private static final NumberFormat SIZE_FORMAT = NumberFormat.getFormat("#,##0.#");

    LogFile(final ModelNode node) {
        set(node);
    }

    LogFile(final String name, final ModelNode node) {
        set(node);
        get(FILE_NAME).set(name);
    }

    public String getFilename() {
        return get(FILE_NAME).asString(); //NON-NLS
    }

    public Date getLastModifiedDate() {
        // first try LAST_MODIFIED_DATE then LAST_MODIFIED_TIMESTAMP
        Date date = convert(LAST_MODIFIED_DATE);
        if (date == null) {
            date = convert(LAST_MODIFIED_TIMESTAMP);
        }
        return date;
    }

    private Date convert(String attribute) {
        if (hasDefined(attribute)) {
            try {
                return DATE_TIME_FORMAT.parse(get(attribute).asString());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    public String getFormattedLastModifiedDate() {
        Date lastModifiedDate = getLastModifiedDate();
        if (lastModifiedDate != null) {
            return Format.shortDateTime(lastModifiedDate);
        }
        return null;
    }

    public long getSize() {
        return get(FILE_SIZE).asLong();
    }

    public String getFormattedSize() {
        return Format.humanReadableFileSize(getSize());
    }
}
