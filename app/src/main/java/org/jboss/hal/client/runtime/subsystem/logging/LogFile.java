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

import org.jboss.hal.ballroom.Format;
import org.jboss.hal.dmr.ModelNode;

import static org.jboss.hal.dmr.ModelDescriptionConstants.LOGGING_PROFILE;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeDate;

class LogFile extends ModelNode {

    // TODO Move to ModelDescriptionConstants
    private static final String FILE_NAME = "file-name";
    private static final String FILE_SIZE = "file-size";
    private static final String LAST_MODIFIED_TIMESTAMP = "last-modified-timestamp";

    LogFile(ModelNode node) {
        set(node);
    }

    LogFile(String name, ModelNode node) {
        set(node);
        get(FILE_NAME).set(name);
    }

    LogFile(String name, String logProfile, ModelNode node) {
        set(node);
        get(FILE_NAME).set(name);
        get(LOGGING_PROFILE).set(logProfile);
    }

    public String getFilename() {
        return get(FILE_NAME).asString();
    }

    public String getLoggingProfile() {
        return hasDefined(LOGGING_PROFILE) ? get(LOGGING_PROFILE).asString() : null;
    }

    public Date getLastModifiedDate() {
        return failSafeDate(this, LAST_MODIFIED_TIMESTAMP);
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
