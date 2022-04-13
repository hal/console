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
package org.jboss.hal.client.bootstrap.tasks;

import javax.inject.Inject;
import javax.inject.Provider;

import org.jboss.hal.config.Environment;
import org.jboss.hal.config.Settings;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.spi.Footer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.web.bindery.event.shared.EventBus;

import static elemental2.dom.DomGlobal.setTimeout;
import static java.util.Collections.singletonList;
import static org.jboss.hal.config.Settings.DEFAULT_POLL_TIME;
import static org.jboss.hal.config.Settings.Key.POLL;
import static org.jboss.hal.config.Settings.Key.POLL_TIME;
import static org.jboss.hal.flow.Flow.parallel;

public class PollingTasks implements InitializedTask {

    private static final Logger logger = LoggerFactory.getLogger(PollingTasks.class);

    private final EventBus eventBus;
    private final Dispatcher dispatcher;
    private final StatementContext statementContext;
    private final Settings settings;
    private final Provider<Progress> progress;
    private final Environment environment;

    @Inject
    public PollingTasks(EventBus eventBus, Dispatcher dispatcher, StatementContext statementContext, Settings settings,
            @Footer Provider<Progress> progress, Environment environment) {
        this.eventBus = eventBus;
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;
        this.settings = settings;
        this.progress = progress;
        this.environment = environment;
    }

    @Override
    public void run() {
        boolean pollEnabled = settings.get(POLL).asBoolean();
        int pollTime = settings.get(POLL_TIME).asInt(DEFAULT_POLL_TIME);
        logger.info("Polling mechanism is: {}", (pollEnabled ? "on" : "off"));
        if (pollEnabled) {
            setTimeout(__ -> parallel(new FlowContext(Progress.NOOP), singletonList(
                    new FindNonProgressingTask(eventBus, dispatcher, environment, statementContext, progress))),
                    pollTime * 1000);
        }
    }
}
