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
package org.jboss.hal.core;

import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static elemental2.dom.DomGlobal.document;
import static org.jboss.hal.resources.CSS.withProgress;

public class ExceptionHandler {

    @NonNls private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);
    private static boolean pendingLifecycleAction = false;

    public static void setPendingLifecycleAction(boolean value) {
        pendingLifecycleAction = value;
        logger.debug("ExceptionHandler.pendingLifecycleAction = {}", pendingLifecycleAction);
    }

    private final EventBus eventBus;
    private final PlaceManager placeManager;
    private final Progress progress;
    private final Resources resources;

    @Inject
    public ExceptionHandler(EventBus eventBus,
            PlaceManager placeManager,
            @Footer Progress progress,
            Resources resources) {
        this.eventBus = eventBus;
        this.placeManager = placeManager;
        this.progress = progress;
        this.resources = resources;
    }

    public void afterBootstrap() {
        GWT.setUncaughtExceptionHandler(e -> {
            if (!pendingLifecycleAction) {
                String errorMessage = e != null ? e.getMessage() : Names.NOT_AVAILABLE;
                logger.error("Uncaught exception: {}", errorMessage);
                placeManager.unlock();
                progress.finish();
                stopProgress();
                MessageEvent.fire(eventBus,
                        Message.error(resources.messages().unknownError(), errorMessage));
            }
        });
    }

    private void stopProgress() {
        Elements.stream(document.querySelectorAll("." + withProgress))
                .forEach(element -> element.classList.remove(withProgress));
    }
}
