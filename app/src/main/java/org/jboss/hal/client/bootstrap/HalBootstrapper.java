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
package org.jboss.hal.client.bootstrap;

import javax.inject.Inject;

import org.jboss.hal.client.bootstrap.endpoint.EndpointManager;
import org.jboss.hal.client.bootstrap.tasks.BootstrapTasks;
import org.jboss.hal.client.bootstrap.tasks.InitializationTasks;
import org.jboss.hal.client.bootstrap.tasks.InitializedTask;
import org.jboss.hal.core.ExceptionHandler;
import org.jboss.hal.flow.Flow;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Outcome;
import org.jboss.hal.js.Browser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gwtplatform.mvp.client.Bootstrapper;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import elemental2.dom.Event;

import static elemental2.dom.DomGlobal.window;

public class HalBootstrapper implements Bootstrapper {

    private static final Logger logger = LoggerFactory.getLogger(HalBootstrapper.class);

    private final PlaceManager placeManager;
    private final EndpointManager endpointManager;
    private final BootstrapTasks bootstrapTasks;
    private final InitializationTasks initializationTasks;
    private final ExceptionHandler exceptionHandler;

    @Inject
    public HalBootstrapper(PlaceManager placeManager,
            EndpointManager endpointManager,
            BootstrapTasks bootstrapTasks,
            InitializationTasks initializationTasks,
            ExceptionHandler exceptionHandler) {
        this.placeManager = placeManager;
        this.endpointManager = endpointManager;
        this.bootstrapTasks = bootstrapTasks;
        this.initializationTasks = initializationTasks;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void onBootstrap() {
        // event for users of the JS API
        if (Browser.isIE()) {
            logger.warn("Custom events are not supported in IE! HAL extension won't work.");
        } else {
            Event event = new Event("halReady"); // NON-NLS
            window.dispatchEvent(event);
        }

        endpointManager.select(() -> {
            LoadingPanel.get().on();
            Flow.series(new FlowContext(), bootstrapTasks.tasks()).subscribe(new Outcome<FlowContext>() {
                @Override
                public void onError(FlowContext context, Throwable error) {
                    logger.error("Bootstrap error: {}", error.getMessage());
                    LoadingPanel.get().off();
                }

                @Override
                public void onSuccess(FlowContext context) {
                    logger.info("Bootstrap finished");
                    LoadingPanel.get().off();
                    placeManager.revealCurrentPlace();
                    exceptionHandler.afterBootstrap();
                    for (InitializedTask task : initializationTasks.tasks()) {
                        task.run();
                    }
                }
            });
        });
    }
}
