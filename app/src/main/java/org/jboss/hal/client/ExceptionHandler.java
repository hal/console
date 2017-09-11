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
package org.jboss.hal.client;

import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import org.jboss.hal.client.bootstrap.BootstrapFailed;
import org.jboss.hal.client.bootstrap.LoadingPanel;
import org.jboss.hal.config.Endpoints;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static elemental2.dom.DomGlobal.document;

public class ExceptionHandler {

    @NonNls private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

    public static void beforeBootstrap() {
        GWT.setUncaughtExceptionHandler(e -> {
            LoadingPanel.get().off();
            String errorMessage = e != null ? e.getMessage() : Names.NOT_AVAILABLE;
            logger.error("Uncaught bootstrap error: {}", errorMessage);
            document.body.appendChild(BootstrapFailed.create(errorMessage, Endpoints.INSTANCE).asElement());
        });
    }


    private final EventBus eventBus;
    private final PlaceManager placeManager;
    private final Resources resources;

    @Inject
    public ExceptionHandler(EventBus eventBus,
            PlaceManager placeManager,
            Resources resources) {
        this.eventBus = eventBus;
        this.placeManager = placeManager;
        this.resources = resources;
    }

    public void afterBootstrap() {
        GWT.setUncaughtExceptionHandler(e -> {
            String errorMessage = e != null ? e.getMessage() : Names.NOT_AVAILABLE;
            logger.error("Uncaught exception: {}", errorMessage);
            placeManager.unlock();
            MessageEvent.fire(eventBus,
                    Message.error(resources.messages().unknownError(), errorMessage));
        });
    }
}
