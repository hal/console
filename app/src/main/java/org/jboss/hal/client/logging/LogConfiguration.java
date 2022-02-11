/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.client.logging;

import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.gwt.core.client.GWT;
import com.google.gwt.logging.client.DefaultLevel;
import com.google.gwt.user.client.Window;

import static elemental2.dom.DomGlobal.console;
import static java.util.logging.Level.*;

public final class LogConfiguration {

    private static final Level DEFAULT_LEVEL = INFO;
    private static final Map<String, Level> KNOWN_LEVELS = new ImmutableMap.Builder<String, Level>()
            .put("SEVERE", SEVERE)
            .put("FATAL", SEVERE)
            .put("ERROR", SEVERE)
            .put("WARNING", WARNING)
            .put("WARN", WARNING)
            .put("INFO", INFO)
            .put("CONFIG", CONFIG)
            .put("DEBUG", FINE)
            .put("FINE", FINE)
            .put("FINER", FINER)
            .put("FINEST", FINEST)
            .build();

    public static void configure() {
        java.util.logging.Logger root = java.util.logging.Logger.getLogger("");
        for (Handler handler : root.getHandlers()) {
            root.removeHandler(handler);
        }
        root.setUseParentHandlers(false);
        setLevels(root);
        root.addHandler(new LogHandler());
        console.info("Set log level to " + root.getLevel());
    }

    private static void setLevels(java.util.logging.Logger l) {
        Level level = null;
        String levelParam = Window.Location.getParameter("logLevel");

        if (Strings.emptyToNull(levelParam) != null) {
            String safeLevelParam = levelParam.toUpperCase();
            if (KNOWN_LEVELS.containsKey(safeLevelParam)) {
                level = KNOWN_LEVELS.get(safeLevelParam);
            } else {
                console.error("Unable to parse log level '" + levelParam + "'. " +
                        "Fall back to " + DEFAULT_LEVEL + ".");
            }
        } else {
            DefaultLevel defaultLevel = GWT.create(DefaultLevel.class);
            level = defaultLevel.getLevel();
        }
        if (level == null) {
            level = DEFAULT_LEVEL;
        }

        l.setLevel(level);
    }

    private LogConfiguration() {
    }
}
