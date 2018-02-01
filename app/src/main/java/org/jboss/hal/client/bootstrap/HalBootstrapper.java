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
import org.jboss.hal.client.bootstrap.endpoint.EndpointManager;
import org.jboss.hal.client.bootstrap.tasks.BootstrapTasks;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.core.ExceptionHandler;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Func0;

import static elemental2.dom.DomGlobal.document;
import static elemental2.dom.DomGlobal.window;

public class HalBootstrapper implements Bootstrapper {

    @NonNls private static final Logger logger = LoggerFactory.getLogger(HalBootstrapper.class);

    private final PlaceManager placeManager;
    private final EndpointManager endpointManager;
    private final Endpoints endpoints;
    private final BootstrapTasks bootstrapTasks;
    private final ExceptionHandler exceptionHandler;

    @Inject
    public HalBootstrapper(PlaceManager placeManager,
            EndpointManager endpointManager,
            Endpoints endpoints,
            BootstrapTasks bootstrapTasks,
            ExceptionHandler exceptionHandler) {
        this.placeManager = placeManager;
        this.endpointManager = endpointManager;
        this.endpoints = endpoints;
        this.bootstrapTasks = bootstrapTasks;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void onBootstrap() {
        // event for users of the JS API
        Event event = new Event("halReady"); //NON-NLS
        window.dispatchEvent(event);

        endpointManager.select(() -> {
            LoadingPanel.get().on();

            Observable.from(bootstrapTasks.functions())
                    .flatMapCompletable(Func0::call, false, 1)
                    .doOnTerminate(() -> LoadingPanel.get().off())
                    .doOnCompleted(() -> {
                        logger.info("Bootstrap finished");
                        placeManager.revealCurrentPlace();
                        exceptionHandler.afterBootstrap();
                    })
                    .doOnError(e -> {
                        logger.error("Bootstrap error: {}", e.getMessage());
                        document.body.appendChild(BootstrapFailed.create(e.getMessage(), endpoints).asElement());
                    })
                    .subscribe();
        });
    }
}
