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

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static elemental2.dom.DomGlobal.console;

public class LogHandler extends Handler {

    LogHandler() {
        setFormatter(new LogFormatter());
        setLevel(Level.ALL);
    }

    @Override
    public void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }
        String message = getFormatter().format(record);
        int val = record.getLevel().intValue();
        if (val >= Level.SEVERE.intValue()) {
            console.error(message);
        } else if (val >= Level.WARNING.intValue()) {
            console.warn(message);
        } else if (val >= Level.INFO.intValue()) {
            console.info(message);
        } else {
            console.log(message);
        }
    }

    @Override
    public void flush() {
        // No action needed
    }

    @Override
    public void close() throws SecurityException {
        // No action needed
    }
}
