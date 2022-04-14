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
package org.jboss.hal.core.finder;

import javax.inject.Inject;

import org.jboss.hal.spi.Callback;

import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.gwtplatform.mvp.client.proxy.NavigationEvent;

import static elemental2.dom.DomGlobal.clearTimeout;
import static elemental2.dom.DomGlobal.console;
import static elemental2.dom.DomGlobal.document;
import static elemental2.dom.DomGlobal.setTimeout;
import static org.jboss.hal.resources.CSS.withProgress;
import static org.jboss.hal.resources.UIConstants.MEDIUM_TIMEOUT;

/**
 * Class to monitor item actions and show a progress indicator if they take longer than a given timeout. Relies on an unique
 * item id implemented by {@link ItemDisplay#getId()} and specified in the column setup.
 */
public class ItemMonitor {

    public static void startProgress(final String itemId) {
        elemental2.dom.Element element = document.getElementById(itemId);
        if (element != null && !element.classList.contains(withProgress)) {
            element.classList.add(withProgress);
            console.log("### Started progress for item %s", itemId);
        }
    }

    public static void stopProgress(final String itemId) {
        elemental2.dom.Element element = document.getElementById(itemId);
        if (element != null) {
            element.classList.remove(withProgress);
            console.log("### Stopped progress for item %s", itemId);
        }
    }

    private final EventBus eventBus;
    private double timeoutHandle = -1;
    private HandlerRegistration handlerRegistration;

    @Inject
    public ItemMonitor(final EventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Wraps and monitors an item action which triggers a place request.
     */
    public <T> ItemActionHandler<T> monitorPlaceRequest(final String itemId, final String nameToken,
            final Callback callback) {
        return itm -> {
            callback.execute();
            startProgress(itemId);
            timeoutHandle = setTimeout(whatever -> handlerRegistration = eventBus.addHandler(NavigationEvent.getType(),
                    navigationEvent -> {
                        if (nameToken.equals(navigationEvent.getRequest().getNameToken())) {
                            handlerRegistration.removeHandler();
                            clearTimeout(timeoutHandle);
                            stopProgress(itemId);
                        }
                    }), MEDIUM_TIMEOUT);
        };
    }
}
