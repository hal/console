/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.logging;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.jboss.hal.ballroom.Format;

import com.google.gwt.logging.impl.FormatterImpl;
import com.google.gwt.logging.impl.StackTracePrintStream;

import static com.google.common.base.Strings.padEnd;
import static org.jboss.hal.resources.Strings.abbreviate;
import static org.jboss.hal.resources.Strings.abbreviateFqClassName;

class LogFormatter extends FormatterImpl {

    private static final int LOGGER_LENGTH = 40;

    @Override
    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder();
        String timestamp = Format.timestamp(new Date(record.getMillis()));
        String logger = abbreviate(padEnd(abbreviateFqClassName(record.getLoggerName()), LOGGER_LENGTH, ' '),
                LOGGER_LENGTH - 4, LOGGER_LENGTH);
        builder.append(timestamp)
                .append(" ")
                .append(level(record.getLevel().intValue()))
                .append(logger)
                .append(" ")
                .append(record.getMessage());
        if (record.getThrown() != null) {
            builder.append("\n");
            record.getThrown().printStackTrace(new StackTracePrintStream(builder));
        }
        return builder.toString();
    }

    public String level(int level) {
        if (level >= Level.SEVERE.intValue()) {
            return "ERROR ";
        } else if (level >= Level.WARNING.intValue()) {
            return "WARN  ";
        } else if (level >= Level.INFO.intValue()) {
            return "INFO  ";
        } else {
            return "DEBUG ";
        }
    }
}
