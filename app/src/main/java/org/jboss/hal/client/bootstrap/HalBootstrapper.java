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

import com.gwtplatform.mvp.client.Bootstrapper;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import elemental.client.Browser;
import org.jboss.gwt.flow.Async;
import org.jboss.gwt.flow.Function;
import org.jboss.gwt.flow.FunctionContext;
import org.jboss.gwt.flow.Outcome;
import org.jboss.gwt.flow.Progress;
import org.jboss.hal.client.bootstrap.endpoint.EndpointManager;
import org.jboss.hal.client.bootstrap.functions.BootstrapFunctions;
import org.jboss.hal.resources.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * @author Harald Pehl
 */
public class HalBootstrapper implements Bootstrapper {

    private static final Logger logger = LoggerFactory.getLogger(HalBootstrapper.class);

    private final PlaceManager placeManager;
    private final EndpointManager endpointManager;
    private final BootstrapFunctions bootstrapFunctions;
    private final Resources resources;

    @Inject
    public HalBootstrapper(final PlaceManager placeManager,
            final EndpointManager endpointManager,
            final BootstrapFunctions bootstrapFunctions,
            final Resources resources) {
        this.placeManager = placeManager;
        this.endpointManager = endpointManager;
        this.bootstrapFunctions = bootstrapFunctions;
        this.resources = resources;
    }

    @Override
    public void onBootstrap() {
        Outcome<FunctionContext> outcome = new Outcome<FunctionContext>() {
            @Override
            public void onFailure(final FunctionContext context) {
                LoadingPanel.get().off();
                logger.error("Bootstrap error: {}", context.getErrorMessage()); //NON-NLS
                Browser.getDocument().getBody().appendChild(
                        BootstrapFailed.create(resources.constants().bootstrapException(), context.getErrorMessage())
                                .asElement());
            }

            @Override
            public void onSuccess(final FunctionContext context) {
                LoadingPanel.get().off();
                placeManager.revealCurrentPlace();
            }
        };

        endpointManager.select(() -> {
            LoadingPanel.get().on();
            new Async<FunctionContext>(Progress.NOOP).waterfall(
                    new FunctionContext(), outcome, (Function[]) bootstrapFunctions.functions());
        });
    }
}
