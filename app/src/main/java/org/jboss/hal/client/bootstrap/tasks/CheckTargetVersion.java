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

import javax.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import elemental2.dom.DomGlobal;
import org.jboss.hal.config.Environment;
import org.jboss.hal.meta.ManagementModel;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.resources.UIConstants;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckTargetVersion implements InitializedTask {

    @NonNls private static Logger logger = LoggerFactory.getLogger(CheckTargetVersion.class);

    private final Environment environment;
    private final EventBus eventBus;
    private final Resources resources;

    @Inject
    public CheckTargetVersion(Environment environment, EventBus eventBus, Resources resources) {
        this.environment = environment;
        this.eventBus = eventBus;
        this.resources = resources;
    }

    @Override
    public void run() {
        DomGlobal.setTimeout(o -> {
            if (environment.getManagementVersion().lessThan(ManagementModel.TARGET_VERSION)) {
                logger.warn("The management model version {} is lower than the target version {}",
                        environment.getManagementVersion(), ManagementModel.TARGET_VERSION);
                MessageEvent.fire(eventBus, Message.warning(resources.messages().managementVersionMismatch(
                        environment.getManagementVersion().toString(), ManagementModel.TARGET_VERSION.toString()),
                        resources.constants().managementVersionMismatchDescription()));
            }
        }, UIConstants.MEDIUM_TIMEOUT);
    }
}
