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
package org.jboss.hal.client.bootstrap.tasks;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.logging.client.DefaultLevel;
import com.google.gwt.user.client.Window;
import org.jboss.hal.client.logging.LogHandler;
import rx.Completable;

public class SetupLoggingTask implements BootstrapTask {

    @Override
    public Completable call() {
        Logger root = Logger.getLogger("");
        for (Handler handler : root.getHandlers()) {
            root.removeHandler(handler);
        }
        root.setUseParentHandlers(false);
        setLevels(root);
        root.addHandler(new LogHandler());

        return Completable.complete();
    }

    private void setLevels(Logger l) {
        // try to pull the log level from the query param
        String levelParam = Window.Location.getParameter("logLevel");
        Level level = levelParam == null ? null : Level.parse(levelParam);
        if (level != null) {
            l.setLevel(level);
        } else {
            // if it isn't there, then pull it from the gwt.xml file
            DefaultLevel defaultLevel = GWT.create(DefaultLevel.class);
            l.setLevel(defaultLevel.getLevel());
        }
    }
}
