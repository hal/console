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
package org.jboss.hal.client.logging;

import java.util.Date;
import java.util.logging.LogRecord;

import com.google.gwt.logging.impl.FormatterImpl;
import com.google.gwt.logging.impl.StackTracePrintStream;
import org.jboss.hal.ballroom.Format;

import static com.google.common.base.Strings.padEnd;
import static org.jboss.hal.core.Strings.abbreviate;
import static org.jboss.hal.core.Strings.abbreviateFqClassName;
import static org.jboss.hal.core.Strings.abbreviateMiddle;

class LogFormatter extends FormatterImpl {

    private static final int LEVEL_LENGTH = 4;
    private static final int LOGGER_LENGTH = 40;

    @Override
    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder();
        String timestamp = Format.timestamp(new Date(record.getMillis()));
        String level = abbreviateMiddle(padEnd(record.getLevel().getName(), LEVEL_LENGTH, ' '), LEVEL_LENGTH);
        String logger = abbreviate(padEnd(abbreviateFqClassName(record.getLoggerName()), LOGGER_LENGTH, ' '),
                LOGGER_LENGTH - 4, LOGGER_LENGTH);
        builder.append(timestamp)
                .append(" ")
                .append(level)
                .append(" ")
                .append(logger)
                .append(" ")
                .append(record.getMessage());
        if (record.getThrown() != null) {
            builder.append("\n");
            record.getThrown().printStackTrace(new StackTracePrintStream(builder));
        }
        return builder.toString();
    }
}
