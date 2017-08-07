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
package org.jboss.hal.client.bootstrap.functions;

import javax.inject.Inject;

import org.jboss.gwt.flow.Control;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.hal.config.Build;
import org.jboss.hal.config.Environment;
import org.jboss.hal.config.Settings;

import static org.jboss.hal.config.Settings.Key.COLLECT_USER_DATA;
import static org.jboss.hal.config.Settings.Key.LOCALE;
import static org.jboss.hal.config.Settings.Key.PAGE_SIZE;
import static org.jboss.hal.config.Settings.Key.RUN_AS;

/**
 * Loads the settings. Please make sure this is the last bootstrap function. This function loads the run-as role which
 * is then used by the dispatcher. But all previous bootstrap functions must not have a run-as role in the dispatcher.
 */
public class LoadSettings implements BootstrapFunction {

    private final Environment environment;
    private final Settings settings;

    @Inject
    public LoadSettings(final Environment environment, final Settings settings) {
        this.environment = environment;
        this.settings = settings;
    }

    @Override
    public void execute(final Control<FunctionContext> control) {
        logStart();

        settings.load(COLLECT_USER_DATA, environment.getHalBuild() == Build.COMMUNITY);
        settings.load(LOCALE, Settings.DEFAULT_LOCALE);
        settings.load(PAGE_SIZE, Settings.DEFAULT_PAGE_SIZE);
        settings.load(RUN_AS, null);

        logDone();
        control.proceed();
    }

    @Override
    public String name() {
        return "Bootstrap[LoadSettings]";
    }
}
