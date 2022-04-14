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
package org.jboss.hal.core;

import javax.inject.Inject;

import org.jboss.elemento.Elements;
import org.jboss.hal.flow.Progress;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jboss.hal.spi.Footer;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.GWT;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import elemental2.dom.Event;
import elemental2.dom.EventInit;
import elemental2.promise.Promise;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import jsinterop.base.Js;

import static elemental2.dom.DomGlobal.document;
import static elemental2.dom.DomGlobal.window;
import static org.jboss.hal.resources.CSS.withProgress;

public class ExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);
    private static boolean pendingLifecycleAction = false;

    static void setPendingLifecycleAction(boolean value) {
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
        window.addEventListener("unhandledrejection", e -> {
            if (!pendingLifecycleAction) {
                PromiseRejectionEvent event = Js.cast(e);
                handleError("Unhandled promise rejection", String.valueOf(event.reason));
            }
        });
        GWT.setUncaughtExceptionHandler(e -> {
            if (!pendingLifecycleAction) {
                handleError("Uncaught exception", e != null ? e.getMessage() : Names.NOT_AVAILABLE);
            }
        });
    }

    private void handleError(String type, String errorMessage) {
        logger.error("{}: {}", type, errorMessage);
        placeManager.unlock();
        progress.finish();
        Elements.stream(document.querySelectorAll("." + withProgress))
                .forEach(element -> element.classList.remove(withProgress));
        MessageEvent.fire(eventBus, Message.error(resources.messages().unknownError(), errorMessage));
    }

    @SuppressWarnings({ "RedundantCast", "unused" })
    @JsType(isNative = true, namespace = JsPackage.GLOBAL)
    static class PromiseRejectionEvent extends Event {

        public PromiseRejectionEvent() {
            // This super call is here only for the code to compile; it is never executed.
            super((String) null, (EventInit) null);
        }

        Promise<?> promise;
        Object reason;
    }
}
