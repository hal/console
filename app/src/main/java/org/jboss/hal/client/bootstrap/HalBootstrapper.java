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
package org.jboss.hal.client.bootstrap;

import javax.inject.Inject;

import com.gwtplatform.mvp.client.Bootstrapper;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import elemental2.dom.Event;
import org.jboss.hal.client.ExceptionHandler;
import org.jboss.hal.client.bootstrap.endpoint.EndpointManager;
import org.jboss.hal.client.bootstrap.functions.BootstrapSteps;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.flow.FlowContext;
import org.jboss.hal.flow.Outcome;
import org.jboss.hal.flow.Progress;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static elemental2.dom.DomGlobal.document;
import static elemental2.dom.DomGlobal.window;
import static org.jboss.hal.flow.Flow.series;

public class HalBootstrapper implements Bootstrapper {

    @NonNls private static final Logger logger = LoggerFactory.getLogger(HalBootstrapper.class);

    private final PlaceManager placeManager;
    private final EndpointManager endpointManager;
    private final Endpoints endpoints;
    private final BootstrapSteps bootstrapFunctions;
    private final ExceptionHandler exceptionHandler;

    @Inject
    public HalBootstrapper(PlaceManager placeManager,
            EndpointManager endpointManager,
            Endpoints endpoints,
            BootstrapSteps bootstrapFunctions,
            ExceptionHandler exceptionHandler) {
        this.placeManager = placeManager;
        this.endpointManager = endpointManager;
        this.endpoints = endpoints;
        this.bootstrapFunctions = bootstrapFunctions;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void onBootstrap() {
        // event for users of the JS API
        Event event = new Event("halReady"); //NON-NLS
        window.dispatchEvent(event);

        endpointManager.select(() -> {
            LoadingPanel.get().on();
            series(Progress.NOOP, new FlowContext(), bootstrapFunctions.functions())
                    .subscribe(new Outcome<FlowContext>() {
                        @Override
                        public void onSuccess(FlowContext context) {
                            LoadingPanel.get().off();
                            logger.info("Bootstrap finished");
                            placeManager.revealCurrentPlace();
                            exceptionHandler.afterBootstrap();
                        }

                        @Override
                        public void onError(FlowContext context, Throwable error) {
                            LoadingPanel.get().off();
                            logger.error("Bootstrap error: {}", error.getMessage());
                            document.body.appendChild(
                                    BootstrapFailed.create(error.getMessage(), endpoints).asElement());
                        }
                    });
        });
    }
}
